package com.sky.mapper;


import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhexueqi
 * @ClassName OrderMapper
 * @since 2024/2/28    16:09
 */
@Mapper
public interface OrderMapper {
    void insert(Orders order);
}
