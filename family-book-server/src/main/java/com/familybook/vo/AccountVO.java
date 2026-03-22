package com.familybook.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账户视图对象
 * 用于返回账户详情给前端
 */
@Data
@Schema(description = "账户视图对象")
public class AccountVO {

    @Schema(description = "账户ID", example = "1001")
    private Long id;

    @Schema(description = "账户名称", example = "招商银行")
    private String name;

    @Schema(description = "账户类型：1-现金，2-银行卡，3-支付宝，4-微信，5-其他", example = "2")
    private Integer type;

    @Schema(description = "账户余额", example = "10000.00")
    private BigDecimal balance;

    @Schema(description = "账户图标", example = "bank")
    private String icon;

    @Schema(description = "排序序号", example = "1")
    private Integer sort;

    @Schema(description = "创建时间", example = "2024-01-15 10:30:00")
    private String createTime;
}
