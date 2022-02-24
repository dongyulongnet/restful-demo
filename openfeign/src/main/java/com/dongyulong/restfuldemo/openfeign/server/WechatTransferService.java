package com.dongyulong.restfuldemo.openfeign.server;

import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.exception.NotFoundException;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import feign.ExceptionPropagationPolicy;
import feign.Feign;
import feign.Headers;
import feign.Logger;
import feign.Param;
import feign.QueryMap;
import feign.Request;
import feign.RequestLine;
import feign.Response;
import feign.Retryer;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.httpclient.ApacheHttpClient;
import feign.slf4j.Slf4jLogger;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author dongy
 * @date 12:55 2022/2/9
 **/
@Service
@PropertySource("classpath:/config.properties")
public class WechatTransferService {

    public Map<String, WechatTransferService.TransferService> ioc = new LinkedHashMap<>(16);

    @Value("wechat.privateKey")
    private String privateKey;
    @Value("wechat.mchId")
    private String mchId;
    @Value("wechat.serialNo")
    private String serialNo;
    @Value("wechat.apiV3Key")
    private String apiV3Key;

    public WechatTransferService() {
//        ioc.put(mchId, build(privateKey, mchId, serialNo, apiV3Key));
    }


    /**
     * @param accountType path params
     * @return -
     */
    public Map<String, Object> fundBalance(AccountType accountType) {
        TransferService transferService = ioc.get(mchId);
        return transferService.fundBalance(accountType.name());
    }

    /**
     * @param accountType path params
     * @param date        2019-08-17
     * @return -
     */
    public Map<String, Object> fundBalance(AccountType accountType, String date) {
        TransferService transferService = ioc.get(mchId);
        return transferService.fundBalance(accountType.name(), date);
    }

    /**
     * @param date 2019-08-17
     * @return -
     */
    public Map<String, Object> fundflowbill(String date) {
        TransferService transferService = ioc.get(mchId);
        FundFlowBill build = FundFlowBill.builder()
                .bill_date(date)
                .build();
        Map<String, Object> fundflowbill = transferService.fundflowbill(build);

        billdownload(String.valueOf(fundflowbill.get("download_url")), date);
        billdownloadAll(String.valueOf(fundflowbill.get("download_url")), date);
        return fundflowbill;
    }

