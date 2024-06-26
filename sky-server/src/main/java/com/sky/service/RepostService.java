package com.sky.service;


import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * @author zhexueqi
 * @ClassName RepostService
 * @since 2024/3/1    20:42
 */
public interface RepostService {

    /**
     * 统计时间段内每天的营业额
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);



    /**
     * 统计时间段内每天的用户数据
     * @return
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 统计时间段内每天的订单数据
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getTop10SalesStatistics(LocalDate begin, LocalDate end);

    void exportBusinessData(HttpServletResponse response);
}
