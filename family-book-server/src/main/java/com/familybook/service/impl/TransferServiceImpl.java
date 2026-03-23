package com.familybook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.Account;
import com.familybook.entity.Transfer;
import com.familybook.mapper.TransferMapper;
import com.familybook.service.AccountService;
import com.familybook.service.TransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 转账记录服务实现类
 */
@Service
public class TransferServiceImpl extends ServiceImpl<TransferMapper, Transfer> implements TransferService {

    private final TransferMapper transferMapper;
    private final AccountService accountService;

    public TransferServiceImpl(TransferMapper transferMapper, AccountService accountService) {
        this.transferMapper = transferMapper;
        this.accountService = accountService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Transfer createTransfer(Transfer transfer) {
        Long userId = transfer.getUserId();
        Long fromAccountId = transfer.getFromAccountId();
        Long toAccountId = transfer.getToAccountId();
        BigDecimal amount = transfer.getAmount();
        BigDecimal fee = transfer.getFee() != null ? transfer.getFee() : BigDecimal.ZERO;

        // 1. 校验参数
        if (fromAccountId.equals(toAccountId)) {
            throw new RuntimeException("转出账户和转入账户不能相同");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("转账金额必须大于0");
        }

        // 2. 校验账户是否存在且属于当前用户
        Account fromAccount = accountService.getById(fromAccountId);
        Account toAccount = accountService.getById(toAccountId);

        if (fromAccount == null || !fromAccount.getUserId().equals(userId)) {
            throw new RuntimeException("转出账户不存在或无权限");
        }
        if (toAccount == null || !toAccount.getUserId().equals(userId)) {
            throw new RuntimeException("转入账户不存在或无权限");
        }

        // 3. 校验转出账户余额（转账金额 + 手续费）
        BigDecimal totalOut = amount.add(fee);
        if (fromAccount.getBalance().compareTo(totalOut) < 0) {
            throw new RuntimeException("转出账户余额不足");
        }

        // 4. 更新账户余额
        // 转出账户减少
        fromAccount.setBalance(fromAccount.getBalance().subtract(totalOut));
        accountService.updateById(fromAccount);

        // 转入账户增加
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountService.updateById(toAccount);

        // 5. 设置默认转账日期
        if (transfer.getTransferDate() == null) {
            transfer.setTransferDate(LocalDate.now());
        }

        // 6. 保存转账记录
        this.save(transfer);

        return transfer;
    }

    @Override
    public List<Transfer> getTransfers(Long userId, Long familyId) {
        LambdaQueryWrapper<Transfer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transfer::getUserId, userId)
                .orderByDesc(Transfer::getCreateTime);

        return this.list(wrapper);
    }
}
