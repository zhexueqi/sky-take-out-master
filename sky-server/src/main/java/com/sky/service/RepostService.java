package com.sky.service;


import com.sky.vo.TurnoverReportVO;

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
}
