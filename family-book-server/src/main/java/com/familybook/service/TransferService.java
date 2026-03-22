package com.familybook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.familybook.entity.Transfer;

import java.util.List;

/**
 * 转账记录服务接口
 */
public interface TransferService extends IService<Transfer> {

    /**
     * 创建转账
     */
    Transfer createTransfer(Transfer transfer);

    /**
     * 获取转账记录列表
     */
    List<Transfer> getTransfers(Long userId, Long familyId);
}
