package com.langkeyo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.langkeyo.common.Result;
import com.langkeyo.common.ResultCode;
import com.langkeyo.entity.Coupon;
import com.langkeyo.mapper.CouponMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/coupon")
public class CouponController {
    @Autowired
    private CouponMapper couponMapper;

    @GetMapping("/admin/list")
    public Result<List<Coupon>> adminList(@RequestParam(required = false) Integer status,
                                          @RequestParam(required = false) String keyword) {
        QueryWrapper<Coupon> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("title", keyword.trim()).or().like("code", keyword.trim()));
        }
        wrapper.orderByDesc("id");
        return Result.success(couponMapper.selectList(wrapper));
    }

    @GetMapping("/enabled")
    public Result<List<Coupon>> enabledList() {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Coupon> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
                .and(w -> w.isNull("start_time").or().le("start_time", now))
                .and(w -> w.isNull("end_time").or().ge("end_time", now))
                .orderByDesc("id");
        return Result.success(couponMapper.selectList(wrapper));
    }

    @PostMapping("/admin/create")
    public Result<Coupon> adminCreate(@RequestBody Coupon payload) {
        if (!StringUtils.hasText(payload.getCode()) || !StringUtils.hasText(payload.getTitle())) {
            return Result.error(ResultCode.PARAM_IS_BLANK);
        }
        QueryWrapper<Coupon> codeQw = new QueryWrapper<>();
        codeQw.eq("code", payload.getCode().trim());
        if (couponMapper.selectCount(codeQw) > 0) {
            return Result.error("优惠券编码已存在");
        }
        payload.setId(null);
        payload.setCode(payload.getCode().trim());
        payload.setTitle(payload.getTitle().trim());
        payload.setStatus(payload.getStatus() == null ? 1 : payload.getStatus());
        LocalDateTime now = LocalDateTime.now();
        payload.setCreateTime(now);
        payload.setUpdateTime(now);
        boolean ok = couponMapper.insert(payload) > 0;
        if (!ok) {
            return Result.error("优惠券创建失败");
        }
        return Result.success(payload);
    }

    @PutMapping("/admin/update/{id}")
    public Result<Coupon> adminUpdate(@PathVariable Long id, @RequestBody Coupon payload) {
        Coupon db = couponMapper.selectById(id);
        if (db == null) {
            return Result.error("优惠券不存在");
        }
        String newCode = StringUtils.hasText(payload.getCode()) ? payload.getCode().trim() : db.getCode();
        if (!newCode.equals(db.getCode())) {
            QueryWrapper<Coupon> codeQw = new QueryWrapper<>();
            codeQw.eq("code", newCode);
            if (couponMapper.selectCount(codeQw) > 0) {
                return Result.error("优惠券编码已存在");
            }
        }
        db.setCode(newCode);
        if (StringUtils.hasText(payload.getTitle())) {
            db.setTitle(payload.getTitle().trim());
        }
        if (payload.getDiscountAmount() != null) {
            db.setDiscountAmount(payload.getDiscountAmount());
        }
        if (payload.getMinimumSpend() != null) {
            db.setMinimumSpend(payload.getMinimumSpend());
        }
        if (payload.getStatus() != null) {
            db.setStatus(payload.getStatus());
        }
        db.setStartTime(payload.getStartTime());
        db.setEndTime(payload.getEndTime());
        db.setUpdateTime(LocalDateTime.now());
        boolean ok = couponMapper.updateById(db) > 0;
        if (!ok) {
            return Result.error("优惠券更新失败");
        }
        return Result.success(db);
    }

    @PutMapping("/admin/status/{id}")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Coupon db = couponMapper.selectById(id);
        if (db == null) {
            return Result.error("优惠券不存在");
        }
        db.setStatus(status);
        db.setUpdateTime(LocalDateTime.now());
        boolean ok = couponMapper.updateById(db) > 0;
        if (!ok) {
            return Result.error("状态更新失败");
        }
        return Result.success("状态更新成功");
    }

    @DeleteMapping("/admin/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Coupon db = couponMapper.selectById(id);
        if (db == null) {
            return Result.error("优惠券不存在");
        }
        boolean ok = couponMapper.deleteById(id) > 0;
        if (!ok) {
            return Result.error("删除失败");
        }
        return Result.success("删除成功");
    }
}
