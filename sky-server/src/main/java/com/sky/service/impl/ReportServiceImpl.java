package com.sky.service.impl;

import com.sky.dto.DataOverViewQueryDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 当前集合用于存放从begin到end的每天日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while(!begin.equals(end)){
            // 日期加1
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 当前集合用于存放从begin到end的每天日期对应的营业额
        List<Double> turnoverList = new ArrayList<>();
        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);    // 一天的开始时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);      // 一天的结束时间
            // 查询对应的营业额并添加到集合中
            DataOverViewQueryDTO dataOverViewQueryDTO = DataOverViewQueryDTO.builder()
                    .begin(beginTime)
                    .end(endTime)
                    .status(Orders.COMPLETED)
                    .build();
            // select sum(amount) from orders where order_time > begin and order_time < end and status = 5
            Double turnover = orderMapper.sumByMap(dataOverViewQueryDTO);
            turnoverList.add(turnover == null ? 0.0 : turnover);
        }

        // 封装返回结果
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }
}
