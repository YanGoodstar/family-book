package com.familybook.service;

import com.familybook.mapper.DreamGoalMapper;
import com.familybook.service.impl.DreamGoalServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DreamGoalServiceImplUnitTest {

    @Mock
    private DreamGoalMapper dreamGoalMapper;

    @Mock
    private SavingsRecordService savingsRecordService;

    @InjectMocks
    private DreamGoalServiceImpl service;

    @Test
    void getCommittedSavingsShouldReturnZeroWhenMapperReturnsNull() {
        when(dreamGoalMapper.sumCommittedSavingsByUserId(7L)).thenReturn(null);

        assertThat(service.getCommittedSavings(7L)).isEqualByComparingTo("0");
    }

    @Test
    void getCommittedSavingsShouldReturnMapperAggregateValue() {
        when(dreamGoalMapper.sumCommittedSavingsByUserId(7L))
                .thenReturn(new BigDecimal("1800.00"));

        assertThat(service.getCommittedSavings(7L)).isEqualByComparingTo("1800.00");
    }

    @Test
    void archiveGoalShouldMarkCompletedArchiveWhenGoalAlreadyReachedTarget() {
        DreamGoalServiceImpl spyService = spy(service);
        com.familybook.entity.DreamGoal goal = new com.familybook.entity.DreamGoal();
        goal.setId(11L);
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setSavedAmount(new BigDecimal("5000.00"));
        goal.setGoalStatus(1);

        doReturn(goal).when(spyService).getById(11L);
        doReturn(true).when(spyService).updateById(goal);

        com.familybook.entity.DreamGoal archived = spyService.archiveGoal(11L);

        assertThat(archived.getGoalStatus()).isEqualTo(2);
    }

    @Test
    void archiveGoalShouldMarkStoppedArchiveWhenGoalIsNotCompleted() {
        DreamGoalServiceImpl spyService = spy(service);
        com.familybook.entity.DreamGoal goal = new com.familybook.entity.DreamGoal();
        goal.setId(12L);
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setSavedAmount(new BigDecimal("1800.00"));
        goal.setGoalStatus(1);

        doReturn(goal).when(spyService).getById(12L);
        doReturn(true).when(spyService).updateById(goal);

        com.familybook.entity.DreamGoal archived = spyService.archiveGoal(12L);

        assertThat(archived.getGoalStatus()).isEqualTo(3);
    }

    @Test
    void saveAmountShouldRejectArchivedGoal() {
        DreamGoalServiceImpl spyService = spy(service);
        com.familybook.entity.DreamGoal goal = new com.familybook.entity.DreamGoal();
        goal.setId(13L);
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setSavedAmount(new BigDecimal("1800.00"));
        goal.setGoalStatus(2);

        doReturn(goal).when(spyService).getById(13L);

        assertThatThrownBy(() -> spyService.saveAmount(13L, new BigDecimal("200.00"), "test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("已归档");
    }
}
