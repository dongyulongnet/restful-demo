package com.dongyulong.restfuldemo.openfeign.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import com.dongyulong.restfuldemo.openfeign.entities.WechatServiceQueryRequestDTO;
import com.dongyulong.restfuldemo.openfeign.server.WechatService;
import com.dongyulong.restfuldemo.openfeign.server.WechatTransferService;
import feign.Response;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author dongy
 * @date 13:17 2022/1/26
 **/
@RestController
public class OpenfeignController {

    @Autowired
    private WechatService wechatService;
    @Autowired
    private WechatTransferService wechatTransferService;


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
