package com.dongyulong.restfuldemo.forrest.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.dongyulong.restfuldemo.forrest.entities.WechatServiceQueryRequestDTO;
import com.dongyulong.restfuldemo.forrest.server.WechatService;
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
public class ForestController {

    @Resource
    private WechatService wechatService;


    @PostMapping("/forest")
    public Map<String, Object> forest(@RequestBody Map<String, Object> body) {
        WechatServiceQueryRequestDTO wechatServiceQueryRequestDTO = BeanUtil.mapToBean(body, WechatServiceQueryRequestDTO.class, Boolean.FALSE, CopyOptions.create());
        return wechatService.query(wechatServiceQueryRequestDTO);
    }
}
