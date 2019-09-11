package com.juning.producerexample;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.util.concurrent.atomic.AtomicLong;

import static com.juning.producerexample.RocketMqGlobalConfig.*;

/**
 * 使用RocketMQ以三种方式发送消息：同步，异步，单向
 * @author yanjun
 */
@Slf4j
public class SimpleProducer {
    private static AtomicLong counter = new AtomicLong(0);
    public static void main(String[] args) throws Exception {
        /**
         * 初始化Produer实例
         */
        DefaultMQProducer producer = new DefaultMQProducer(NAMESPACE, PRODUCER_GROUP);
        producer.setNamesrvAddr(NAMESRVADDR);
        producer.setRetryTimesWhenSendAsyncFailed(0);
        producer.setRetryTimesWhenSendFailed(0);
        producer.start();

        syncSend(producer);

        asyncSend(producer);

        onewaySend(producer);

        /*当producer不再使用时关闭并清理资源*/
        Thread.sleep(3000);
        producer.shutdown();
    }

    /**
     * 同步传输用于广泛的场景，例如重要的通知消息，SMS通知，SMS营销系统等
     * @param producer
     * @throws Exception
     */
    public static void syncSend(MQProducer producer) throws Exception {
        for (int i = 0; i < 3; i++) {
            byte[] msgBody = ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET);
            Message msg = new Message(TOPIC, "SYNC", String.valueOf(counter.getAndIncrement()), msgBody);
            /**
             * 同步发送消息
             * message: 消息对象
             * timeout: 发送超时
             */
            SendResult sendResult = producer.send(msg, 3000);
            //log.info("发送结果 sendResult = {}", sendResult);
        }
    }

    /**
     * 异步传输通常用于响应时间敏感的业务场景
     * @param producer
     * @throws Exception
     */
    public static void asyncSend(MQProducer producer) throws Exception {
        for (int i = 0; i < 10; i++) {
            byte[] msgBody = ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET);
            Message msg = new Message(TOPIC , "ASYNC", String.valueOf(counter.getAndIncrement()), msgBody);
            /**
             * 异步发送消息
             * message： 消息对象
             * SendCallback: 异步回调
             * timeout: 发送超时
             */
            producer.send(msg, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("异步发送消息结果 sendResult = {}", sendResult);
                }

                @Override
                public void onException(Throwable e) {
                    log.info("异步发送消息结果 e = {}", e.getMessage());
                }
            }, 3000);
        }
    }

    /**
     * 单向传输用于中等可靠性的情况，例如日志收集
     * @param producer
     * @throws Exception
     */
    public static void onewaySend(MQProducer producer) throws Exception {
        for (int i = 0; i < 10; i++) {
            byte[] msgBody = ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET);
            Message msg = new Message(TOPIC , "ONEWAY", String.valueOf(counter.getAndIncrement()), msgBody);
            producer.sendOneway(msg);
        }
    }
}