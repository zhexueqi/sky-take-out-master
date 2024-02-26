package com.sky.mapper;


import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
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


    List<SetmealDish> getDishBySetmealId(Long setmealId);

    @Delete("delete from setmeal_dish where setmeal_id = #{SetmealId}")
    void deleteDishBySetmealId(Long SetmealId);

    void insertBatch(List<SetmealDish> setmealDishes);
}
