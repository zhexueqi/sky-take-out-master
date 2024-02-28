package com.sky.service;


import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

/**
 * @author zhexueqi
 * @ClassName ShoppingCartService
 * @since 2024/2/28    9:55
 */
public interface ShoppingCartService {

    void add(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showShoppingCart();



    void cleanShoppingCart();

    void sub(ShoppingCartDTO shoppingCartDTO);
}
