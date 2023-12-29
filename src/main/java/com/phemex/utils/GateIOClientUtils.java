package com.phemex.utils;

/**
 * @author: yuyu.shi
 * @Project: phemex
 * @Package: com.phemex.utils.GateIOClientUtils
 * @Date: 2022年07月10日 21:42
 * @Description:
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import static io.restassured.RestAssured.given;

public class GateIOClientUtils {
    /**
     * HMacSha256( URL Path + QueryString + Expiry + body )
     * HexEncode(HMAC_SHA512(secret, signature_string))
     * Request Method + "\n" + Request URL + "\n"
     * + Query String + "\n" + HexEncode(SHA512(Request Payload)) + "\n" + Timestamp
     */


    @Test
    void testBuyCrypto() {
        String apiKey = "e84bf42acc9ec011a7b7c9aebc39eac4";
        String apiSecret = "2ef7163a17629c8b04688d3c76bb210cf1726bc09e69c94340933ccb4da898ac";

        String host = "https://api.gateio.ws";
        String method = "POST";
        String prefix = "/api/v4";
        String url = "/spot/orders";
        String ts = String.valueOf(System.currentTimeMillis() / 1000L);
        String bodyString = "{\"currencyPair\":\"MART_USDT\",\"type\":\"limit\",\"account\":\"spot\",\"side\":\"buy\",\"amount\":\"100\",\"price\":\"0.05\",\"timeInForce\":\"gtc\"}";
        String queryString = "";

        String signature = sign(apiSecret, method, prefix + url, ts, bodyString, queryString);

        JSONObject jsonObj = (JSONObject) JSON.parse(bodyString);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("KEY", apiKey);
        headers.put("Timestamp", ts);
        headers.put("SIGN", signature);
//        headers.put("x-gate-api-v4", "true");
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        given().
                body(jsonObj).
                headers(headers).
                when().
                log().all().
                post(host + prefix + url).
                then().log().body();
    }


    public static void main(String[] args) {
        String host = "https://api.gateio.ws";
        String common_headers = "{'Accept': 'application/json', 'Content-Type': 'application/json'}";
        String apiKey = "e84bf42acc9ec011a7b7c9aebc39eac4";
        String apiSecret = "2ef7163a17629c8b04688d3c76bb210cf1726bc09e69c94340933ccb4da898ac";

        String prefix = "/api/v4";
        String url = "/spot/orders";
        String method = "POST";

        String ts = String.valueOf(System.currentTimeMillis() / 1000L);
        String bodyString = "{\"currencyPair\":\"MART_USDT\",\"type\":\"limit\",\"account\":\"spot\",\"side\":\"buy\",\"amount\":\"100\",\"price\":\"0.05\",\"timeInForce\":\"gtc\"}";
        String queryString = "";

        System.out.println(sign(apiSecret, method, prefix + url, ts, bodyString, queryString));


    }

    private static String sign(String apiSecret, String method, String url, String ts, String bodyString, String queryString) {
        String signatureString = String.format("%s\n%s\n%s\n%s\n%s", method, url, queryString, DigestUtils.sha512Hex(bodyString), ts);
        System.out.println(signatureString);
        String signature;
        try {
            Mac hmacSha512 = Mac.getInstance("HmacSHA512");
            final byte[] bytesSecret = apiSecret.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec spec = new SecretKeySpec(bytesSecret, "HmacSHA512");
            hmacSha512.init(spec);
            final byte[] bytesData = signatureString.getBytes(StandardCharsets.UTF_8);
            signature = Hex.encodeHexString(hmacSha512.doFinal(bytesData));
            return signature;
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
