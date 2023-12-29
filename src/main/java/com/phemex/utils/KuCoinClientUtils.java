package com.phemex.utils;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import org.apache.commons.codec.binary.Base64;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.HashMap;
import java.util.Random;

import static io.restassured.RestAssured.given;

public class KuCoinClientUtils {

    /**
     * HMacSha256( URL Path + QueryString + Expiry + body )
     */
    public static String sign(String timestamp, String method, String endpoint, String body, String secretKey) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(timestamp)
                .append(method)
                .append(endpoint)
                .append((body == null ? "" : body));
        String signedStr = sb.toString();
        String signature = sign(signedStr, secretKey);
        return signature;
    }

    private static String sign(String message, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);
            String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(message.getBytes()));
            return hash;
        } catch (Exception e) {
            throw new RuntimeException("Unable to sign message, error: " + e.getMessage(), e);
        }
    }


    public static String expiry() {
        Clock clock = Clock.systemDefaultZone();
        long expiry = clock.millis();
//        long expiry = 1640793600L;
        return String.valueOf(expiry);
    }

    @Test
    void testBuyCrypto() {
        String method = "POST";
        String endpoint = "/api/v1/orders";
        String timestamp = KuCoinClientUtils.expiry();
        String body = "{\"clientOid\":\"00047013-2a96-4305-9722-77b2e0f7cb86\",\"side\":\"buy\",\"type\":\"market\",\"price\":\"0.2\",\"size\":\"100\",\"symbol\":\"GARI-USDT\",\"tradeType\":\"TRADE\"}";
        String secretKey = "5ac1b01d-5b7d-4a2f-84e8-16ce1d3508da";
        String ApiKey = "61e64d8ddefeaa0001d51946";
        System.out.println(KuCoinClientUtils.sign(timestamp, method,endpoint, body, secretKey));

        HashMap<String, String> headers = new HashMap<>();
        headers.put("KC-API-KEY", ApiKey);
        headers.put("KC-API-SIGN", KuCoinClientUtils.sign(timestamp, method,endpoint, body, secretKey));
        headers.put("KC-API-TIMESTAMP", timestamp);
        headers.put("KC-API-PASSPHRASE", "Shiyu@1234");
        headers.put("Content-Type", "application/json");
//        headers.put("KC-API-KEY-VERSION", "1");
        given().
                body(body).
                headers(headers).
                when().
                log().all().
                post("https://api.kucoin.com/api/v1/orders").
                then().log().body();
    }


    public static void main(String[] args) {
        System.out.println(KuCoinClientUtils.expiry());
        String path = "/spot/orders";
        String queryString = "";
        String body = "{\"actionBy\":\"FromOrderPlacement\",\"symbol\":\"sVPADUSDT\",\"clOrdID\":\"" + System.currentTimeMillis() + "-d88c-5742" + new Random().nextInt() + "\",\"side\":\"Buy\",\"priceEp\":3500000,\"ordType\":\"Market\",\"qtyType\":\"ByQuote\",\"triggerType\":\"UNSPECIFIED\",\"pegPriceType\":\"UNSPECIFIED\",\"quoteQtyEv\":10000000000,\"timeInForce\":\"ImmediateOrCancel\"} ";
        String secretKey = "ENCSgP0TZBxPbP3lEr5weY5tMttRtVpJH_rV49OvcJBjOWMxNWI0OS01MmE5LTRkMjYtOWUxZi0xMThmNTllNDRjMDA";

    }

}
