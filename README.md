# Apache RocketMQ Examples

Aapche RocketMQ结合不同框架的使用示例，子项目及其说明如下：

* rocketmq-plain-example

  rocketmq原生客户端使用示例，可以借助该项目工程调试消息发送和消费过程

* rocket-spring-example

  rocketmq客户端结合springframework使用示例，未完成

* rocket-spring-boot-example

  rocketmq客户端结合spring boot使用示例，设计了订单支付和库存扣减的场景，可以给实际项目提供应用参考

* rocket-spring-cloud-example

  rocketmq客户端结合spring cloud概念模型，未完成

## rocket-spring-boot-example

### 场景

以下是一个分布式事务场景，即订单支付-发送支付成功事件-库存扣减

![1567741380224](assets\1567741380224.png)

### 数据库

分为order库和inventory库

![1567741480195](assets\1567741480195.png)

建表语句

```sql
-- 订单表
CREATE TABLE `orders` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `userid` int(11) DEFAULT NULL COMMENT '用户ID',
  `productid` int(11) DEFAULT NULL COMMENT '产品ID',
  `totalqty` int(11) DEFAULT NULL COMMENT '总数',
  `pay_status` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'NOT_YET' COMMENT '支付状态',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '订单描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '订单更新时间',
  `version` int(11) DEFAULT NULL COMMENT '版本号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 订单事务表
CREATE TABLE `rocketmq_transaction_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `transaction_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事务ID',
  `log` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '日志描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='rocketmq事务日志';

-- 库存表
CREATE TABLE `inventory` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `productid` int(11) DEFAULT NULL COMMENT '产品ID',
  `qty` int(11) DEFAULT NULL COMMENT '产品数量',
  `version` int(11) DEFAULT NULL COMMENT '版本',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
```

