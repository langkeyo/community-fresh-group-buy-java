package com.langkeyo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.langkeyo.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 继承MyBatis-Plus的BaseMapper，自动拥有CRUD方法
 * @author langkeyo
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper已提供常用方法：
    // - insert(T entity)
    // - deleteById(Serializable id)
    // - updateById(T entity)
    // - selectById(Serializable id)
    // - selectOne(Wrapper<T> queryWrapper)
    // - selectList(Wrapper<T> queryWrapper)
    // 如需自定义SQL，可在这里添加方法
}