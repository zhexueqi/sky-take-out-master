package com.sky.mapper;


import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author zhexueqi
 * @ClassName UserMapper
 * @since 2024/2/27    10:31
 */
@Mapper
public interface UserMapper {

    @Select("select * from user where openid = #{openid}")
    User getUserByOpenId(String openid);

    void insert(User user);

    @Select("select * from user where id =#{userId}")
    User getById(Long userId);
}
