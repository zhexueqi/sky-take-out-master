package com.sky.mapper;


import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author zhexueqi
 * @ClassName OrderDetailMapper
 * @since 2024/2/28    16:32
 */
@Mapper
public interface OrderDetailMapper {
    void insertBatch(List<OrderDetail> orderDetailList);

    @Select("select * from order_detail where order_id=#{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);
}
