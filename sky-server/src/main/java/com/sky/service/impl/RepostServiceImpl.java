package com.sky.service.impl;


import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.RepostService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhexueqi
 * @ClassName RepostServiceImpl
 * @since 2024/3/1    20:42
 */
@Service
@Slf4j
public class RepostServiceImpl implements RepostService {


    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    /**
     * 统计时间段内每天的营业额
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //当前集合用来存放从begin到end范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }


        //拼接日期，以逗号分隔
        String date = "";
        for (LocalDate localDate : dateList) {
            if (localDate.equals(dateList.get(0))){
                date = localDate.toString();
            }
            date = date + "," + localDate;
        }
        log.info("日期：{}", date);


        //存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            log.info("营业额：{}", turnover);
            if (turnover == null){
                turnover = 0.0;
            }
            turnoverList.add(turnover);
        }




        return TurnoverReportVO.builder()
                .dateList(date)
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    /**
     * 统计时间段内每天的用户数据
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //当前集合用来存放从begin到end范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //拼接日期，以逗号分隔
        String date = "";
        for (LocalDate localDate : dateList) {
            if (localDate.equals(dateList.get(0))){
                date = localDate.toString();
            }
            date = date + "," + localDate;
        }
        log.info("日期：{}", date);

        //用户总量集合
        List<Integer> totalUserList = new ArrayList<>();
        //新用户总量集合
        List<Integer> newUserList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            //获取日期的开始时间
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            //获取日期的结束时间
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            //封装查询总量的map集合
            Map map = new HashMap<>();
            map.put("end",endTime);
            //查询总量
            Integer userTotalCount = userMapper.countByMap(map);
            totalUserList.add(userTotalCount);

            map.put("begin",beginTime);
            //查询该日期的新用户总量
            Integer newUserCount = userMapper.countByMap(map);
            newUserList.add(newUserCount);
        }


        return UserReportVO
                .builder()
                .dateList(date)
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build()
                ;
    }

    /**
     * 统计时间段内每天的订单数据
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //当前集合用来存放从begin到end范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //拼接日期，以逗号分隔
        String date = "";
        for (LocalDate localDate : dateList) {
            if (localDate.equals(dateList.get(0))){
                date = localDate.toString();
            }
            date = date + "," + localDate;
        }
        log.info("日期：{}", date);
        //每日订单数
        List<Integer> orderCountList = new ArrayList<>();
        //每日有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();

        //订单总数
        Integer totalOrderCount = 0;
        //有效订单数
        Integer validOrderCount = 0;

        for (LocalDate localDate : dateList) {
            //获取日期的开始时间
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            //获取日期的结束时间
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            //根据动态SQL查询订单量
            Integer orderCount = orderMapper.getOrderCount(map);
            //订单总数
            totalOrderCount = totalOrderCount + orderCount;

            orderCountList.add(orderCount);
            map.put("status",Orders.COMPLETED);
            //根据动态SQL查询有效订单量
            Integer validOrderDayCount = orderMapper.getOrderCount(map);
            validOrderCountList.add(validOrderDayCount);
            //有效订单数
            validOrderCount = validOrderCount + validOrderDayCount;

            log.info("{}订单量：{}",localDate,orderCount);
            log.info("{}有效订单量：{}",localDate,validOrderDayCount);
        }

        //求总订单
        totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //求总有效订单
        validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        //计算订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue()/totalOrderCount;
        }

        return OrderReportVO
                .builder()
                .dateList(date)
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getTop10SalesStatistics(LocalDate begin, LocalDate end) {
        //获取日期的开始时间
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        //获取日期的结束时间
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        Map map = new HashMap<>();
        map.put("begin",beginTime);
        map.put("end",endTime);
        map.put("status",Orders.COMPLETED);
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getDetailByMap(map);
        List<String> names = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");
        List<Integer> numbers = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");


        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }
}
