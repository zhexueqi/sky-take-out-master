package com.sky.controller.admin;


import com.sky.result.Result;
import com.sky.service.RepostService;
import com.sky.vo.TurnoverReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * @author zhexueqi
 * @ClassName ReportController
 * @since 2024/3/1    20:38
 */
@RestController
@Slf4j
@RequestMapping("/admin/report")
@Api(tags = "数据统计相关接口")
public class ReportController {

    @Autowired
    private RepostService repostService;


    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> turnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("营业额数据统计：{}，{}",begin,end);
        TurnoverReportVO turnoverReportVO = repostService.getTurnoverStatistics(begin,end);

        return Result.success(turnoverReportVO);
    }
}
