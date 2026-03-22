package com.familybook.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账户请求DTO
 * 用于创建和更新账户
 */
@Data
@Schema(description = "账户请求对象")
public class AccountRequest {

    @Schema(description = "账户ID（创建时可为空，更新时必填）")
    private Long id;

    @NotBlank(message = "账户名称不能为空")
    @Schema(description = "账户名称", required = true, example = "招商银行")
    private String name;

    @NotNull(message = "账户类型不能为空")
    @Schema(description = "账户类型：1-现金，2-银行卡，3-支付宝，4-微信，5-其他", required = true, example = "2")
    private Integer type;

    @NotNull(message = "初始余额不能为空")
    @Schema(description = "账户余额", required = true, example = "10000.00")
    private BigDecimal balance;

    @Schema(description = "账户图标", example = "bank")
    private String icon;

    @Schema(description = "排序序号，数值越小越靠前", example = "1")
    private Integer sort;
}
