package com.phemex.utils;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.*;

import static io.restassured.RestAssured.given;

public class MEXCClientUtils {

    /**
     * 获取get请求参数字符串
     *
     * @param param get/delete请求参数map
     * @return
     */
    public static String getRequestParamString(Map<String, String> param) {
        if (MapUtils.isEmpty(param)) {
            return "";
        }
        StringBuilder sb = new StringBuilder(1024);
        SortedMap<String, String> map = new TreeMap<>(param);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = StringUtils.isBlank(entry.getValue()) ? "" : entry.getValue();
            sb.append(key).append('=').append(urlEncode(value)).append('&');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 encoding not supported!");
        }
    }

    /**
     * 签名
     */
    public static String sign(SignVo signVo) {
        if (signVo.getRequestParam() == null) {
            signVo.setRequestParam("");
        }
        String str = signVo.getAccessKey() + signVo.getReqTime() + signVo.getRequestParam();
        return actualSignature(str, signVo.getSecretKey());
    }

    public static String actualSignature(String inputStr, String key) {
        Mac hmacSha256;
        try {
            hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No such algorithm: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key: " + e.getMessage());
        }
        byte[] hash = hmacSha256.doFinal(inputStr.getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(hash);
    }

    @Getter
    @Setter
    public static class SignVo {
        private String reqTime;
        private String accessKey;
        private String secretKey;
        private String requestParam; //get请求参数根据字典顺序排序,使用&拼接字符串,post为json字符串
    }

    public static String reqTime() {
        Clock clock = Clock.systemDefaultZone();
        long reqTime = clock.millis();
        return String.valueOf(reqTime);
    }

    @Test
    void testBuyCrypto() {
        String accessKey = "mx0BtVSsC9TWh4MPES";
        String secretKey = "98ae4cd7ed1f4768bfad262db316003c";
        String body = "{\"order_type\":\"LIMIT_ORDER\",\"price\":\"0.00846945\",\"quantity\":\"1000\",\"symbol\":\"RACA_USDT\",\"trade_type\":\"BID\"}";
        String reqTime = MEXCClientUtils.reqTime();

//        SignVo signVo = new SignVo();
//        signVo.setReqTime(reqTime);
//        signVo.setAccessKey(accessKey);
//        signVo.setSecretKey(secretKey);
//        signVo.setRequestParam(body);
//        String signature = MEXCClientUtils.sign(signVo);

        String str = accessKey + reqTime + body;
        String signature = MEXCClientUtils.actualSignature(str, secretKey);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("ApiKey", accessKey);
        headers.put("Signature", signature);
        headers.put("Request-Time", reqTime);
        headers.put("Content-Type", "application/json");
        given().
                body(body).
                headers(headers).
                when().
                log().all().
                post("https://www.mexc.com/open/api/v2/order/place").
                then().log().body();
    }

    public static void main(String[] args) {
        SignVo signVo = new SignVo();
        Clock clock = Clock.systemDefaultZone();
        long reqTime = clock.millis() / 1000;
        signVo.setReqTime(String.valueOf(reqTime));
        signVo.setAccessKey("aaa");
        signVo.setSecretKey("bbb");

        System.out.println(MEXCClientUtils.sign(signVo));
    }

}
