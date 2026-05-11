package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品和对应的口味
     */
    public void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     */
    void delete(List<Long> ids);

    /**
     * 根据id查询菜品和对应的口味
     */
    DishVO queryByIdWithFlavor(Long id);

    /**
     * 修改菜品
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 菜品起售、停售
     */
    void startOrStop(Integer status, Long id);
}