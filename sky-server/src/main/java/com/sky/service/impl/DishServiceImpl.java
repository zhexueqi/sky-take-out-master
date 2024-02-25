package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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

    @Autowired
    private SetmealDishMapper setmealDishMapper;

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

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询:{}",dishPageQueryDTO);
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /*
     * 菜品批量删除
     * */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //查看是否有起售中的菜品，有的话则不能删除
        for (Long DishId : ids) {
            Dish dish = dishMapper.getById(DishId);
            if (dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //查看是否有菜品关联了套餐，有的话则不能删除
        List<Long> setmealByDishId = setmealDishMapper.getSetmealByDishId(ids);
        if (setmealByDishId != null && setmealByDishId.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品
//        for (Long DishId : ids) {
//            dishMapper.deleteById(DishId);
//            //删除菜品的口味
//            dishFlavorMapper.deleteById(DishId);
//        }

        //批量删除菜品sql
        dishMapper.deleteByIds(ids);
        //批量删除口味sql
        dishFlavorMapper.deleteByIds(ids);

    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        log.info("根据ID查询菜品和口味:{}",id);
        //根据ID查询菜品
        Dish dish = dishMapper.getById(id);
        //根据ID查询口味
        List<DishFlavor> flavor = dishFlavorMapper.getById(id);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(flavor);
        return dishVO;
    }

    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        log.info("修改菜品:{}",dishDTO);
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        //修改菜品表信息
        dishMapper.update(dish);

        //删除原有口味
        dishFlavorMapper.deleteById(dishDTO.getId());

        //重新添加口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size()>0){
            //因为前端传不了dishId给我们，所以我们这里需要把dishId赋值到flavors中
            flavors.forEach(flavor -> flavor.setDishId(dishDTO.getId()));
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }


}
