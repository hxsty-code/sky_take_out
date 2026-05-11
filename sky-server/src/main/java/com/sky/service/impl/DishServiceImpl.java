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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Transactional  // 开启事务
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        //1、创建菜品对象
        Dish dish = new Dish();
        //2、对象属性拷贝
        BeanUtils.copyProperties(dishDTO, dish);
        //3、插入到数据库
        dishMapper.insert(dish);
        //4、获取插入后的菜品的id
        Long dishId = dish.getId();
        //5、创建菜品口味对象
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && !flavors.isEmpty()) {
            //6、遍历菜品口味列表，为每个菜品口味设置菜品id
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            //7、批量插入菜品口味数据到数据库
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //1、执行分页查询
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        //2、执行查询
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        //3、封装返回结果
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    @Transactional  // 开启事务
    public void delete(List<Long> ids) {
        //判断菜品状态，起售中的菜品不能删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                //起售中的菜品不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断菜品是否关联了套餐，被套餐关联的菜品不能删除
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //批量删除菜品数据
        dishMapper.delete(ids);
        //批量删除菜品关联的口味数据
        dishFlavorMapper.deleteByDishId(ids);
    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    @Override
    public DishVO queryByIdWithFlavor(Long id) {
        //根据id查询菜品数据
        DishVO dishVO = dishMapper.queryById(id);
        if (dishVO != null) {
            //根据id查询口味数据
            List<DishFlavor> flavors = dishFlavorMapper.queryByDishId(id);
            //封装口味数据
            dishVO.setFlavors(flavors);
        }
        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
       //1、创建菜品对象
       Dish dish = new Dish();
       //2、对象属性拷贝
        BeanUtils.copyProperties(dishDTO, dish);
        //3、修改菜品数据
        dishMapper.update(dish);
        //4、删除菜品关联的口味数据
        dishFlavorMapper.deleteByDishId(Collections.singletonList(dishDTO.getId()));    //单个元素转成list
        //5、重新插入菜品口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            //6、遍历菜品口味列表，为每个菜品口味设置菜品id
            for(DishFlavor flavor : flavors){
                flavor.setDishId(dishDTO.getId());
            }
            //7、批量插入菜品口味数据到数据库
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 启用、禁用菜品
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
                dishMapper.update(dish);
    }

}