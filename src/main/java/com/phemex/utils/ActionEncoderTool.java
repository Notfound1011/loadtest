package com.phemex.utils;

import java.net.URL;
import java.net.URLClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionEncoderTool {
    private static final Logger log = LoggerFactory.getLogger(ActionEncoderTool.class);

    public ActionEncoderTool() {
    }

    public static String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) {
        log.info("args {}", args);
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader)cl).getURLs();
        URL[] var3 = urls;
        int var4 = urls.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            URL url = var3[var5];
            System.out.println(url.getFile());
        }

        String loadTestValue = genTestValue(args.length > 0 ? args[0] : "dummy_test_str", args.length > 1 ? Integer.parseInt(args[1]) : 30, args.length > 2 ? args[2] : "");
        log.info("load test value is {}", loadTestValue);
        System.out.println(ActionEncoderTool.loadTestValue("5ef9cfe9-ca67-412c7-9b9c-54a2b49ba35",30));
    }

    public static String loadTestValue(String key, int ttlMinutes) {
        return ActionEncoder.getInstance().base64Encode(key, System.currentTimeMillis() + (long)('\uea60' * ttlMinutes));
    }

    public static String genTestValue(String key, int ttlMinutes, String token) {
        ActionEncoder actionEncoder = new ActionEncoder(token);
        return actionEncoder.base64Encode(key, System.currentTimeMillis() + (long)('\uea60' * ttlMinutes));
    }
}
