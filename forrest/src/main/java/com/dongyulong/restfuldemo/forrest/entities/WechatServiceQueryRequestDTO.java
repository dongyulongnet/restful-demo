package com.dongyulong.restfuldemo.forrest.entities;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.dongyulong.restfuldemo.forrest.config.Required;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * @author dongy
 * @date 11:28 2022/1/26
 **/
@Data
public class WechatServiceQueryRequestDTO {

    /**
     * 公众账号ID
     * eg:wxd678efh567hg6787
     * 微信支付分配的公众账号ID（企业号corpid即为此appid）
     */
    @Required(max = 32)
    private String appid;
    /**
     * 商户号
     * eg:1230000109
     * 微信支付分配的商户号
     */
    @Required(max = 32)
    private String mch_id;
    /**
     * 微信订单号
     * eg:4200001304202201266815935409
     * 微信的订单号，建议优先使用
     */
    @Required(max = 32)
    private String transaction_id;
    /**
     * 商户订单号
     * eg:btt-968863601915854850
     * 商户系统内部订单号，要求32个字符内（最少6个字符），只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号
     */
    @Required(must = false, max = 32, params = "transaction_id")
    private String out_trade_no;
    /**
     * 随机字符串
     * 随机字符串，不长于32位。推荐随机数生成算法
     * eg:C380BEC2BFD727A4B6845133519F3AD6
     */
    @Required(max = 32)
    private String nonce_str;

    /**
     * 签名
     * 通过签名算法计算得出的签名值，详见签名生成算法
     * eg:C380BEC2BFD727A4B6845133519F3AD6
     */
    @Required(max = 32)
    private String sign;

    /**
     *
     */
    private String key;

    public void sign() {
        Map<String, Object> targetMap = new TreeMap<>(String::compareTo);
        BeanUtil.beanToMap(this, targetMap, CopyOptions.create().ignoreNullValue().setIgnoreProperties("sign"));
        String collect = targetMap.entrySet().stream().map(this::joinString).collect(Collectors.joining("&"));
        sign = DigestUtils.md5Hex(String.format("%s&key=%s",collect,this.key).getBytes(StandardCharsets.UTF_8)).toUpperCase();
    }

    private String joinString(Map.Entry<String, Object> entry){
        if (entry == null){
            return "";
        }
        return String.format("%s=%s",entry.getKey(),entry.getValue());
    }
}
