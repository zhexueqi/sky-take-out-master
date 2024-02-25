package com.sky.mapper;


import com.sky.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author zhexueqi
 * @ClassName SetmealDishMapper
 * @since 2024/2/25    21:03
 */
@Mapper
public interface SetmealDishMapper {

    List<Long> getSetmealByDishId(List<Long> ids);
}
