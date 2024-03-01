package com.sky.service.impl;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.RepostService;
import com.sky.vo.TurnoverReportVO;
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
}
