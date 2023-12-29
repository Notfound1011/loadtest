package com.phemex.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.restassured.response.Response;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.*;

import static io.restassured.RestAssured.*;

public class ClientUtils {

    /**
     * HMacSha256( URL Path + QueryString + Expiry + body )
     */
    public static String sign(String path, String queryString, long expiry, String body, byte[] secretKey) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(path)
                .append(queryString == null ? "" : queryString)
                .append(expiry)
                .append((body == null ? "" : body));
        String signedStr = sb.toString();
        String signature = sign(signedStr, secretKey);
        return signature;
    }

    public static String sign(String path, String queryString, String body, byte[] secretKey) {
        StringBuilder sb = new StringBuilder(1024);
        Clock clock = Clock.systemDefaultZone();
        String expiry = expiry();
//        String expiry= "1640793600";
        sb.append(path)
                .append(queryString == null ? "" : queryString)
                .append(expiry)
                .append((body == null ? "" : body));
        String signedStr = sb.toString();
//        System.out.println(signedStr);
        String signature = sign(signedStr, secretKey);
        return signature;
    }

    private static String sign(String message, byte[] secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);
            return new String(Hex.encodeHex(sha256_HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            throw new RuntimeException("Unable to sign message, error: " + e.getMessage(), e);
        }
    }


    public static String expiry() {
        Clock clock = Clock.systemDefaultZone();
        long expiry = clock.millis() * 1 / 1000 + 60L;
//        long expiry = 1640793600L;
        return String.valueOf(expiry);
    }

    @Test
    void testBuyCrypto() {
        String path = "/spot/orders";
        String queryString = "";
        String expiry = ClientUtils.expiry();
        String body = "{\"actionBy\":\"FromOrderPlacement\",\"symbol\":\"sVPADUSDT\",\"clOrdID\":\"" + UUID.randomUUID() + "\",\"side\":\"Buy\",\"priceEp\":3500000,\"ordType\":\"Market\",\"qtyType\":\"ByQuote\",\"triggerType\":\"UNSPECIFIED\",\"pegPriceType\":\"UNSPECIFIED\",\"quoteQtyEv\":10000000000,\"timeInForce\":\"ImmediateOrCancel\"} ";
        String secretKey = "";
        System.out.println(ClientUtils.sign(path, queryString, body, secretKey.getBytes()));

        HashMap<String, String> headers = new HashMap<>();
        headers.put("x-phemex-request-expiry", expiry);
        headers.put("x-phemex-request-signature", ClientUtils.sign(path, queryString, body, secretKey.getBytes()));
        headers.put("x-phemex-access-token", "");
        headers.put("Content-Type", "application/json");
        given().
                body(body).
                headers(headers).
                when().
                log().all().
                post("https://api10.phemex.com/spot/orders").
                then().log().body();
    }


    public static void main(String[] args) {
        System.out.println(ClientUtils.expiry());
        String path = "/spot/orders";
        String queryString = "";
        String body = "{\"actionBy\":\"FromOrderPlacement\",\"symbol\":\"sVPADUSDT\",\"clOrdID\":\"" + System.currentTimeMillis() + "-d88c-5742" + new Random().nextInt() + "\",\"side\":\"Buy\",\"priceEp\":3500000,\"ordType\":\"Market\",\"qtyType\":\"ByQuote\",\"triggerType\":\"UNSPECIFIED\",\"pegPriceType\":\"UNSPECIFIED\",\"quoteQtyEv\":10000000000,\"timeInForce\":\"ImmediateOrCancel\"} ";
        String secretKey = "ENCSgP0TZBxPbP3lEr5weY5tMttRtVpJH_rV49OvcJBjOWMxNWI0OS01MmE5LTRkMjYtOWUxZi0xMThmNTllNDRjMDA";
        System.out.println(ClientUtils.sign(path, queryString, body, secretKey.getBytes()));
        System.out.println(body);

//        String[] nums = new String[] {"100","150","200","250","300","350","400","450","500","550"};
//        Random random =  new Random();
//        int i = random.nextInt(nums.length);
//        System.out.println(nums[i]);
        int a = 0;
        Random b = new Random();
        a = b.nextInt(10);
        System.out.println(a);
    }

}
