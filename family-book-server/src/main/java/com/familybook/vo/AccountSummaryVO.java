package com.familybook.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账户汇总视图对象
 * 用于展示用户的资产概况
 */
@Data
@Schema(description = "账户汇总视图对象")
public class AccountSummaryVO {

    @Schema(description = "总资产（所有余额为正账户之和）", example = "50000.00")
    private BigDecimal totalAssets;

    @Schema(description = "总负债（所有余额为负账户之和，如信用卡欠款）", example = "5000.00")
    private BigDecimal totalLiabilities;

    @Schema(description = "净资产（总资产 - 总负债）", example = "45000.00")
    private BigDecimal netAssets;
}
