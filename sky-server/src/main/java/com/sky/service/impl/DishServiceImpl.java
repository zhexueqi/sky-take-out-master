package com.sky.service.impl;


import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zhexueqi
 * @ClassName DishServiceImpl
 * @since 2024/2/25    16:42
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        log.info("新增菜品:{}",dishDTO);
        Dish dish = new Dish();
        //向菜品表插入1条数据
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);

        //因为在dishMapper.xml中，我们设置了返回主键值到Dish对象的id属性中，所以这里可以get到
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size()>0){
            //因为前端传不了dishId给我们，所以我们这里需要把dishId赋值到flavors中
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }

    }
}
