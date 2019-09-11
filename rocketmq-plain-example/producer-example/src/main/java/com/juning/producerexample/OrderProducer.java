package com.juning.producerexample;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.util.List;

import static com.juning.producerexample.RocketMqGlobalConfig.NAMESPACE;
import static com.juning.producerexample.RocketMqGlobalConfig.NAMESRVADDR;
import static com.juning.producerexample.RocketMqGlobalConfig.PRODUCER_GROUP;

/**
 * RocketMQ使用FIFO顺序提供有序消息
 * 以下示例演示了发送/接收全局和分区排序的消息
 * @author yanjun
 */
@Slf4j
public class OrderProducer {
    public static void main(String[] args) throws Exception {
        /**
         * 初始化生产者客户端
         * 生产者组：PRODUCER_GROUP，将相同服务提供者归为一组，事务回查
         * 名字地址：NAMESRVADDR，心跳获取 broker 队列路由信息
         */
        DefaultMQProducer producer = new DefaultMQProducer(NAMESPACE, PRODUCER_GROUP);
        producer.setNamesrvAddr(NAMESRVADDR);
        /**
         * 启动服务
         */
        producer.start();
        /**
         * 标签是消费端过滤选择消息的简单和有效的方式
         */
        String[] tags = new String[] {"TagA", "TagB", "TagC", "TagD", "TagE"};
        for (int i = 0; i < 100; i++) {
            int orderId = i % 10;
            /**
             * 组装消息，主要属性
             * TOPIC：主题
             * TAG：标签
             * KEYS：健值，设置为有业务意义值例如说订单号，通常使用keys在运维平台快速查询消息，keys也可以用作消费端选择过滤消息
             * BODY：消息体，byte数组
             */
            Message msg = new Message(RocketMqGlobalConfig.TOPIC,
                    tags[i % tags.length],
                    "KEY" + i,
                    ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            /**
             * 同步发送消息
             * MessageQueueSelector：通过它获得目标消息队列以传递消息
             * arg：与消息队列选择器配合的参数
             * SendResult：TODO what how why
             */
            SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    Integer orderId = (Integer) arg;
                    int index = orderId % mqs.size();
                    return mqs.get(index);
                }
            }, orderId);
            log.info("发送结果 sendResult = {}", sendResult);
        }

        /**
         * 关闭连接和清理资源
         */
        producer.shutdown();
    }
}
