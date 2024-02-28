package com.sky.mapper;


import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author zhexueqi
 * @ClassName DishFlavorMapper
 * @since 2024/2/25    16:48
 */
@Mapper
public interface DishFlavorMapper {


    void insertBatch(List<DishFlavor> flavors);


    /*
    * 根据菜品ID删除口味
    * */
    @Delete("delete from dish_flavor where dish_id = #{DishId}")
    void deleteById(Long DishId);

    void deleteByIds(List<Long> ids);

    @Select("select * from dish_flavor where dish_id = #{DishId}")
    List<DishFlavor> getById(Long DishId);
}
