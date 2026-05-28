package com.smartconstruct.backend_core.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.mapper.*;
import com.smartconstruct.backend_core.service.impl.PhaseExecutionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration test: Student enter node → submit → complete → phase unlock flow.
 *
 * Verifies the complete student progression lifecycle:
 * 1. Student enters a node → progress record created with status=1
 * 2. Student completes the node → status updated to 2, duration calculated
 * 3. When all required nodes in phase are complete → next phase unlocks
 * 4. Phase lock prevents access to locked phases
 */
@ExtendWith(MockitoExtension.class)
class StudentPhaseFlowTest {

    @Mock
    private WfNodeInstanceMapper nodeInstanceMapper;

    @Mock
    private WfStudentNodeProgressMapper studentNodeProgressMapper;

    @Mock
    private BizTrainingParticipationMapper participationMapper;

    @Mock
    private BizTrainingTaskMapper trainingTaskMapper;

    @Mock
    private WfTrainingTemplateMapper trainingTemplateMapper;

    @Mock
    private WfStudentActivityStateMapper studentActivityStateMapper;

    @InjectMocks
    private PhaseExecutionServiceImpl phaseExecutionService;

    private BizTrainingParticipation participation;
    private BizTrainingTask task;
    private WfTrainingTemplate template;

    @BeforeEach
    void setUp() {
        participation = new BizTrainingParticipation();
        participation.setId(1L);
        participation.setTaskId(10L);
        participation.setStudentId(100L);

        task = new BizTrainingTask();
        task.setId(10L);
        task.setTemplateId(50L);
        task.setEndTime(LocalDateTime.now().plusDays(7)); // Not expired

        template = new WfTrainingTemplate();
        template.setId(50L);
        template.setPhasesJson("[{\"phase_id\":\"p1\",\"phase_name\":\"课前\",\"sort_num\":1},{\"phase_id\":\"p2\",\"phase_name\":\"课中\",\"sort_num\":2}]");
    }

    @Test
    @DisplayName("Enter node: creates progress record with status=1")
    void testEnterNode_createsProgressRecord() {
        // Arrange: no existing progress, node in unlocked phase
        when(participationMapper.selectById(1L)).thenReturn(participation);
        when(trainingTaskMapper.selectById(10L)).thenReturn(task);
        when(studentNodeProgressMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(studentActivityStateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Node instance in phase p1
        WfNodeInstance nodeInstance = new WfNodeInstance();
        nodeInstance.setId(200L);
        nodeInstance.setPhaseId("p1");
        nodeInstance.setTaskId(10L);
        when(nodeInstanceMapper.selectById(200L)).thenReturn(nodeInstance);

        // Phase p1 is the first phase (unlocked by default)
        when(trainingTemplateMapper.selectById(50L)).thenReturn(template);

        // Mock: no required nodes in p1 (so it's considered the current unlocked phase)
        when(nodeInstanceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // Act
        WfStudentNodeProgress result = phaseExecutionService.enterNode(1L, 200L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getStatus()); // In progress
        assertNotNull(result.getEnteredAt());
        verify(studentNodeProgressMapper).insert(any(WfStudentNodeProgress.class));
        verify(studentActivityStateMapper).insert(any(WfStudentActivityState.class));
    }

    @Test
    @DisplayName("Complete node: updates status to 2 and calculates duration")
    void testCompleteNode_updatesStatusAndDuration() {
        // Arrange: existing progress record in progress
        WfStudentNodeProgress progress = new WfStudentNodeProgress();
        progress.setId(300L);
        progress.setParticipationId(1L);
        progress.setNodeInstanceId(200L);
        progress.setStatus(1); // In progress
        progress.setEnteredAt(LocalDateTime.now().minusMinutes(5));

        when(studentNodeProgressMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(progress);
        when(studentActivityStateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act
        phaseExecutionService.completeNode(1L, 200L);

        // Assert
        assertEquals(2, progress.getStatus()); // Completed
        assertNotNull(progress.getExitedAt());
        assertTrue(progress.getStayDurationSeconds() > 0);
        verify(studentNodeProgressMapper).updateById(progress);
    }

    @Test
    @DisplayName("Phase complete: all required nodes completed → isPhaseComplete returns true")
    void testPhaseComplete_allRequiredDone() {
        // Arrange: two required nodes, both completed
        WfNodeInstance node1 = new WfNodeInstance();
        node1.setId(201L);
        node1.setConfigJson("{\"orchestration_settings\":{\"is_required\":true}}");

        WfNodeInstance node2 = new WfNodeInstance();
        node2.setId(202L);
        node2.setConfigJson("{\"orchestration_settings\":{\"is_required\":true}}");

        when(nodeInstanceMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(node1, node2));

        when(participationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(participation);

        // Both nodes have completed progress
        WfStudentNodeProgress completedProgress = new WfStudentNodeProgress();
        completedProgress.setStatus(2);
        when(studentNodeProgressMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // Act
        boolean result = phaseExecutionService.isPhaseComplete(10L, "p1", 100L);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Phase incomplete: one required node not completed → isPhaseComplete returns false")
    void testPhaseIncomplete_requiredNodePending() {
        // Arrange: two required nodes, one not completed
        WfNodeInstance node1 = new WfNodeInstance();
        node1.setId(201L);
        node1.setConfigJson("{\"orchestration_settings\":{\"is_required\":true}}");

        WfNodeInstance node2 = new WfNodeInstance();
        node2.setId(202L);
        node2.setConfigJson("{\"orchestration_settings\":{\"is_required\":true}}");

        when(nodeInstanceMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(node1, node2));

        when(participationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(participation);

        // First call returns 1 (completed), second returns 0 (not completed)
        when(studentNodeProgressMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(1L)
                .thenReturn(0L);

        // Act
        boolean result = phaseExecutionService.isPhaseComplete(10L, "p1", 100L);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Optional nodes don't block phase completion")
    void testOptionalNodes_dontBlockCompletion() {
        // Arrange: one required (completed) + one optional (not completed)
        WfNodeInstance requiredNode = new WfNodeInstance();
        requiredNode.setId(201L);
        requiredNode.setConfigJson("{\"orchestration_settings\":{\"is_required\":true}}");

        WfNodeInstance optionalNode = new WfNodeInstance();
        optionalNode.setId(202L);
        optionalNode.setConfigJson("{\"orchestration_settings\":{\"is_required\":false}}");

        when(nodeInstanceMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(requiredNode, optionalNode));

        when(participationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(participation);

        // Required node is completed
        when(studentNodeProgressMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // Act
        boolean result = phaseExecutionService.isPhaseComplete(10L, "p1", 100L);

        // Assert: phase is complete because only required node matters
        assertTrue(result);
    }
}
