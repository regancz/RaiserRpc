package com.raiser.test.client;

import com.raiser.rpc.client.RpcClient;
import com.raiser.rpc.client.handler.AsyncRPCCallback;
import com.raiser.rpc.client.handler.RpcFuture;
import com.raiser.rpc.client.proxy.RpcService;
import com.raiser.test.entity.Person;
import com.raiser.test.service.PersonService;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author: zhengyangxin
 * @date: 9/5/2022 8:39 PM
 */
public class RpcCallbackTest2 {
    public static void main(String[] args) throws InterruptedException {
        final RpcClient rpcClient = new RpcClient("127.0.0.1:2181");
        int threadNum = 8;
        final CountDownLatch countDownLatch = new CountDownLatch(threadNum);

        for (int i = 0; i < threadNum; i++) {
            int finalI = i;
            new Thread(() -> {
                try {
                    RpcService client = rpcClient.createAsyncService(PersonService.class, "");
                    int num = 5;
                    RpcFuture helloPersonFuture = client.call("callPerson", "Jerry" + finalI, num);
                    helloPersonFuture.addCallback(new AsyncRPCCallback() {
                        @Override
                        public void success(Object result) {
                            List<Person> persons = (List<Person>) result;
                            for (Person person : persons) {
                                System.out.println(person);
                            }
                            countDownLatch.countDown();
                        }

                        @Override
                        public void fail(Exception e) {
                            System.out.println(e);
                            countDownLatch.countDown();
                        }
                    });
                } catch (Exception e) {
                    System.out.println(e);
                }
            }).start();
        }

        try {
            countDownLatch.await();
            rpcClient.stop();
            System.out.println("End");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
