package com.juning.ordercenter.service.order;

import com.juning.ordercenter.dao.messaging.RocketmqTransactionLogMapper;
import com.juning.ordercenter.dao.order.OrdersMapper;
import com.juning.ordercenter.domain.dto.messaging.OrderPaidEvent;
import com.juning.ordercenter.domain.entity.messaging.RocketmqTransactionLog;
import com.juning.ordercenter.domain.entity.order.Orders;
import com.juning.ordercenter.domain.enums.OrderPayStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yanjun
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class OrderService {
    private final OrdersMapper orderMapper;
    private final RocketmqTransactionLogMapper rocketmqTransactionLogMapper;
    private final RocketMQTemplate rocketMQTemplate;

    public Orders findById(Integer id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 支付订单，成功支付后发送普通消息给库存管理系统
     * @param id
     * @return
     */
    public Orders payOrderById(Integer id) {
        // 1. 查询订单，如果Order不存在或者状态是已支付，抛出异常
        Orders order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (StringUtils.equals(OrderPayStatus.PAID.name(), order.getPayStatus())) {
            throw new IllegalArgumentException("订单已支付");
        }
        // 2. 支付订单...
        boolean payResult = id % 5 != 0;
        // 3. 如果支付失败，抛出异常，通知用户
        if (!payResult) {
            throw new RuntimeException("支付失败请稍后重试");
        }
        // 4. 如果支付成功，更新订单行，发送扣减库存消息
        order.setPayStatus(OrderPayStatus.PAID.name());
        orderMapper.updateByPrimaryKey(order);

        /**
         * 构建普通消息
         */
        Message<OrderPaidEvent> simpleMessage = MessageBuilder.withPayload(
                OrderPaidEvent.builder()
                        .orderId(id)
                        .productId(order.getProductid()).totalQty(order.getTotalqty())
                        .build())
                .setHeader("v1", "版本号")
                .setHeader(RocketMQHeaders.KEYS, order.getId())
                .build();
        SendResult sendResult = rocketMQTemplate.syncSend("order-topic:order-paid",simpleMessage);
        log.info("发送普通消息返回结果 sendResult = {}", sendResult);

        // 5. 通知用户订单已完成

        return order;
    }

    /**
     * 支付订单，支持分布式事务，成功支付后发送事务消息给库存管理系统
     * @param id
     */
    public Orders payOrderByIdWithDT(Integer id) {
        // 1. 查询订单，如果Order不存在或者状态是已支付，抛出异常
        Orders order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (StringUtils.equals(OrderPayStatus.PAID.name(), order.getPayStatus())) {
            throw new IllegalArgumentException("订单已支付");
        }
        // 2. 支付订单...
        boolean payResult = id % 5 != 0;
        // 3. 如果支付失败，抛出异常，通知用户
        if (!payResult) {
            throw new RuntimeException("支付失败请稍后重试");
        }
        /**
         * 组装事务性消息
         */
        Message<OrderPaidEvent> transactionalMessage = MessageBuilder.withPayload(
                OrderPaidEvent.builder()
                        .orderId(id)
                        .productId(order.getProductid()).totalQty(order.getTotalqty())
                        .build())
                .setHeader("v1", "版本号")
                .setHeader(RocketMQHeaders.KEYS, order.getId())
                .build();
        TransactionSendResult transactionSendResult = rocketMQTemplate.sendMessageInTransaction(
                "order-center-group-tx",
                "order-topic:order-paid",
                transactionalMessage,
                order);
        log.info("发送事务消息返回结果 transactionSendResult = {}", transactionSendResult);

        return order;
    }

    /**
     * 发送半消息后尝试提交本地事务，如果提交成功，将半消息状态设置为COMMIT，否则FALLBACK
     * {@link org.apache.rocketmq.spring.core.RocketMQLocalTransactionState}
     * @param orderId
     * @param transationId
     * @param order
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void payOrderByIdWithTransactionLog(Integer orderId, String transationId, Orders order) {
        // 4. 如果支付成功，更新订单行，发送扣减库存消息，通知用户
        order.setPayStatus(OrderPayStatus.PAID.name());
        orderMapper.updateByPrimaryKey(order);

        // 5. 插入事务日志打到消息事务表，供消息回查事务状态
        RocketmqTransactionLog rocketmqTransactionLog = RocketmqTransactionLog.builder()
                .transactionId(transationId)
                .externId(orderId)
                .log("支付成功")
                .build();
        rocketmqTransactionLogMapper.insert(rocketmqTransactionLog);
    }

}