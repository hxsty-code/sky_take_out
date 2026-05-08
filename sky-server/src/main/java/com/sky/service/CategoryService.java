package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    /**
     * 新增分类
     * @param category
     */
    void save(CategoryDTO category);

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 分类状态修改
     */
    void startOrStop(Integer status, Long id);

    /**
     * 删除分类
     * @param id
     */
    void delete(Long id);

    /**
     * 根据类型查询
     * @param type
     * @return
     */
    List<Category> queryByType(Integer type);

    /**
     * 编辑分类
     * @param categoryDTO
     */
    void update(CategoryDTO categoryDTO);
}
