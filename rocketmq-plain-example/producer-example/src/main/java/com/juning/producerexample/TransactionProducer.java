package com.juning.producerexample;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.NamespaceUtil;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.juning.producerexample.RocketMqGlobalConfig.*;

/**
 * 生产事务消息
 * @author yanjun
 */
@Slf4j
public class TransactionProducer {

    public static void main(String[] args) throws Exception {
        TransactionMQProducer producer = new TransactionMQProducer(NAMESPACE, PRODUCER_GROUP);
        producer.setNamesrvAddr(NAMESRVADDR);
        producer.setExecutorService(new ThreadPoolExecutor(
                producer.getCheckThreadPoolMinSize(),
                producer.getCheckThreadPoolMaxSize(),
                1000 * 60,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(producer.getCheckRequestHoldMax()),
                new ThreadFactory() {
                    AtomicInteger idx = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "TransactionCheckerThread-" + idx.getAndIncrement());
                    }
                }));
        producer.setTransactionListener(new TransactionListenerImpl());
        producer.start();

        for (int i = 0; i < 10; i++) {
            Message message = new Message(TOPIC, "TRANSACTION", "KEYS_" + i,
                    ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            /**
             * TODO submit pull request
             */
            message.setTopic(NamespaceUtil.wrapNamespace(producer.getNamespace(), message.getTopic()));
            SendResult sendResult = producer.sendMessageInTransaction(message, "hello" + i);
        }
        Thread.sleep(1000 * 600);
        producer.shutdown();
    }
}

@Slf4j
class TransactionListenerImpl implements TransactionListener {
    private AtomicInteger flag = new AtomicInteger(0);
    private ConcurrentHashMap<String, Object> repostoryMapper = new ConcurrentHashMap<>();

    /**
     * 当发送半消息成功后执行本地事务，返回{@link LocalTransactionState}三种状态中的一种
     *     COMMIT_MESSAGE,  --提交消息
     *     ROLLBACK_MESSAGE,--回滚消息
     *     UNKNOW,          --未知状态，需要RocketMQ回查本地事务状态来决定消息是提交或回滚
     * @param msg
     * @param arg
     * @return
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        // 获取消息的事务ID属性
        String transactionId = msg.getTransactionId();
        int indexer = flag.getAndIncrement();
        // 模拟提交或者回滚本地事务
        if (indexer % 3 == 0) {
            repostoryMapper.put(transactionId, arg);
            log.info("事务消息提交成功-body={}, arg={}", new String(msg.getBody()), arg);
            return LocalTransactionState.COMMIT_MESSAGE;
        } else if (indexer % 5 == 0) {
            return LocalTransactionState.ROLLBACK_MESSAGE;
        } else {
            repostoryMapper.put(transactionId, arg);
            return LocalTransactionState.UNKNOW;
        }
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        String transactionId = msg.getTransactionId();
        if (repostoryMapper.containsKey(transactionId)) {
            log.info("事务消息回查成功-body={}", new String(msg.getBody()));
            return LocalTransactionState.COMMIT_MESSAGE;
        }
        log.info("事务消息回查失败-body={}", new String(msg.getBody()));
        return LocalTransactionState.ROLLBACK_MESSAGE;
    }
}
