package com.langkeyo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.langkeyo.common.Result;
import com.langkeyo.common.ResultCode;
import com.langkeyo.dto.ProductItemDTO;
import com.langkeyo.entity.Product;
import com.langkeyo.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Autowired
    private ProductMapper productMapper;

    @GetMapping("/list")
    public Result<List<ProductItemDTO>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.eq("deleted", 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.like("name", keyword.trim());
        }
        List<Product> products = productMapper.selectList(wrapper);
        List<ProductItemDTO> data = products.stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<ProductItemDTO> detail(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error(ResultCode.PRODUCT_NOT_FOUND);
        }
        return Result.success(toItemDTO(product));
    }

    private ProductItemDTO toItemDTO(Product product) {
        ProductItemDTO dto = new ProductItemDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        dto.setPrice(product.getPrice());
        dto.setGroupPrice2(product.getGroupPrice2());
        dto.setGroupPrice3(product.getGroupPrice3());
        dto.setStock(product.getStock());
        dto.setImages(product.getImages());
        dto.setStatus(product.getStatus());
        return dto;
    }
}
