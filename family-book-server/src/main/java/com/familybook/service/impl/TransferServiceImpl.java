package com.familybook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.Transfer;
import com.familybook.mapper.TransferMapper;
import com.familybook.service.TransferService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 转账记录服务实现类
 */
@Service
public class TransferServiceImpl extends ServiceImpl<TransferMapper, Transfer> implements TransferService {

    private final TransferMapper transferMapper;

    public TransferServiceImpl(TransferMapper transferMapper) {
        this.transferMapper = transferMapper;
    }

    @Override
    public Transfer createTransfer(Transfer transfer) {
        return null;
    }

    @Override
    public List<Transfer> getTransfers(Long userId, Long familyId) {
        return null;
    }
}
