package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zhexueqi
 * @ClassName SetmealServiceImpl
 * @since 2024/2/26    9:20
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Transactional
    public SetmealVO getById(Long id) {

        //根据ID查询套餐信息
        Setmeal setmeal = setmealMapper.getById(id);
        //根据setmealId查询菜品信息
        List<SetmealDish> setmealDishes = setmealDishMapper.getDishBySetmealId(id);

        //将查询到的套餐信息封装到VO当中
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);

//        //根据查询到的套餐信息中的分类ID查询分类表
//        Long categoryId = setmeal.getCategoryId();

//        //获取到对应的分类信息
//        Category category = categoryMapper.getById(categoryId);

//        //将分类名称封装到VO当中
//        setmealVO.setCategoryName(category.getName());




        //将菜品信息封装到vo中
        setmealVO.setSetmealDishes(setmealDishes);
        System.out.println(setmealVO);
        return setmealVO;

    }

    @Override
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //插入套餐信息的同时，需要保存套餐和菜品的关联关系
        //插入套餐信息
        setmealMapper.save(setmeal);
        //保存套餐和菜品的关联关系
        Long id = setmeal.getId();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(id);
        }
        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        Page page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void update(SetmealDTO setmealDTO) {
        //拷贝属性
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //修改套餐信息
        setmealMapper.update(setmeal);
        //先删除原有的菜品，再把现在传过来的菜品再次插入
        setmealDishMapper.deleteDishBySetmealId(setmealDTO.getId());
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        Long id = setmeal.getId();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(id);
        }
        setmealDishMapper.insertBatch(setmealDishes);


    }

    @Override
    public void startOrStop(Integer status, Long id) {
        //起售商品时，如果套餐内有未停售的菜品，则提示:"套餐内包含未启售菜品，无法启售"
        if (status == StatusConstant.ENABLE){
            //查询出传入的套餐id对应的菜品
            List<Dish> dishList = setmealMapper.getBySetmealId(id);
            dishList.forEach(dish -> {
                if (dish.getStatus() == StatusConstant.DISABLE){
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            });
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status).build();
        setmealMapper.update(setmeal);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
