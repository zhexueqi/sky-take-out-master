package com.sky.mapper;


import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author zhexueqi
 * @ClassName DishFlavorMapper
 * @since 2024/2/25    16:48
 */
@Mapper
public interface DishFlavorMapper {


    void insertBatch(List<DishFlavor> flavors);
}