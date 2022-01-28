package com.dongyulong.restfuldemo.openfeign.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.dongyulong.restfuldemo.openfeign.entities.WechatServiceQueryRequestDTO;
import com.dongyulong.restfuldemo.openfeign.server.WechatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author dongy
 * @date 13:17 2022/1/26
 **/
@RestController
public class OpenfeignController {

    @Autowired
    private WechatService wechatService;


    @PostMapping("/forest")
    public Map<String, Object> forest(@RequestBody Map<String, Object> body) {
        WechatServiceQueryRequestDTO wechatServiceQueryRequestDTO = BeanUtil.copyProperties(body, WechatServiceQueryRequestDTO.class);
        wechatServiceQueryRequestDTO.sign(body.get("key").toString());
        return wechatService.query(wechatServiceQueryRequestDTO);
    }
}
