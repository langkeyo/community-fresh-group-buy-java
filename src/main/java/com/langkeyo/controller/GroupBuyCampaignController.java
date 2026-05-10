package com.langkeyo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.langkeyo.common.Result;
import com.langkeyo.common.ResultCode;
import com.langkeyo.entity.GroupBuyCampaign;
import com.langkeyo.mapper.GroupBuyCampaignMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/group-buy")
public class GroupBuyCampaignController {
    private final GroupBuyCampaignMapper campaignMapper;

    public GroupBuyCampaignController(GroupBuyCampaignMapper campaignMapper) {
        this.campaignMapper = campaignMapper;
    }

    @GetMapping("/list")
    public Result<List<GroupBuyCampaign>> listActive(@RequestParam(required = false) Long productId,
                                                     @RequestParam(required = false) Long pickPointId) {
        QueryWrapper<GroupBuyCampaign> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0).eq("status", 1);
        if (productId != null) wrapper.eq("product_id", productId);
        if (pickPointId != null) wrapper.eq("pick_point_id", pickPointId);
        wrapper.le("start_time", LocalDateTime.now()).ge("end_time", LocalDateTime.now()).orderByDesc("id");
        return Result.success(campaignMapper.selectList(wrapper));
    }

    @GetMapping("/admin/list")
    public Result<List<GroupBuyCampaign>> adminList(@RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) String keyword) {
        QueryWrapper<GroupBuyCampaign> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        if (status != null) wrapper.eq("status", status);
        if (StringUtils.hasText(keyword)) wrapper.like("title", keyword.trim());
        wrapper.orderByDesc("id");
        return Result.success(campaignMapper.selectList(wrapper));
    }

    @PostMapping("/admin/create")
    public Result<String> create(@RequestBody GroupBuyCampaign payload) {
        if (!StringUtils.hasText(payload.getTitle()) || payload.getProductId() == null
                || payload.getGroupTarget() == null || payload.getStartTime() == null || payload.getEndTime() == null) {
            return Result.error(ResultCode.PARAM_IS_BLANK);
        }
        if (payload.getEndTime().isBefore(payload.getStartTime())) {
            return Result.error("活动结束时间不能早于开始时间");
        }
        payload.setId(null);
        payload.setStatus(payload.getStatus() == null ? 1 : payload.getStatus());
        payload.setDeleted(0);
        payload.setCreateTime(LocalDateTime.now());
        payload.setUpdateTime(LocalDateTime.now());
        boolean ok = campaignMapper.insert(payload) > 0;
        return ok ? Result.success("团购活动创建成功") : Result.error("团购活动创建失败");
    }

    @PutMapping("/admin/update/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody GroupBuyCampaign payload) {
        GroupBuyCampaign db = campaignMapper.selectById(id);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error("活动不存在");
        }
        payload.setId(id);
        payload.setDeleted(db.getDeleted());
        payload.setCreateTime(db.getCreateTime());
        payload.setUpdateTime(LocalDateTime.now());
        boolean ok = campaignMapper.updateById(payload) > 0;
        return ok ? Result.success("团购活动更新成功") : Result.error("团购活动更新失败");
    }

    @PutMapping("/admin/status/{id}")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        GroupBuyCampaign db = campaignMapper.selectById(id);
        if (db == null || (db.getDeleted() != null && db.getDeleted() == 1)) {
            return Result.error("活动不存在");
        }
        db.setStatus(status);
        db.setUpdateTime(LocalDateTime.now());
        boolean ok = campaignMapper.updateById(db) > 0;
        return ok ? Result.success("团购活动状态更新成功") : Result.error("团购活动状态更新失败");
    }
}
