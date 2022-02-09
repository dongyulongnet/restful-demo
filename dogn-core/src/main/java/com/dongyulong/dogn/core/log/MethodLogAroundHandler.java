package com.dongyulong.dogn.core.log;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.dongyulong.dogn.common.entities.ReplyMap;
import com.dongyulong.dogn.common.entities.ResultMap;
import com.dongyulong.dogn.common.tookit.JsonMapper;
import com.dongyulong.dogn.core.annotation.LogOpen;
import com.dongyulong.dogn.core.annotation.UseLogger;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 打印方法(不适用静态方法)开始和结束的日志，
 * 输出方法名（或指定的业务表示{@link LogOpen}）和方法参数，方法的注解优先级高于类的注解{@link LogOpen}，
 * 打印私有方法需要将注解{@link LogOpen#open}设置为true,类上设置无效
 *
 * @author dongy/和小奇
 * @date 2019/2/22 10:38 AM
 * @see LogOpen
 * @see UseLogger
 */

@Slf4j
public class MethodLogAroundHandler {

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 缓存日志模板
     * className.method start. parameter1:value1,parameter2:value2 ...
     * flag{开始/结束}. parameter1:{},parameter2:{},parameter3:{} ...
     */
    private final Map<String, String> methodParameterTemplate = new ConcurrentHashMap<>(64);

    /**
     * businessFlag
     */
    private final Map<String, String> methodBusinessFlag = new ConcurrentHashMap<>(64);

    /**
     * 没有开启日志的方法
     */
    private final Set<String> methodWithoutLog = new ConcurrentHashSet<>();

    /**
     * 缓存Logger对象
     */
    private final Map<String, Logger> loggerCache = new ConcurrentHashMap<>(16);

    /**
     * 基本数据类型及其包装类型
     */
    private static final Set<String> TYPES = new HashSet<>(Arrays.asList(
            byte.class.getTypeName(), Byte.class.getTypeName(),
            short.class.getTypeName(), Short.class.getTypeName(),
            int.class.getTypeName(), Integer.class.getTypeName(),
            long.class.getTypeName(), Long.class.getTypeName(),
            float.class.getTypeName(), Float.class.getTypeName(),
            double.class.getTypeName(), Double.class.getTypeName(),
            boolean.class.getTypeName(), Boolean.class.getTypeName(),
            char.class.getTypeName(), Character.class.getTypeName()));


    @SneakyThrows
    public Object around(final ProceedingJoinPoint pjp) {
        // 获取对象、方法、参数
        Object target = pjp.getTarget();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Object[] args = pjp.getArgs();

        String methodName = pjp.getTarget().getClass().getSimpleName() + "." + signature.getMethod().getName();
        if (methodWithoutLog.contains(methodName)) {
            return pjp.proceed(args);
        }
        String template = methodParameterTemplate.get(methodName);
        // 缓存中没有模板
        if (template == null || "".equals(template)) {
            // 是否打印日志
            if (isPrintLog(target, method)) {
                // 创建并缓存模板
                template = generateTemplate(target, method, methodName);
            } else {
                // 不打印日志
                methodWithoutLog.add(methodName);
                return pjp.proceed(args);
            }
        }
        String className = target.getClass().getName();
        Logger logger = loggerCache.get(className);
        // 是否使用指定的Logger
        if (logger == null) {
            logger = getLogger(target);
            loggerCache.put(className, logger);
        }
        Object[] params = processArgs(args);
        // 打印开始日志
        params[0] = " 开始 ";
        logger.info(template, params);
        Object result = pjp.proceed(args);
        // 打印结束日志
        if (result instanceof ReplyMap) {
            ReplyMap resultMap = (ReplyMap) result;
            logger.info("{}结束, resultCode:{}, resultMsg:{}, ret:{}", methodBusinessFlag.get(methodName), resultMap.getCode(), resultMap.getMessage(), resultMap.get("ret"));
        } else if (result instanceof ResultMap) {
            ResultMap resultMap = (ResultMap) result;
            logger.info("{}结束, resultCode:{}, resultMsg:{}, ret:{}", methodBusinessFlag.get(methodName), resultMap.getCode(), resultMap.getMsg(), resultMap.get("ret"));
        } else {
            logger.info("{}结束,params:{}, ret:{}", methodBusinessFlag.get(methodName), params, result);
        }
        return result;
    }

    /**
     * 校验方法是否开启了日志
     *
     * @param target -
     * @param method -
     * @return -
     */
    private boolean isPrintLog(Object target, Method method) {
        // 方法上的注解优先级最高
        LogOpen methodLogOpen = method.getDeclaredAnnotation(LogOpen.class);
        if (methodLogOpen != null) {
            return methodLogOpen.open();
        }
        LogOpen classLogOpen = target.getClass().getDeclaredAnnotation(LogOpen.class);
        boolean classOpen = classLogOpen != null && classLogOpen.open();
        if (classOpen) {
            // 类注解时private方法不打印日志
            return Modifier.isPublic(method.getModifiers());
        } else {
            return false;
        }
    }


    /**
     * 生成方法日志的模板
     * 如果{@link LogOpen#value()} 未指定了日志前缀，则使用 {@param methodName}
     *
     * @param target     目标对象
     * @param method     目标方法
     * @param methodName 方法全名
     */
    private String generateTemplate(Object target, Method method, String methodName) {
        // 第一次调用创建日志模板
        LogOpen businessFlag = method.getDeclaredAnnotation(LogOpen.class);
        String logPrefix;
        if (businessFlag != null) {
            logPrefix = "".equals(businessFlag.value()) ? methodName : businessFlag.value();
        } else {
            logPrefix = methodName;
        }
        methodBusinessFlag.put(methodName, logPrefix);
        HandlerMethod handlerMethod = new HandlerMethod(target, method);
        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
        StringBuilder builder = new StringBuilder(logPrefix);
        // 开始/结束
        builder.append("{}.");
        for (MethodParameter methodParameter : methodParameters) {
            methodParameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
            builder.append(methodParameter.getParameterName())
                    .append(":")
                    .append("{}")
                    .append(",");
        }
        String template = builder.toString();
        template = template.substring(0, template.length() - 1);
        methodParameterTemplate.put(methodName, template);
        if (log.isDebugEnabled()) {
            log.debug("方法:{}, 模板:{}", methodName, template);
        }
        return template;
    }


    /**
     * 获取制定Logger
     */
    @SneakyThrows
    private Logger getLogger(Object target) {
        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            UseLogger useLogger = field.getDeclaredAnnotation(UseLogger.class);
            if (useLogger != null) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return (Logger) field.get(target);
            }
        }
        return LoggerFactory.getLogger(target.getClass());
    }


    /**
     * 将参数转化为String
     */
    private Object[] processArgs(Object[] args) {
        Object[] params = new Object[args.length + 1];
        for (int i = 0; i < args.length; i++) {
            Object object = args[i];
            if (object == null) {
                params[i + 1] = null;
            } else if (TYPES.contains(object.getClass().getTypeName())) {
                params[i + 1] = object;
            } else {
                params[i + 1] = JsonMapper.toJson(object);
            }
        }
        return params;
    }
}
