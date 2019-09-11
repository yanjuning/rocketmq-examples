package com.juning.producerexample;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.juning.producerexample.RocketMqGlobalConfig.*;

/**
 * @author yanjun
 */
@Slf4j
public class BatchProducer {

    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(NAMESPACE, PRODUCER_GROUP);
        producer.setNamesrvAddr(NAMESRVADDR);
        producer.setRetryTimesWhenSendAsyncFailed(0);
        producer.setRetryTimesWhenSendFailed(0);
        producer.start();

        // 组装批量数据
        List<Message> messages = new ArrayList<>();
        messages.add(new Message(TOPIC, "BATCH", "OrderID001", "Hello world 0".getBytes()));
        messages.add(new Message(TOPIC, "BATCH", "OrderID002", "Hello world 1".getBytes()));
        messages.add(new Message(TOPIC, "BATCH", "OrderID003", "Hello world 2".getBytes()));

        // 如果不确定批量数据是否超过大小限制
        ListSplitter listSplitter = new ListSplitter(messages);
        while (listSplitter.hasNext()) {
            SendResult sendResult = producer.send(listSplitter.next());
            log.info("批量发送消息 sendResult={}", sendResult);
        }
        producer.shutdown();
    }

    /**
     * 发送超大批量数据时，可能不确定是否超出了大小限制=1MB
     * 使用ListSplitter类将大批量数据分批发送，保证每批数据不超过限制
     * 线程不安全
     */
    public static class ListSplitter implements Iterator<List<Message>> {
        private final int SIZE_LIMIT = 1000 * 1000;
        private final List<Message> messages;
        private int currIndex;
        public ListSplitter(List<Message> messages) {
            this.messages = messages;
        }
        @Override public boolean hasNext() {
            return currIndex < messages.size();
        }
        @Override public List<Message> next() {
            int nextIndex = currIndex;
            int totalSize = 0;
            for (; nextIndex < messages.size(); nextIndex++) {
                Message message = messages.get(nextIndex);
                int tmpSize = message.getTopic().length() + message.getBody().length;
                Map<String, String> properties = message.getProperties();
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    tmpSize += entry.getKey().length() + entry.getValue().length();
                }
                //for log overhead
                tmpSize = tmpSize + 20;
                if (tmpSize > SIZE_LIMIT) {
                    //it is unexpected that single message exceeds the SIZE_LIMIT
                    //here just let it go, otherwise it will block the splitting process
                    if (nextIndex - currIndex == 0) {
                        //if the next sublist has no element, add this one and then break, otherwise just break
                        nextIndex++;
                    }
                    break;
                }
                if (tmpSize + totalSize > SIZE_LIMIT) {
                    break;
                } else {
                    totalSize += tmpSize;
                }
            }
            List<Message> subList = messages.subList(currIndex, nextIndex);
            currIndex = nextIndex;
            return subList;
        }
    }
}
