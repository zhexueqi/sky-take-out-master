package com.sky.service.impl;


import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhexueqi
 * @ClassName ShoppingCartServiceImpl
 * @since 2024/2/28    9:56
 */
@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;


    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void add(ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车");
        //先判断是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        if(shoppingCartList.size() > 0){
            //如果存在，则数量+1
            ShoppingCart list = shoppingCartList.get(0);
            list.setNumber(list.getNumber() + 1);
            shoppingCartMapper.update(list);
        }else{
            //如果不存在，判断是菜品还是套餐
            Long setmealId = shoppingCart.getSetmealId();
            if (setmealId != null){
                //则代表是套餐
                //则把对应对应套餐查询出来，将里面信息封装在shoppingCart中，然后添加到购物车表
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }else {
                //代表是菜品
                //则把对应对应菜品查询出来，将里面信息封装在shoppingCart中，然后添加到购物车表
                Long dishId = shoppingCart.getDishId();
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }
            //再统一对时间进行设置
            shoppingCart.setCreateTime(LocalDateTime.now());
            //因为不存在，所以默认为1
            shoppingCart.setNumber(1);
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        //获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        //构建ShoppingCart对象
        ShoppingCart cart = ShoppingCart.builder()
                .userId(userId).build();
        //查看购物车
        List<ShoppingCart> list = shoppingCartMapper.list(cart);
        return list;
    }

    @Override
    public void cleanShoppingCart() {
        //获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        //构建ShoppingCart对象
        ShoppingCart cart = ShoppingCart.builder()
                .userId(userId).build();
        //根据ID清空购物车
        shoppingCartMapper.deleteByUserId(cart);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        //获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        //构建ShoppingCart对象
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(userId);

        //因为可能userId一样，然后dishId一样的话，就要去对比口味了，所以这里先动态查询
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list.size() > 0 &&list !=null){
            ShoppingCart cart = list.get(0);
            if (cart.getNumber() == 1){
                //如果数量为1，则删除
                shoppingCartMapper.deleteByUserId(shoppingCart);
            }else {
                //如果数列量大于1，则数量-1
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.update(cart);
            }
        }
    }
}
