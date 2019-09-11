package com.juning.inventorycenter.service.inventory;

import com.juning.inventorycenter.dao.inventory.InventoryMapper;
import com.juning.inventorycenter.domain.dto.messaging.OrderPaidEvent;
import com.juning.inventorycenter.domain.entity.inventory.Inventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * @author yanjun
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class InventoryService {
    private final InventoryMapper inventoryMapper;

    public void minusInventory(OrderPaidEvent message) {
        // 1. 判断重复消息
        // TODO 通过处理事务表去重

        // 2. 若不重复，消费消息；若重复，丢弃消息
        Integer productId = message.getProductId();
        Assert.notNull(productId, "不允许空");
        Inventory inventory = inventoryMapper.selectOne(Inventory.builder()
                .productid(productId)
                .build()
        );
        Assert.isTrue(inventory.getQty() >= message.getTotalQty(), "库存不足");
        inventory.setQty(inventory.getQty() - message.getTotalQty());
        inventoryMapper.updateByPrimaryKeySelective(inventory);
    }
}
