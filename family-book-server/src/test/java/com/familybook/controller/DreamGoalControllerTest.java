package com.familybook.controller;

import com.familybook.entity.DreamGoal;
import com.familybook.service.DreamGoalService;
import com.familybook.service.SavingsRecordService;
import com.familybook.vo.DreamGoalVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DreamGoalControllerTest {

    private final DreamGoalController controller = new DreamGoalController(
            mock(DreamGoalService.class),
            mock(SavingsRecordService.class)
    );

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
    void convertToVoShouldDeriveBusinessStatusFromProgressInsteadOfEntityStatus() throws Exception {
        DreamGoal goal = new DreamGoal();
        goal.setId(1L);
        goal.setStatus(1);
        goal.setName("买相机");
        goal.setTargetAmount(new BigDecimal("100.00"));
        goal.setSavedAmount(new BigDecimal("100.00"));

        DreamGoalVO vo = invokeConvertToVO(goal);

        assertThat(vo.getCompleted()).isTrue();
        assertThat(vo.getStatus()).isEqualTo(2);
    }

    private DreamGoalVO invokeConvertToVO(DreamGoal goal) throws Exception {
        Method method = DreamGoalController.class.getDeclaredMethod("convertToVO", DreamGoal.class);
        method.setAccessible(true);
        return (DreamGoalVO) method.invoke(controller, goal);
    }
}
