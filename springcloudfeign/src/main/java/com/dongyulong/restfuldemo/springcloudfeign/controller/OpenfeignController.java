package com.dongyulong.restfuldemo.springcloudfeign.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.dongyulong.restfuldemo.springcloudfeign.entities.WechatServiceQueryRequestDTO;
import com.dongyulong.restfuldemo.springcloudfeign.server.WechatService;
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

    @Resource
    private WechatService wechatService;


    @PostMapping("/forest")
    public Map<String, Object> forest(@RequestBody Map<String, Object> body) {
        WechatServiceQueryRequestDTO wechatServiceQueryRequestDTO = BeanUtil.mapToBean(body, WechatServiceQueryRequestDTO.class, Boolean.FALSE, CopyOptions.create());
        wechatServiceQueryRequestDTO.sign();


        return wechatService.query(wechatServiceQueryRequestDTO);
    }
}
