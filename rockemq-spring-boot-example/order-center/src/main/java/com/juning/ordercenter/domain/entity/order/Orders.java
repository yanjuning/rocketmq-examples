package com.juning.ordercenter.domain.entity.order;

import lombok.*;

import java.util.Date;
import javax.persistence.*;

@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Orders {
    /**
     * 订单ID
     */
    @Id
    @Column(name = "`id`")
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 用户ID
     */
    @Column(name = "`userid`")
    private Integer userid;

    /**
     * 产品ID
     */
    @Column(name = "`productid`")
    private Integer productid;

    /**
     * 总数
     */
    @Column(name = "`totalqty`")
    private Integer totalqty;

    /**
     * 支付状态
     */
    @Column(name = "`pay_status`")
    private String payStatus;

    /**
     * 订单描述
     */
    @Column(name = "`description`")
    private String description;

    /**
     * 订单创建时间
     */
    @Column(name = "`create_time`")
    private Date createTime;

    /**
     * 订单更新时间
     */
    @Column(name = "`update_time`")
    private Date updateTime;

    /**
     * 版本号
     */
    @tk.mybatis.mapper.annotation.Version
    @Column(name = "`version`")
    private Integer version;
}