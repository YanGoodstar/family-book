package com.familybook.controller;

import com.familybook.common.Result;
import com.familybook.dto.request.DreamGoalSaveRequest;
import com.familybook.entity.DreamGoal;
import com.familybook.service.DreamGoalService;
import com.familybook.service.SavingsRecordService;
import com.familybook.vo.DreamGoalDashboardVO;
import com.familybook.vo.DreamGoalVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DreamGoalControllerTest {

    private final DreamGoalService dreamGoalService = mock(DreamGoalService.class);
    private final SavingsRecordService savingsRecordService = mock(SavingsRecordService.class);
    private final DreamGoalController controller = new DreamGoalController(
            dreamGoalService,
            savingsRecordService
    );

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        reset(dreamGoalService, savingsRecordService);
    }

    @Test
    void convertToVoShouldSerializeIdAsStringForJavaScriptClients() throws Exception {
        DreamGoal goal = new DreamGoal();
        goal.setId(9007199254740993L);
        goal.setName("买相机");
        goal.setTargetAmount(new BigDecimal("10000.00"));
        goal.setSavedAmount(BigDecimal.ZERO);

        DreamGoalVO vo = invokeConvertToVO(goal);
        String json = new ObjectMapper().writeValueAsString(vo);

        assertThat(json).contains("\"id\":\"9007199254740993\"");
    }

    @Test
    void convertToVoShouldKeepActiveStatusEvenWhenTargetAmountAlreadyReached() throws Exception {
        DreamGoal goal = new DreamGoal();
        goal.setId(1L);
        goal.setGoalStatus(1);
        goal.setStatus(1);
        goal.setName("买相机");
        goal.setTargetAmount(new BigDecimal("100.00"));
        goal.setSavedAmount(new BigDecimal("100.00"));

        DreamGoalVO vo = invokeConvertToVO(goal);

        assertThat(vo.getCompleted()).isTrue();
        assertThat(vo.getGoalStatus()).isEqualTo(1);
        assertThat(vo.getStatus()).isEqualTo(1);
    }

    @Test
    void convertToVoShouldExposeStoppedArchiveStatusWithoutPretendingGoalIsCompleted() throws Exception {
        DreamGoal goal = new DreamGoal();
        goal.setId(2L);
        goal.setGoalStatus(3);
        goal.setStatus(1);
        goal.setName("旅行");
        goal.setTargetAmount(new BigDecimal("1000.00"));
        goal.setSavedAmount(new BigDecimal("300.00"));

        DreamGoalVO vo = invokeConvertToVO(goal);

        assertThat(vo.getCompleted()).isFalse();
        assertThat(vo.getGoalStatus()).isEqualTo(3);
        assertThat(vo.getStatus()).isEqualTo(1);
    }

    @Test
    void saveAmountShouldRejectArchivedGoalBeforeCallingService() {
        loginAs(7L);
        DreamGoal goal = createGoal(9L, 7L, 2, "1000.00", "500.00");
        DreamGoalSaveRequest request = new DreamGoalSaveRequest();
        request.setAmount(new BigDecimal("100.00"));

        when(dreamGoalService.getById(9L)).thenReturn(goal);

        Result<DreamGoalDashboardVO> result = controller.saveAmount(9L, request);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("已归档");
        verify(dreamGoalService, never()).saveAmount(anyLong(), any(), any());
    }

    @Test
    void saveAmountShouldRejectCompletedGoalBeforeCallingService() {
        loginAs(7L);
        DreamGoal goal = createGoal(10L, 7L, 1, "1000.00", "1000.00");
        DreamGoalSaveRequest request = new DreamGoalSaveRequest();
        request.setAmount(new BigDecimal("50.00"));

        when(dreamGoalService.getById(10L)).thenReturn(goal);

        Result<DreamGoalDashboardVO> result = controller.saveAmount(10L, request);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("已完成");
        verify(dreamGoalService, never()).saveAmount(anyLong(), any(), any());
    }

    @Test
    void archiveShouldRejectArchivedGoalBeforeCallingService() {
        loginAs(7L);
        DreamGoal goal = createGoal(11L, 7L, 3, "1000.00", "300.00");

        when(dreamGoalService.getById(11L)).thenReturn(goal);

        Result<DreamGoalDashboardVO> result = controller.archive(11L);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("已归档");
        verify(dreamGoalService, never()).archiveGoal(anyLong());
    }

    private DreamGoalVO invokeConvertToVO(DreamGoal goal) throws Exception {
        Method method = DreamGoalController.class.getDeclaredMethod("convertToVO", DreamGoal.class);
        method.setAccessible(true);
        return (DreamGoalVO) method.invoke(controller, goal);
    }

    private DreamGoal createGoal(Long id, Long userId, Integer goalStatus, String targetAmount, String savedAmount) {
        DreamGoal goal = new DreamGoal();
        goal.setId(id);
        goal.setUserId(userId);
        goal.setStatus(1);
        goal.setGoalStatus(goalStatus);
        goal.setTargetAmount(new BigDecimal(targetAmount));
        goal.setSavedAmount(new BigDecimal(savedAmount));
        return goal;
    }

    private void loginAs(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList())
        );
    }
}
