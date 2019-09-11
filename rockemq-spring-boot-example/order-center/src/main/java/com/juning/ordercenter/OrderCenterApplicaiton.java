package com.juning.ordercenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author yanjun
 */
@MapperScan(basePackages = "com.juning.ordercenter.dao")
@SpringBootApplication
public class OrderCenterApplicaiton {
    public static void main( String[] args ) {
        SpringApplication.run(OrderCenterApplicaiton.class, args);
    }
}