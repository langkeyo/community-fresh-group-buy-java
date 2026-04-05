package com.langkeyo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.langkeyo.common.Result;
import com.langkeyo.dto.PickPointItemDTO;
import com.langkeyo.entity.PickPoint;
import com.langkeyo.mapper.PickPointMapper;
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
                .map(item -> new PickPointItemDTO(item.getId(), item.getName(), item.getAddress()))
                .collect(Collectors.toList());
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<PickPointItemDTO> detail(@PathVariable Long id) {
        PickPoint point = pickPointMapper.selectById(id);
        if (point == null) {
            return Result.error("自提点不存在");
        }
        return Result.success(new PickPointItemDTO(point.getId(), point.getName(), point.getAddress()));
    }
}
