package com.dongyulong.dogn.common.tookit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jary on 2017/6/20.
 */
public class JsonMapper {
    private static final char UNDERLINE = '_';
    private static Logger logger = LoggerFactory.getLogger(JsonMapper.class);
    public static final Gson GSON = new Gson();

    public static final int UNDERLINE_TO_CAMEL_CASE = 0;
    public static final int CAMEL_CASE_TO_UNDERLINE = 1;
    public static Pattern linePattern = Pattern.compile("_(\\w)");
    public static Pattern camelPattern = Pattern.compile("[A-Z]");

    public static String toJson(Object obj) {
        String jsonStr = "";
        try {
            jsonStr = JSON.toJSONString(obj);
        } catch (Exception e) {
            logger.error("toJson failed.", e);
        }
        return jsonStr;
    }

    public static <T> T json2Bean(String jsonString, Class<T> cls) {
        T t = null;
        try {
            t = JSON.parseObject(jsonString, cls);
        } catch (JSONException e) {
            logger.debug("try again with gson. json:" + jsonString);
            t = GSON.fromJson(jsonString, cls);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("json2Bean failed." + jsonString, e);
        }
        return t;
    }

    public static <T> T json2Bean(String jsonString, Type clzz) {
        T t = null;
        try {
            t = JSON.parseObject(jsonString, clzz);
        } catch (JSONException e) {
            logger.debug("try again with gson. json:" + jsonString);
            t = GSON.fromJson(jsonString, clzz);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("json2Bean failed." + jsonString, e);
        }
        return t;
    }

    public static <T> List<T> json2List(String jsonString, Class<T> cls) {
        List<T> list = null;
        try {
            list = JSON.parseArray(jsonString, cls);
        } catch (Exception e) {
            logger.error("json2List failed." + jsonString, e);
        }
        return list;
    }

    public static Map<String, Object> json2Map(String jsonString) {
        Map<String, Object> map = null;
        try {
            map = JSON.parseObject(jsonString, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            logger.error("json2Map failed." + jsonString, e);
        }
        return map;
    }

    public static String toJson(Object obj, int type) {
        String str = toJson(obj);
        StringBuffer sb = new StringBuffer();
        if (type == UNDERLINE_TO_CAMEL_CASE) {
            Matcher matcher = linePattern.matcher(str);
            while (matcher.find()) {
                matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
            }
            matcher.appendTail(sb);
        } else {
            Matcher matcher = camelPattern.matcher(str);
            while (matcher.find()) {
                matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
            }
            matcher.appendTail(sb);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        /*UserEntity userEntity = new UserEntity();
        userEntity.setCid("12312323");
        userEntity.setDevicetype(1);
        String jsonb = toJson(userEntity);
        System.out.println(jsonb);
        UserEntity ue = json2Bean(jsonb, UserEntity.class);
        System.out.println("----------");
        List<UserEntity> userEntitys = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            UserEntity userEntity1 = new UserEntity();
            userEntity1.setName(i + "");
            userEntity1.setCid("12312323");
            userEntity1.setDevicetype(1);
            userEntitys.add(userEntity1);
        }
        String jsonL = toJson(userEntitys);
        System.out.println(jsonL);
        List<UserEntity> ues = json2List(jsonL, UserEntity.class);
        System.out.println("----------");
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            map.put(i + "", i + 10);
        }
        String jsonM = toJson(map);
        System.out.println(jsonM);
        Map<String, Object> m = json2Map(jsonM);*/

    }
}

