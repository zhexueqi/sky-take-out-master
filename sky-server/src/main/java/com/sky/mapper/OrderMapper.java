package com.sky.mapper;


import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
