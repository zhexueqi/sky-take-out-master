package com.sky.service;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;
import org.springframework.stereotype.Service;

/**
 * @author zhexueqi
 * @ClassName SetmealService
 * @since 2024/2/26    9:20
 */


public interface SetmealService {
    SetmealVO getById(Long id);

    void save(SetmealDTO setmealDTO);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void update(SetmealDTO setmealDTO);

    void startOrStop(Integer status, Long id);
}
