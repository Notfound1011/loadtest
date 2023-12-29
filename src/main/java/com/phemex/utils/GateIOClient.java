package com.phemex.utils;

/**
 * @author: yuyu.shi
 * @Project: phemex
 * @Package: com.phemex.utils.GateIOClientUtils
 * @Date: 2022年07月10日 21:42
 * @Description:
 */

import io.gate.gateapi.ApiClient;
import io.gate.gateapi.ApiException;
import io.gate.gateapi.Configuration;
import io.gate.gateapi.GateApiException;
import io.gate.gateapi.api.SpotApi;
import io.gate.gateapi.models.Order;

public class GateIOClient {
    public static void main(String[] args) {
        ApiClient defaultClient = getApiClient("e84bf42acc9ec011a7b7c9aebc39eac4",
                "2ef7163a17629c8b04688d3c76bb210cf1726bc09e69c94340933ccb4da898ac");
        spotCreateOrder(Order.SideEnum.BUY, Order.TypeEnum.LIMIT, "BTC_USDT",
                "100", "0.05", defaultClient);
    }

    public static void spotCreateOrder(Order.SideEnum side, Order.TypeEnum type, String currency,
                                        String amount, String price, ApiClient defaultClient) {
        // 现货api
        SpotApi apiInstance = new SpotApi(defaultClient);

        Order order = new Order(); // Order | 下单参数
        order.setAccount(Order.AccountEnum.SPOT);
        order.setSide(side);
        order.setType(type);
        order.setAmount(amount);
        order.setPrice(price);
        order.setCurrencyPair(currency);
        System.out.println(order);

        try {
            // 创建现货订单
            Order result = apiInstance.createOrder(order);
            System.out.println(result);
        } catch (GateApiException e) {
//            System.err.println(String.format("Gate api exception, label: %s, message: %s", e.getErrorLabel(), e.getMessage()));
            e.printStackTrace();
        } catch (ApiException e) {
//            System.err.println("Exception when calling SpotApi#createOrder");
//            System.err.println("Status code: " + e.getCode());
//            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }

    public static ApiClient getApiClient(String key, String secret) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api.gateio.ws/api/v4");
        defaultClient.setApiKeySecret(key, secret);
        return defaultClient;
    }
}
