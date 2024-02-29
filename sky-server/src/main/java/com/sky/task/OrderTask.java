package com.sky.task;


import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhexueqi
 * @ClassName OrderTask
 * @since 2024/2/29    11:34
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;


    @Scheduled(cron = "0 * * * * ?")//每分钟执行
    public void processTimeOutOrders(){
        log.info("超时订单处理：{}", LocalDateTime.now());

        //计算当前时间的前15分钟
        LocalDateTime time = LocalDateTime.now().minusMinutes(15);
        //查询订单处于未支付且下单时间是当前时间前15以前的订单（超时订单）
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT,time);
        if (ordersList!=null && ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setRejectionReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨1点执行
    public void processDeliveryOrders(){
        log.info("自动处理处于派送中的订单：{}", LocalDateTime.now());
        //计算当前时间的前60分钟，则表示处理下单时间为昨天的订单
        LocalDateTime time = LocalDateTime.now().minusMinutes(60);
        //查询订单处于派送中的订单（自动派送订单）
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS,time);

        if (ordersList!=null && ordersList.size()>0){
            for (Orders orders : ordersList) {
                //订单状态改为已完成
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
