package com.phemex.utils;

import com.google.common.io.BaseEncoding;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionEncoder {
    private static final Logger log = LoggerFactory.getLogger(ActionEncoder.class);
    public static ActionEncoder instance = getInstance();
    private final byte[] key;

    public static ActionEncoder getInstance() {
        return new ActionEncoder();
    }

    public ActionEncoder() {
        this((String)null);
    }

    public ActionEncoder(String key) {
        this.key = StringUtils.isEmpty(key) ? "]~WKEM^Z O}'CP8:+s|v'v$Xa1 =4fV36".getBytes(StandardCharsets.UTF_8) : key.getBytes(StandardCharsets.UTF_8);
    }

    <T> byte[] encode2Bytes(T data, long expire) {
        byte[] payload = JsonStringConverter.instance().toBytes(data);
        byte[] hash = HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_256, this.key).doFinal(payload);
        log.info("hash len is [{}]", hash.length);
        boolean hasExpire = expire > 0L;
        int len = hasExpire ? 1 + hash.length + payload.length + 8 : 1 + hash.length + payload.length;
        byte[] ans = new byte[len];
        ans[0] = (byte)(hasExpire ? 50 : 49);
        System.arraycopy(hash, 0, ans, 1, hash.length);
        System.arraycopy(payload, 0, ans, 1 + hash.length, payload.length);
        if (hasExpire) {
            byte[] b = BytesUtils.long2LittleEndian(expire);
            System.arraycopy(b, 0, ans, len - 8, 8);
        }

        return ans;
    }

    <T> T byte2Obj(byte[] bytes, Class<T> clazz) {
        byte v = bytes[0];
        boolean hasExpire = false;
        if (v == 50) {
            hasExpire = true;
        } else {
            if (v != 49) {
                log.info("version is not correct [{}]", v);
                return null;
            }

            hasExpire = false;
        }

        int payloadLen = bytes.length - 33;
        byte[] expireB;
        if (hasExpire) {
            expireB = new byte[8];
            System.arraycopy(bytes, bytes.length - 8, expireB, 0, 8);
            if (BytesUtils.littleEndian2Long(expireB) < System.currentTimeMillis()) {
                log.warn("code expired");
                return null;
            }

            payloadLen -= 8;
        }

        expireB = new byte[32];
        byte[] payload = new byte[payloadLen];
        System.arraycopy(bytes, 1, expireB, 0, 32);
        System.arraycopy(bytes, 33, payload, 0, payloadLen);
        byte[] expectedHash = HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_256, this.key).doFinal(payload);
        return !Arrays.equals(expectedHash, expireB) ? null : JsonStringConverter.instance().from(payload, clazz);
    }

    public <T> String encode(T data) {
        return Hex.encodeHexString(this.encode2Bytes(data, 0L));
    }

    public <T> String encode(T data, long expire) {
        return Hex.encodeHexString(this.encode2Bytes(data, expire));
    }

    public <T> String base64Encode(T data, long expire) {
        return Base64.getEncoder().encodeToString(this.encode2Bytes(data, expire));
    }

    public <T> String base64Encode(T data) {
        return this.base64Encode(data, 0L);
    }

    public <T> String base32Encode(T data, long expire) {
        return BaseEncoding.base32().omitPadding().encode(this.encode2Bytes(data, expire));
    }

    public <T> String base32Encode(T data) {
        return this.base32Encode(data, 0L);
    }

    public <T> T base32Decode(String hex, Class<T> clazz) throws DecoderException {
        if (StringUtils.isEmpty(hex)) {
            return null;
        } else {
            byte[] decodeBytes = BaseEncoding.base32().omitPadding().decode(hex);
            return this.byte2Obj(decodeBytes, clazz);
        }
    }

    public <T> T base64Decode(String hex, Class<T> clazz) throws DecoderException {
        if (StringUtils.isEmpty(hex)) {
            return null;
        } else {
            byte[] decodeBytes = Base64.getDecoder().decode(hex);
            return this.byte2Obj(decodeBytes, clazz);
        }
    }

    public <T> T decode(String hex, Class<T> clazz) {
        if (StringUtils.isEmpty(hex)) {
            throw new RuntimeException("verify token is empty ");
        } else {
            byte[] hexArr = new byte[0];

            try {
                hexArr = Hex.decodeHex(hex);
            } catch (DecoderException var5) {
                throw new RuntimeException("Failed to decode verify code " + hex);
            }

            return this.byte2Obj(hexArr, clazz);
        }
    }
}