    /**
     * @param startedDate 2019-08-17
     * @return -
     */
    public Map<String, Object> fundflowbillAll(String startedDate) {
        TransferService transferService = ioc.get(mchId);
        FundFlowBill build = FundFlowBill.builder()
                .bill_date(startedDate)
                .build();
        Map<String, Object> fundflowbill = transferService.fundflowbill(build);

        billdownloadAll(String.valueOf(fundflowbill.get("download_url")), startedDate);

        LocalDate localDate = LocalDate.parse(startedDate).plusDays(1);

        if (localDate.isBefore(LocalDate.of(2022, 02, 07))) {
            fundflowbillAll(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        return fundflowbill;
    }

    /**
     * @param date 2019-08-17
     * @return -
     */
    public Map<String, Object> fundtradebill(String date) {
        TransferService transferService = ioc.get(mchId);
        FundFlowBill build = FundFlowBill.builder()
                .bill_date(date)
                .build();
        return transferService.fundtradebill(build);
    }

    /**
     * @param token -
     * @return -
     */
    @SneakyThrows
    public Response billdownload(String token) {
        TransferService transferService = ioc.get(mchId);
        Verifier verifier = CERTIFICATES_MANAGER.getVerifier(mchId);
        String serialNumber = verifier.getValidCertificate().getSerialNumber().toString(16).toUpperCase(Locale.ROOT);
        return transferService.billdownload(token, serialNumber);
    }

    /**
     * @param url -
     * @return -
     */
    @SneakyThrows
    public void billdownload(String url, String date) {
        TransferService transferService = ioc.get(mchId);
        Verifier verifier = CERTIFICATES_MANAGER.getVerifier(mchId);
        String serialNumber = verifier.getValidCertificate().getSerialNumber().toString(16).toUpperCase(Locale.ROOT);

        Response response = transferService.billdownload(URI.create(url), serialNumber);
        try (OutputStream outStream = new FileOutputStream(String.format("%s.csv", date));
             InputStream inputStream = response.body().asInputStream()) {
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            outStream.write(buffer);
        }

    }

    /**
     * @param url -
     * @return -
     */
    @SneakyThrows
    public void billdownloadAll(String url, String date) {
        TransferService transferService = ioc.get(mchId);
        Verifier verifier = CERTIFICATES_MANAGER.getVerifier(mchId);
        String serialNumber = verifier.getValidCertificate().getSerialNumber().toString(16).toUpperCase(Locale.ROOT);

        Response response = transferService.billdownload(URI.create(url), serialNumber);
        Reader reader = response.body().asReader(StandardCharsets.UTF_8);
        CsvData csvData = CsvUtil.getReader().read(reader);
        List<CsvRow> rows = csvData.getRows();
        if (rows.size() < 3) {
            return;
        }
        List<CsvRow> csvRows = rows.subList(rows.size() - 1, rows.size());
        csvRows.get(0).add(1, date);
        CsvUtil.getWriter("d:/all.csv", StandardCharsets.UTF_8, Boolean.TRUE).write(csvRows).close();
        reader.close();
    }


    private static final CertificatesManager CERTIFICATES_MANAGER = CertificatesManager.getInstance();

    private WechatTransferService.TransferService build(String privateKey, String merchantId, String serialNo, String apiV3Key) {
        PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(new ByteArrayInputStream(privateKey.getBytes(StandardCharsets.UTF_8)));
        // 获取证书管理器实例
        Verifier verifier = getVerifier(privateKey, merchantId, serialNo, apiV3Key);
        WechatPayHttpClientBuilder wechatPayHttpClientBuilder = WechatPayHttpClientBuilder.create()
                .withMerchant(merchantId, serialNo, merchantPrivateKey)
//                .withValidator(new WechatPay2Validator(verifier))
                .withValidator(response -> true);
        HttpClient httpClient = wechatPayHttpClientBuilder.build();
        ApacheHttpClient apacheHttpClient = new ApacheHttpClient(httpClient);
        return Feign.builder()
                .logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .client(apacheHttpClient)
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .options(new Request.Options(3000L, TimeUnit.MILLISECONDS, 2000L, TimeUnit.MILLISECONDS, Boolean.FALSE))
                .exceptionPropagationPolicy(ExceptionPropagationPolicy.UNWRAP)
                .retryer(Retryer.NEVER_RETRY)
                .target(TransferService.class, WechatService.HOST);

    }

    @SneakyThrows
    private Verifier getVerifier(String privateKey, String merchantId, String serialNo, String apiV3Key) {
        synchronized (CERTIFICATES_MANAGER) {
            try {
                return CERTIFICATES_MANAGER.getVerifier(merchantId);
            } catch (NotFoundException e) {
                synchronized (CERTIFICATES_MANAGER) {
                    PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(new ByteArrayInputStream(privateKey.getBytes(StandardCharsets.UTF_8)));
                    // 向证书管理器增加需要自动更新平台证书的商户信息
                    CERTIFICATES_MANAGER.putMerchant(merchantId, new WechatPay2Credentials(merchantId, new PrivateKeySigner(serialNo, merchantPrivateKey)), apiV3Key.getBytes(StandardCharsets.UTF_8));
                    return CERTIFICATES_MANAGER.getVerifier(merchantId);
                }
            }
        }
    }

    public interface TransferService {

        /**
         * @param accountType path params
         * @return -
         */
        @RequestLine("GET /v3/merchant/fund/balance/{account_type}")
        Map<String, Object> fundBalance(@Param("account_type") String accountType);

        /**
         * @param accountType path params
         * @return -
         */
        @RequestLine("GET /v3/merchant/fund/dayendbalance/{account_type}?date={date}")
        Map<String, Object> fundBalance(@Param("account_type") String accountType, @Param("date") String date);

        /**
         * @param billDate path params
         * @return -
         */
        @RequestLine("GET /v3/bill/fundflowbill")
        Map<String, Object> fundflowbill(@QueryMap FundFlowBill billDate);

        /**
         * 获取账单下载链接和账单摘要
         *
         * @param billDate path params
         * @return -
         */
        @RequestLine("GET /v3/bill/tradebill")
        Map<String, Object> fundtradebill(@QueryMap FundFlowBill billDate);

        /**
         * @param token path params
         * @return -
         */
        @RequestLine("GET /v3/billdownload/file?token={token}")
        @Headers("Wechatpay-Serial:{wechatpaySerial}")
        Response billdownload(@Param("token") String token, @Param("wechatpaySerial") String wechatpaySerial);

        /**
         * @param wechatpaySerial path params
         * @return -
         */
        @RequestLine("GET ")
        @Headers("Wechatpay-Serial:{wechatpaySerial}")
        Response billdownload(URI host, @Param("wechatpaySerial") String wechatpaySerial);
    }

    @Data
    @Builder
    public static class FundFlowBill {

        private String bill_date;
    }

    /**
     * @author dongy
     * @date 13:23 2022/2/7
     **/
    public enum AccountType {
        /**
         * 基本账户
         */
        BASIC,
        /**
         * 运营账户
         */
        OPERATION,
        /**
         * 手续费账户
         */
        FEES
    }

}
