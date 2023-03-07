package com.raiser.test.client;

import com.raiser.rpc.client.RpcClient;
import com.raiser.rpc.client.handler.RpcFuture;
import com.raiser.rpc.client.proxy.RpcService;
import com.raiser.test.service.HelloService;

import java.util.concurrent.TimeUnit;

/**
 * @author: zhengyangxin
 * @date: 9/5/2022 8:38 PM
 */
public class RpcAsyncTest {
    public static void main(String[] args) throws InterruptedException {
        final RpcClient rpcClient = new RpcClient("127.0.0.1:2181");

        int threadNum = 1;
        final int requestNum = 100;
        Thread[] threads = new Thread[threadNum];

        long startTime = System.currentTimeMillis();
        //benchmark for async call
        for (int i = 0; i < threadNum; ++i) {
            threads[i] = new Thread(() -> {
                for (int i1 = 0; i1 < requestNum; i1++) {
                    try {
                        RpcService client = rpcClient.createAsyncService(HelloService.class, "2.0");
                        RpcFuture helloFuture = client.call("hello", Integer.toString(i1));
                        String result = (String) helloFuture.get(3000, TimeUnit.MILLISECONDS);
                        if (!result.equals("Hi " + i1)) {
                            System.out.println("error = " + result);
                        } else {
                            System.out.println("result = " + result);
                        }
//                        try {
//                            Thread.sleep(5 * 1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            });
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        long timeCost = (System.currentTimeMillis() - startTime);
        String msg = String.format("Async call total-time-cost:%sms, req/s=%s", timeCost, ((double) (requestNum * threadNum)) / timeCost * 1000);
        System.out.println(msg);

        rpcClient.stop();

    }
}
