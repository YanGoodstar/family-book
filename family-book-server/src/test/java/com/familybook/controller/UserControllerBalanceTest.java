package com.familybook.controller;

import com.familybook.common.Result;
import com.familybook.dto.request.BalanceRequest;
import com.familybook.entity.User;
import com.familybook.service.DreamGoalService;
import com.familybook.service.UserService;
import com.familybook.vo.BalanceVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerBalanceTest {

    private final UserService userService = mock(UserService.class);
    private final DreamGoalService dreamGoalService = mock(DreamGoalService.class);

    private final UserController controller = new UserController(userService, dreamGoalService);

    @Test
    void loggedInUserReturnsBalanceWithDreamGoalMetrics() {
        User user = new User();
        user.setId(42L);
        user.setInitialBalance(new BigDecimal("100.00"));

        when(userService.getCurrentUser()).thenReturn(user);
        when(userService.calculateBalance(42L)).thenReturn(new BigDecimal("600.00"));
        when(dreamGoalService.getCommittedSavings(42L)).thenReturn(new BigDecimal("200.00"));

        Result<BalanceVO> result = controller.getBalance();
        BalanceVO vo = result.getData();

        assertThat(vo.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(vo.getCommittedSavings()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(vo.getSpendableBalance()).isEqualByComparingTo(new BigDecimal("400.00"));
        assertThat(vo.isOverCommitted()).isFalse();
    }

    @Test
    void anonymousUserReturnsZeros() {
        when(userService.getCurrentUser()).thenReturn(null);

        Result<BalanceVO> result = controller.getBalance();
        BalanceVO vo = result.getData();

        assertThat(vo.getInitialBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(vo.getCurrentBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(vo.getCommittedSavings()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(vo.getSpendableBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(vo.isOverCommitted()).isFalse();
    }

    @Test
    void loggedInUserReturnsOverCommittedWhenCommittedSavingsExceedsBalance() {
        User user = new User();
        user.setId(7L);
        user.setInitialBalance(new BigDecimal("500.00"));

        when(userService.getCurrentUser()).thenReturn(user);
        when(userService.calculateBalance(7L)).thenReturn(new BigDecimal("800.00"));
        when(dreamGoalService.getCommittedSavings(7L)).thenReturn(new BigDecimal("1200.00"));

        Result<BalanceVO> result = controller.getBalance();
        BalanceVO vo = result.getData();

        assertThat(vo.getSpendableBalance()).isEqualByComparingTo(new BigDecimal("-400.00"));
        assertThat(vo.isOverCommitted()).isTrue();
    }

    @Test
    void setBalanceReturnsUpdatedCommitmentMetrics() {
        User user = new User();
        user.setId(11L);

        BalanceRequest request = new BalanceRequest();
        request.setInitialBalance(new BigDecimal("1500.00"));

        when(userService.getCurrentUser()).thenReturn(user);
        when(userService.calculateBalance(11L)).thenReturn(new BigDecimal("2200.00"));
        when(dreamGoalService.getCommittedSavings(11L)).thenReturn(new BigDecimal("700.00"));

        Result<BalanceVO> result = controller.setBalance(request);
        BalanceVO vo = result.getData();

        verify(userService).setInitialBalance(11L, new BigDecimal("1500.00"));
        assertThat(vo.getInitialBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(vo.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("2200.00"));
        assertThat(vo.getCommittedSavings()).isEqualByComparingTo(new BigDecimal("700.00"));
        assertThat(vo.getSpendableBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(vo.isOverCommitted()).isFalse();
    }
}
