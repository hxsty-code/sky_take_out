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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //判断当前商品是否在购物车中
        ShoppingCart shoppingcart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingcart);
        shoppingcart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingcart);

        // 如果已经存在，则增加数量
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        }else{
            // 如果不存在，则添加到购物车
            // 判断添加的是菜品还是套餐
            Long dishId = shoppingcart.getDishId();
            if (dishId != null) {
                // 添加的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingcart.setName(dish.getName());
                shoppingcart.setImage(dish.getImage());
                shoppingcart.setAmount(dish.getPrice());
            }else{
                // 添加的是套餐
                Setmeal setmeal = setmealMapper.getById(shoppingcart.getSetmealId());
                shoppingcart.setName(setmeal.getName());
                shoppingcart.setImage(setmeal.getImage());
                shoppingcart.setAmount(setmeal.getPrice());
            }
            shoppingcart.setNumber(1);
            shoppingcart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingcart);
        }

    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        // 获取当前用户id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingcart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingcart);
        return list;
    }
}
