package com.langkeyo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.langkeyo.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    @Select({
            "<script>",
            "SELECT id, name FROM products WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<Product> selectNameListByIds(@Param("ids") List<Long> ids);
}
