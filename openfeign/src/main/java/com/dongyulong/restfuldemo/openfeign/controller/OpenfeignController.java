package com.dongyulong.restfuldemo.openfeign.controller;

import cn.hutool.core.bean.BeanUtil;
import com.didapinche.agaue.common.utils.JsonTools;
import com.dongyulong.restfuldemo.openfeign.entities.WechatServiceQueryRequestDTO;
import com.dongyulong.restfuldemo.openfeign.server.WechatService;
import com.dongyulong.restfuldemo.openfeign.server.WechatTransferService;
import feign.Response;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author dongy
 * @date 13:17 2022/1/26
 **/
@Slf4j
@RestController
public class OpenfeignController {

    @Autowired
    private WechatService wechatService;
    @Autowired
    private WechatTransferService wechatTransferService;

    /**
     * 解析请求参数（只能解析出来表单参数或url参数）
     *
     * @param request -
     * @return -
     */
    public Map<String, Object> getParameterMap(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (entry) -> Arrays.stream(entry.getValue()).findFirst().orElse(StringUtils.EMPTY)));
    }

    /**
     * 解析请求参数（只能解析出来表单参数或url参数）
     *
     * @param request -
     * @return -
     */
    public Map<String, Object> getParameter(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (entry) -> Arrays.stream(entry.getValue()).findFirst().orElse(StringUtils.EMPTY)));
    }

    @SneakyThrows
    public static <T> T mapToBean(Map<String, Object> map, Class<T> tClass) {
        //因为不支持我们在前面封装的那个转换类所以在这个这个位置自己注册一个新的转换类，用来把string to date
        ConvertUtils.register(new Converter() {
            @SneakyThrows
            @Override
            public <K> K convert(Class<K> aClass, Object val) {
                if (null == val) {
                    return null;
                }
                if (!(val instanceof String)) {
                    throw new ConversionException("只支持字符串转换");
                }
                String value = (String) val;
                if (StringUtils.isBlank(value.trim())) {
                    return null;
                }
                SimpleDateFormat format;
                if (value.length() > 10) {
                    format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                } else {
                    format = new SimpleDateFormat("yyyy-MM-dd");
                }
                return (K) format.parse(value);
            }
        }, Date.class);

        T bean = tClass.newInstance();
        BeanUtils.populate(bean, map);
        return bean;
    }

    /**
     * 获取body参数
     *
     * @param request -
     * @return -
     */
    public String getBodyParameter(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = request.getReader();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            log.error("getBodyParameter. 获取请求内容异常", e);
            return null;
        }
        return sb.toString();
    }

    @PostMapping("/test-params")
    public void testParams(HttpServletRequest request, HttpServletResponse response) {
        String bodyParameter = getBodyParameter(request);
        log.info("bodyParameter ：{}", bodyParameter);

        Map<String, Object> parameterMap = getParameterMap(request);

        log.info("parameterMap ：{}", JsonTools.toJSON(parameterMap));
        BeanPoJo beanPoJo = OpenfeignController.mapToBean(parameterMap, BeanPoJo.class);
        log.info("beanPoJo ：{}", JsonTools.toJSON(beanPoJo));

    }

    @Data
    public static class BeanPoJo {

        private Date time;

        private String name;

        private Integer age;

        private String addr;
    }


    @PostMapping("/forest")
    public Map<String, Object> forest(@RequestBody Map<String, Object> body) {
        WechatServiceQueryRequestDTO wechatServiceQueryRequestDTO = BeanUtil.copyProperties(body, WechatServiceQueryRequestDTO.class);
        wechatServiceQueryRequestDTO.sign(body.get("key").toString());
        return wechatService.query(wechatServiceQueryRequestDTO);
    }

    @PostMapping("/bill-receipt")
    public Map<String, Object> billReceipt(String accountType) {
        return wechatTransferService.fundBalance(WechatTransferService.AccountType.valueOf(accountType));
    }

    @PostMapping("/bill-receipt/{date}")
    public Map<String, Object> billReceipt(String accountType, @PathVariable("date") String date) {
        return wechatTransferService.fundBalance(WechatTransferService.AccountType.valueOf(accountType), date);
    }

    @PostMapping("/fundflowbill/{date}")
    public Map<String, Object> fundflowbill(@PathVariable("date") String date) {
        return wechatTransferService.fundflowbill(date);
    }

    @PostMapping("/fundflowbillall/{date}")
    public Map<String, Object> fundflowbillAll(@PathVariable("date") String date) {
        return wechatTransferService.fundflowbillAll(date);
    }

    @PostMapping("/fundtradebill/{date}")
    public Map<String, Object> fundtradebill(@PathVariable("date") String date) {
        return wechatTransferService.fundtradebill(date);
    }

    @SneakyThrows
    @PostMapping("/billdownload")
    public void billdownload(String token) {
        Response response = wechatTransferService.billdownload(token);
        InputStream inputStream = response.body().asInputStream();
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);

        File targetFile = new File("targetFile.tmp");
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
    }
}
