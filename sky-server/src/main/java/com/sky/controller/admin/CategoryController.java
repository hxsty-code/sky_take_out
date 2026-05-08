package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody CategoryDTO  categoryDTO){
        log.info("新增分类：{}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 分类分页查询
     */
    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分页查询：{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用禁用分类
     */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("分类状态：{},分类id：{}", status, id);
        categoryService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping
    public Result delete(Long id){
        log.info("删除分类，id：{}", id);
        categoryService.delete(id);
        return Result.success();
    }

    /**
     * 根据类型查询
     */
    @GetMapping("/list")
    public Result<List<Category>> queryByType(Integer type){
        log.info("查询分类：{}", type);
        List<Category> category = categoryService.queryByType(type);
        return Result.success(category);
    }

    /**
     * 编辑分类
     */
    @PutMapping
    public Result update(@RequestBody CategoryDTO categoryDTO){
        log.info("编辑分类：{}", categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }
}
