package com.juning.inventorycenter.domain.entity.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import javax.persistence.*;

/**
 * 产品ID的库存数量
 */
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 产品ID
     */
    private Integer productid;

    /**
     * 产品数量
     */
    private Integer qty;

    /**
     * 版本
     */
    @tk.mybatis.mapper.annotation.Version
    private Integer version;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

}