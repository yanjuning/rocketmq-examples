package com.juning.ordercenter.controller.order;

import com.juning.ordercenter.domain.entity.order.Orders;
import com.juning.ordercenter.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author yanjun
 */
@RestController
@RequestMapping("orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class OrderController {
    private final OrderService orderService;
    /** GET 测试简单 **/
    @GetMapping("pay/{id}")
    public Orders payOrderById(@PathVariable Integer id) {
        return orderService.payOrderById(id);
    }

    @GetMapping("payxt/{id}")
    public Orders payOrderByIdXT(@PathVariable Integer id) {
        return orderService.payOrderByIdWithDT(id);
    }

    @GetMapping("/{id}")
    public Orders findById(@PathVariable Integer id) {
        return orderService.findById(id);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException(Exception ex) {
        return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
