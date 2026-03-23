package com.familybook.controller;

import com.familybook.common.Result;
import com.familybook.dto.request.TransferRequest;
import com.familybook.entity.Transfer;
import com.familybook.security.SecurityUtils;
import com.familybook.service.TransferService;
import com.familybook.vo.PageVO;
import com.familybook.vo.TransferVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "转账管理", description = "账户间转账、转账记录查询")
@RestController
@RequestMapping("/api/v1/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @Operation(summary = "创建转账", description = "在两个账户之间进行转账，自动更新账户余额")
    @PostMapping
    public Result<TransferVO> create(@RequestBody TransferRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Transfer transfer = new Transfer();
        BeanUtils.copyProperties(request, transfer);
        transfer.setUserId(userId);

        Transfer saved = transferService.createTransfer(transfer);

        TransferVO vo = new TransferVO();
        BeanUtils.copyProperties(saved, vo);
        return Result.success(vo);
    }

    @Operation(summary = "获取转账记录列表", description = "查询转账记录，支持分页")
    @GetMapping("/list")
    public Result<PageVO<TransferVO>> list(
            @RequestParam(required = false) Long familyId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = SecurityUtils.getCurrentUserId();

        List<Transfer> list = transferService.getTransfers(userId, familyId);

        // 分页处理
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Transfer> pageList = start < total ? list.subList(start, end) : List.of();

        List<TransferVO> voList = pageList.stream()
                .map(t -> {
                    TransferVO vo = new TransferVO();
                    BeanUtils.copyProperties(t, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        PageVO<TransferVO> pageVO = PageVO.of(pageNum, pageSize, (long) total, (total + pageSize - 1) / pageSize, voList);
        return Result.success(pageVO);
    }

    @Operation(summary = "获取转账详情", description = "根据ID获取转账详情")
    @GetMapping("/{id}")
    public Result<TransferVO> getById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        Transfer transfer = transferService.getById(id);
        if (transfer == null) {
            return Result.error("转账记录不存在");
        }

        // 检查权限
        if (!transfer.getUserId().equals(userId)) {
            return Result.error("无权查看此记录");
        }

        TransferVO vo = new TransferVO();
        BeanUtils.copyProperties(transfer, vo);
        return Result.success(vo);
    }

    @Operation(summary = "删除转账记录", description = "删除转账记录（不会回滚账户余额）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        Transfer transfer = transferService.getById(id);
        if (transfer == null) {
            return Result.error("转账记录不存在");
        }

        if (!transfer.getUserId().equals(userId)) {
            return Result.error("无权删除此记录");
        }

        transferService.removeById(id);
        return Result.success();
    }
}
