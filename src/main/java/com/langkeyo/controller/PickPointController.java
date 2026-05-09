package com.langkeyo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.langkeyo.common.Result;
import com.langkeyo.common.ResultCode;
import com.langkeyo.dto.PickPointItemDTO;
import com.langkeyo.entity.PickPoint;
import com.langkeyo.mapper.PickPointMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pick-point")
public class PickPointController {
    @Autowired
    private PickPointMapper pickPointMapper;

    @GetMapping("/list")
    public Result<List<PickPointItemDTO>> list(@RequestParam(required = false) String keyword) {
        QueryWrapper<PickPoint> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String q = keyword.trim();
            wrapper.like("name", q).or().like("address", q);
        }
        List<PickPoint> points = pickPointMapper.selectList(wrapper);
        List<PickPointItemDTO> data = points.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<PickPointItemDTO> detail(@PathVariable Long id) {
        PickPoint point = pickPointMapper.selectById(id);
        if (point == null) {
            return Result.error("自提点不存在");
        }
        return Result.success(toDTO(point));
    }

    @PostMapping("/admin/create")
    public Result<String> create(@RequestBody PickPoint payload) {
        if (!StringUtils.hasText(payload.getName())
                || !StringUtils.hasText(payload.getAddress())
                || !StringUtils.hasText(payload.getLeaderName())
                || !StringUtils.hasText(payload.getPhone())) {
            return Result.error(ResultCode.PARAM_IS_BLANK);
        }
        PickPoint row = new PickPoint();
        row.setId(null);
        row.setName(payload.getName().trim());
        row.setAddress(payload.getAddress().trim());
        row.setLeaderName(payload.getLeaderName().trim());
        row.setPhone(payload.getPhone().trim());
        row.setLatitude(payload.getLatitude());
        row.setLongitude(payload.getLongitude());
        row.setDeleted(0);
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        boolean ok = pickPointMapper.insert(row) > 0;
        if (!ok) {
            return Result.error("提货点创建失败");
        }
        return Result.success("提货点创建成功");
    }

    private PickPointItemDTO toDTO(PickPoint point) {
        PickPointItemDTO dto = new PickPointItemDTO();
        dto.setId(point.getId());
        dto.setName(point.getName());
        dto.setAddress(point.getAddress());
        dto.setLeaderName(point.getLeaderName());
        dto.setPhone(point.getPhone());
        dto.setLatitude(point.getLatitude());
        dto.setLongitude(point.getLongitude());
        dto.setCreateTime(point.getCreateTime());
        dto.setUpdateTime(point.getUpdateTime());
        dto.setDeleted(point.getDeleted());
        return dto;
    }
}
