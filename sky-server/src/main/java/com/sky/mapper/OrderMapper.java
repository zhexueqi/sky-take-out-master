package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author zhexueqi
 * @ClassName OrderMapper
 * @since 2024/2/28    16:09
 */
@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     * @param order
     */
    void insert(Orders order);


    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);


    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);


    @Select("select count(*) from orders where status = #{status}")
    Integer getStatistics(Integer status);

    @Select("SELECT * from orders where status=#{status} and order_time < #{time}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime time);


    /**
     * 根据map查询营业额
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    Integer getOrderCount(Map map);


    /**
     * 统计时间段内销量前十
     * @param map
     * @return
     */
    List<GoodsSalesDTO> getDetailByMap(Map map);
}
