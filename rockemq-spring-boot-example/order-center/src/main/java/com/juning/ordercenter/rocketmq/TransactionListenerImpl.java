package com.juning.ordercenter.rocketmq;

import com.juning.ordercenter.dao.messaging.RocketmqTransactionLogMapper;
import com.juning.ordercenter.domain.entity.messaging.RocketmqTransactionLog;
import com.juning.ordercenter.domain.entity.order.Orders;
import com.juning.ordercenter.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import static org.apache.rocketmq.spring.support.RocketMQUtil.toRocketHeaderKey;

/**
 * @author yanjun
 */
@Service
@RocketMQTransactionListener(txProducerGroup = "order-center-group-tx")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class TransactionListenerImpl implements RocketMQLocalTransactionListener {
    private final OrderService orderService;
    private final RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        MessageHeaders headers = msg.getHeaders();
        String transationId = (String) headers.get(toRocketHeaderKey(RocketMQHeaders.TRANSACTION_ID));
        Integer orderId = Integer.valueOf((String) headers.get(toRocketHeaderKey(RocketMQHeaders.KEYS)));
        try {
            orderService.payOrderByIdWithTransactionLog(orderId, transationId, (Orders)arg);
            return RocketMQLocalTransactionState.UNKNOWN;
        } catch (Throwable e) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * RocketMQ回查本地事务
     * @param msg
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        MessageHeaders headers = msg.getHeaders();
        String transationId = (String) headers.get(toRocketHeaderKey(RocketMQHeaders.TRANSACTION_ID));
        RocketmqTransactionLog transactionLog = RocketmqTransactionLog.builder()
                .transactionId(transationId)
                .build();
        /*sql : select * from table where transation_id = ? */
        RocketmqTransactionLog selectOne = rocketmqTransactionLogMapper.selectOne(transactionLog);
        if (selectOne != null) {
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }
}