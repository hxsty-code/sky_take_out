package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增菜品数据
     * @param dish
     */
    @Insert("insert into dish (name, category_id, price, status, create_time, update_time, create_user, update_user, image, description) " +
            "values (#{name},#{categoryId},#{price},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser},#{image},#{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")   // 设置主键返回
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    //select * from dish where id = ?
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 批量删除菜品
     * @param ids
     */
    //delete from dish where id in (?,?,?)
    void delete(List<Long> ids);

    /**
     * 根据id查询菜品和口味
     * @param id
     * @return
     */
    DishVO queryById(Long id);

    /**
     * 修改菜品
     * @param dish
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据条件查询菜品数据
     * @param dish
     * @return
     */
    List<Dish> list(Dish dish);

    /**
     * 根据套餐id查询菜品
     * @param setmealId
     * @return
     */
    @Select("select d.* from dish d left join setmeal_dish s on s.dish_id = d.id where s.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);
}