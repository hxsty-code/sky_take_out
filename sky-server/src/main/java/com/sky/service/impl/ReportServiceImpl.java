package com.sky.service.impl;

import com.sky.dto.DataOverViewQueryDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
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
    @Autowired
    private UserMapper userMapper;

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

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 当前集合用于存放从begin到end的每天日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while(!begin.equals(end)){
            // 日期加1
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> newUserList = new ArrayList<>();      // 新用户数量
        List<Integer> totalUserList = new ArrayList<>();    // 用户总数

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);    // 一天的开始时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);      // 一天的结束时间
            
            // 统计当天新增用户：select count(id) from user where create_time > beginTime and create_time < endTime
            DataOverViewQueryDTO newUserQuery = DataOverViewQueryDTO.builder()
                    .begin(beginTime)
                    .end(endTime)
                    .build();
            Integer newUser = userMapper.countByMap(newUserQuery);
            
            // 统计截至当天的总用户数：select count(id) from user where create_time < endTime
            DataOverViewQueryDTO totalUserQuery = DataOverViewQueryDTO.builder()
                    .end(endTime)
                    .build();
            Integer totalUser = userMapper.countByMap(totalUserQuery);

            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }

        // 封装返回结果
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }
}
