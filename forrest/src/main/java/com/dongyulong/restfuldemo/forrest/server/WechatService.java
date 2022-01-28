package com.dongyulong.restfuldemo.forrest.server;

import com.dongyulong.restfuldemo.forrest.entities.WechatServiceQueryRequestDTO;
import com.dtflys.forest.annotation.Address;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.annotation.XMLBody;

import java.util.Map;

/**
 * @author dongy
 * @date 11:25 2022/1/26
 **/
@Address(host = "https://api.mch.weixin.qq.com")
public interface WechatService {

    /**
     * 
     * @param queryRequest
     * @return
     */
    @Post(url = "/pay/orderquery")
    Map<String, Object> query(@XMLBody WechatServiceQueryRequestDTO queryRequest);

}
