package com.juning.producerexample;

/**
 * 大部分场景下，tag标签是一个选择消息简单且实用的方式
 * 复杂场景下，可以使用SQL表达式过滤消息
 *
 * message.putUserProperty("a", String.valueOf(10));
 * consumer.subscribe("SomeTopic", MessageSelector.bySql("a between 0 and 100");
 * @author yanjun
 */
public class FilterProducer {
}
