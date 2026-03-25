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
}
