package com.sky.controller.admin;


import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * @author zhexueqi
 * @ClassName OrderController
 * @since 2024/2/28    20:22
 */
@RestController("adminController")
@Slf4j
@Api(tags = "后台管理-订单管理")
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping ("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("查询条件查询");
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics(){
        log.info("各个状态的订单数量统计");
        OrderStatisticsVO orderStatisticsVO = orderService.getStatistics();
        return Result.success(orderStatisticsVO);
    }

    @PutMapping("/confirm")
    public Result confirm(@RequestBody Orders orders){
        log.info("接单：{}",orders);
        orderService.confirm(orders);
        return Result.success();
    }

    @PutMapping("/rejection")
    public Result rejection(@RequestBody Orders orders) throws Exception {
        log.info("拒单：{}",orders);
        orderService.rejection(orders);
        return Result.success();
    }


    @PutMapping("/cancel")
    public Result cancel(@RequestBody Orders orders) throws Exception {
        log.info("取消订单：{}",orders);
        orderService.rejection(orders);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable("id") Long id){
        log.info("订单发货：{}",id);
        orderService.delivery(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable("id") Long id){
        log.info("订单完成：{}",id);
        orderService.complete(id);
        return Result.success();
    }


}
