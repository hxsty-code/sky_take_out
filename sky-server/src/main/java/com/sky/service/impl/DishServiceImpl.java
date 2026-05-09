package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
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
}
