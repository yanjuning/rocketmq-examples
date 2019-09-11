package com.juning.consumerexample;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.juning.consumerexample.RocketMqGlobalConfig.*;


/**
 * @author yanjun
 */
@Slf4j
public class OrderConsumer {
    public static void main(String[] args) throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(NAMESPACE, CONSUMER_GROUP);
        consumer.setNamesrvAddr(NAMESRVADDR);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe(TOPIC, "TagA || TagC || TagD");
        consumer.setConsumeMessageBatchMaxSize(1);
        consumer.registerMessageListener(new MessageListenerOrderly() {
            AtomicLong consumeTimes = new AtomicLong(0);
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                context.setAutoCommit(false);
                for (MessageExt messageExt: msgs) {
                    log.info("接收到新的消息：queueId={}, body={}", messageExt.getQueueId(), new String(messageExt.getBody()));
//                    consumeTimes.incrementAndGet();
//                if ((this.consumeTimes.get() % 2) == 0) {
//                    return ConsumeOrderlyStatus.SUCCESS;
//                } else if ((this.consumeTimes.get() % 3) == 0) {
//                    return ConsumeOrderlyStatus.ROLLBACK;
//                } else if ((this.consumeTimes.get() % 4) == 0) {
//                    return ConsumeOrderlyStatus.COMMIT;
//                } else if ((this.consumeTimes.get() % 5) == 0) {
//                    context.setSuspendCurrentQueueTimeMillis(3000);
//                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
//                }
                }
                return ConsumeOrderlyStatus.SUCCESS;
            }
        });

        consumer.start();
        log.info("消费端已启动.");
    }
}
