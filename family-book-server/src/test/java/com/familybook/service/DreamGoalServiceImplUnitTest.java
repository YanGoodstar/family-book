package com.familybook.service;

import com.familybook.entity.Category;
import com.familybook.entity.Transaction;
import com.familybook.mapper.CategoryMapper;
import com.familybook.mapper.DreamGoalMapper;
import com.familybook.service.impl.DreamGoalServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DreamGoalServiceImplUnitTest {

    @Mock
    private DreamGoalMapper dreamGoalMapper;

    @Mock
    private SavingsRecordService savingsRecordService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private CategoryMapper categoryMapper;

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

        com.familybook.entity.DreamGoal archived = spyService.archiveGoal(11L, false, null);

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

        com.familybook.entity.DreamGoal archived = spyService.archiveGoal(12L, false, null);

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

    @Test
    void archiveGoalShouldRejectExpenseCreationWhenSavedAmountIsZero() {
        DreamGoalServiceImpl spyService = spy(service);
        com.familybook.entity.DreamGoal goal = new com.familybook.entity.DreamGoal();
        goal.setId(14L);
        goal.setUserId(7L);
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setSavedAmount(BigDecimal.ZERO);
        goal.setGoalStatus(1);

        doReturn(goal).when(spyService).getById(14L);

        assertThatThrownBy(() -> spyService.archiveGoal(14L, true, 88L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("当前已存金额为0");
    }

    @Test
    void archiveGoalShouldRejectInvalidExpenseCategoryType() {
        DreamGoalServiceImpl spyService = spy(service);
        com.familybook.entity.DreamGoal goal = new com.familybook.entity.DreamGoal();
        goal.setId(15L);
        goal.setUserId(7L);
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setSavedAmount(new BigDecimal("600.00"));
        goal.setGoalStatus(1);

        Category category = new Category();
        category.setId(66L);
        category.setUserId(7L);
        category.setType(2);

        doReturn(goal).when(spyService).getById(15L);
        when(categoryMapper.selectById(66L)).thenReturn(category);

        assertThatThrownBy(() -> spyService.archiveGoal(15L, true, 66L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("请选择有效的支出分类");
    }

    @Test
    void archiveGoalShouldCreateExpenseAndArchiveGoalInOneFlow() {
        DreamGoalServiceImpl spyService = spy(service);
        com.familybook.entity.DreamGoal goal = new com.familybook.entity.DreamGoal();
        goal.setId(16L);
        goal.setUserId(7L);
        goal.setName("北海道旅行");
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setSavedAmount(new BigDecimal("1800.00"));
        goal.setGoalStatus(1);

        Category category = new Category();
        category.setId(88L);
        category.setUserId(7L);
        category.setType(1);

        doReturn(goal).when(spyService).getById(16L);
        doReturn(true).when(spyService).updateById(goal);
        when(categoryMapper.selectById(88L)).thenReturn(category);
        when(transactionService.record(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.familybook.entity.DreamGoal archived = spyService.archiveGoal(16L, true, 88L);
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);

        verify(transactionService).record(transactionCaptor.capture());

        Transaction transaction = transactionCaptor.getValue();
        assertThat(transaction.getUserId()).isEqualTo(7L);
        assertThat(transaction.getCategoryId()).isEqualTo(88L);
        assertThat(transaction.getType()).isEqualTo(1);
        assertThat(transaction.getAmount()).isEqualByComparingTo("1800.00");
        assertThat(transaction.getRemark()).contains("北海道旅行");
        assertThat(transaction.getTransactionDate()).isNotNull();
        assertThat(transaction.getTransactionTime()).isNotNull();
        assertThat(archived.getGoalStatus()).isEqualTo(3);
    }
}
