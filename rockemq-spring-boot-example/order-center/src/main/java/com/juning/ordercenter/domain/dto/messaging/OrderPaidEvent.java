package com.juning.ordercenter.domain.dto.messaging;

import lombok.*;

import java.math.BigDecimal;

/**
 * @author yanjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderPaidEvent {
    private Integer orderId;
    private Integer productId;
    private Integer totalQty;
    private BigDecimal paidMoney;
}