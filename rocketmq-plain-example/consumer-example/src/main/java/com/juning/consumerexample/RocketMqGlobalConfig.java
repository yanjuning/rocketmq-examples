package com.juning.consumerexample;

/**
 * @author yanjun
 */
public class RocketMqGlobalConfig {
    public static final String NAMESPACE = "dev";
    public static final String CONSUMER_GROUP = "rocketmq-plain-example-consumer-group";
    public static final String TOPIC = "rocketmq-plain-example-topic";
    public static final String TAG_SIMPLE = "tag_simple";
    public static final String TAG_ORDER = "tag_order";
    public static final String TAG_BATCH = "tag_batch";
    public static final String TAG_SCHEDULE = "tag_schedule";
    public static final String TAG_TRANSACTION = "tag_transaction";

    public static final String NAMESRVADDR = "172.29.41.175:9876";
}