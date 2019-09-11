package com.juning.ordercenter.domain.entity.messaging;

import lombok.*;

import javax.persistence.*;

@Table(name = "rocketmq_transaction_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RocketmqTransactionLog {
    @Id
    @Column(name = "`id`")
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 事务ID
     */
    @Column(name = "`transaction_id`")
    private String transactionId;

    /**
     * 管理外部表主键
     */
    @Column(name = "`extern_id`")
    private Integer externId;

    /**
     * 日志描述
     */
    @Column(name = "`log`")
    private String log;
}