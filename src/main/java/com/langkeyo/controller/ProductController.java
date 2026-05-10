package com.langkeyo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.langkeyo.common.Result;
import com.langkeyo.common.ResultCode;
import com.langkeyo.dto.LeaderGroupControlReqDTO;
import com.langkeyo.dto.ProductItemDTO;
import com.langkeyo.entity.Product;
import com.langkeyo.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private static final int PRODUCT_IMAGES_MAX_LENGTH = 1024;

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

    @GetMapping("/admin/list")
    public Result<List<ProductItemDTO>> adminList(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Integer status) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.like("name", keyword.trim());
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("id");
        List<ProductItemDTO> data = productMapper.selectList(wrapper).stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());
        return Result.success(data);
    }

    @GetMapping("/leader/list")
    public Result<List<ProductItemDTO>> leaderList(@RequestParam(required = false) String keyword) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.like("name", keyword.trim());
        }
        wrapper.orderByDesc("id");
        List<ProductItemDTO> data = productMapper.selectList(wrapper).stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());
        return Result.success(data);
    }

    @PostMapping("/admin/create")
    public Result<String> adminCreate(@RequestBody Product payload) {
        if (!StringUtils.hasText(payload.getName())) {
            return Result.error(ResultCode.PARAM_IS_BLANK);
        }
        Result<String> imageCheck = validateImagesField(payload.getImages());
        if (imageCheck != null) {
            return imageCheck;
        }
        payload.setId(null);
        payload.setStatus(payload.getStatus() == null ? 1 : payload.getStatus());
        payload.setGroupOpen(payload.getGroupOpen() == null ? 0 : payload.getGroupOpen());
        payload.setDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        payload.setCreateTime(now);
        payload.setUpdateTime(now);
        boolean ok = productMapper.insert(payload) > 0;
        if (!ok) {
            return Result.error("商品创建失败");
        }
        return Result.success("商品创建成功");
    }

    @PutMapping("/admin/update/{id}")
    public Result<String> adminUpdate(@PathVariable Long id, @RequestBody Product payload) {
        Product db = productMapper.selectById(id);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error(ResultCode.PRODUCT_NOT_FOUND);
        }
        Result<String> imageCheck = validateImagesField(payload.getImages());
        if (imageCheck != null) {
            return imageCheck;
        }
        payload.setId(id);
        payload.setCreateTime(db.getCreateTime());
        payload.setDeleted(db.getDeleted() == null ? 0 : db.getDeleted());
        payload.setUpdateTime(LocalDateTime.now());
        boolean ok = productMapper.updateById(payload) > 0;
        if (!ok) {
            return Result.error("商品更新失败");
        }
        return Result.success("商品更新成功");
    }

    @PutMapping("/admin/status/{id}")
    public Result<String> adminUpdateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Product db = productMapper.selectById(id);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error(ResultCode.PRODUCT_NOT_FOUND);
        }
        db.setStatus(status);
        db.setUpdateTime(LocalDateTime.now());
        boolean ok = productMapper.updateById(db) > 0;
        if (!ok) {
            return Result.error("商品状态更新失败");
        }
        return Result.success("商品状态更新成功");
    }

    @PutMapping("/admin/stock/{id}")
    public Result<String> adminUpdateStock(@PathVariable Long id, @RequestParam Integer stock) {
        Product db = productMapper.selectById(id);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (stock == null || stock < 0) {
            return Result.error(ResultCode.PARAM_ERROR);
        }
        db.setStock(stock);
        db.setUpdateTime(LocalDateTime.now());
        boolean ok = productMapper.updateById(db) > 0;
        if (!ok) {
            return Result.error("库存更新失败");
        }
        return Result.success("库存更新成功");
    }

    @PutMapping("/leader/group/{id}")
    public Result<String> leaderUpdateGroupControl(@PathVariable Long id, @RequestBody LeaderGroupControlReqDTO req) {
        Product db = productMapper.selectById(id);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (req == null) {
            return Result.error(ResultCode.PARAM_ERROR);
        }
        if (req.getGroupOpen() != null && req.getGroupOpen() != 0 && req.getGroupOpen() != 1) {
            return Result.error("开团状态参数错误");
        }
        if (req.getGroupStartTime() != null && req.getGroupEndTime() != null
                && req.getGroupEndTime().isBefore(req.getGroupStartTime())) {
            return Result.error("开团结束时间不能早于开始时间");
        }
        if (req.getGroupOpen() != null) {
            db.setGroupOpen(req.getGroupOpen());
        }
        if (req.getGroupStartTime() != null) {
            db.setGroupStartTime(req.getGroupStartTime());
        }
        if (req.getGroupEndTime() != null) {
            db.setGroupEndTime(req.getGroupEndTime());
        }
        db.setUpdateTime(LocalDateTime.now());
        boolean ok = productMapper.updateById(db) > 0;
        if (!ok) {
            return Result.error("开团设置失败");
        }
        return Result.success("开团设置成功");
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
        dto.setGroupOpen(product.getGroupOpen());
        dto.setGroupStartTime(product.getGroupStartTime());
        dto.setGroupEndTime(product.getGroupEndTime());
        return dto;
    }

    private Result<String> validateImagesField(String images) {
        if (!StringUtils.hasText(images)) {
            return null;
        }
        String value = images.trim();
        if (value.startsWith("data:image/")) {
            return Result.error("图片字段仅支持URL，不支持base64内容");
        }
        if (value.length() > PRODUCT_IMAGES_MAX_LENGTH) {
            return Result.error("图片URL过长，请使用短链接或图床地址");
        }
        return null;
    }
}
