package com.juning.ordercenter.service.mock;

import com.juning.ordercenter.dao.order.OrdersMapper;
import com.juning.ordercenter.domain.entity.order.Orders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yanjun
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MockService {
    private final OrdersMapper ordersMapper;
    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 异步发送
     */
    public void asyncMessage() {
        Orders orders = ordersMapper.selectByPrimaryKey(1);
        Message message = MessageBuilder
                .withPayload(orders)
                .setHeader(RocketMQHeaders.KEYS, orders.getId())
                .setHeader("description", "异步消息")
                .build();
        rocketMQTemplate.asyncSend("order-mock-topic: async-order", message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("发送异步消息成功 message = {}, sendResult = {}", message, sendResult);
            }

            @Override
            public void onException(Throwable e) {
                log.info("发送异步失败message = {}, sendResult = {}", message, e.getMessage());
            }
        });
    }

    /**
     * 批量发送消息
     */
    public void batchMessages() {
        List<Orders> ordersList = ordersMapper.selectAll();
        List<Message<Orders>> messageList = ordersList.stream()
                .map(orders -> MessageBuilder
                        .withPayload(orders)
                        .setHeader(RocketMQHeaders.KEYS, orders.getId())
                        .setHeader("description", "批量消息")
                        .build())
                .collect(Collectors.toList());
        SendResult sendResult = rocketMQTemplate.syncSend("order-mock-topic: batch-order", messageList);
        log.info("批量发送消息 sendResult={}", sendResult);
    }

    /**
     * 顺序消息
     */
    public void orderlyMessage() {

    }
}