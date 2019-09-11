package com.juning.inventorycenter.rocketmq;

import com.juning.inventorycenter.domain.dto.messaging.OrderPaidEvent;
import com.juning.inventorycenter.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yanjun
 */
@Service
@RocketMQMessageListener(consumerGroup = "${inventorycenter.rocketmq.consumer-group}",
        topic = "${ordercenter.rocketmq.topic}",
        selectorType = SelectorType.TAG, selectorExpression = "order-paid")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class OrderPaidEventConsumer implements RocketMQListener<OrderPaidEvent> {
    private final InventoryService inventoryService;

    @Override
    public void onMessage(OrderPaidEvent message) {
        // TODO 返回消费结果
        log.info("接收到消息 OrderPaidEvent = {}", message);
        inventoryService.minusInventory(message);
    }
}