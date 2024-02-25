package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.result.Result;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhexueqi
 * @ClassName DishController
 * @since 2024/2/25    16:40
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    DishService dishService;

    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
      log.info("新增菜品");
      dishService.saveWithFlavor(dishDTO);
      log.info("新增菜品成功");
      return Result.success();
    }
}
