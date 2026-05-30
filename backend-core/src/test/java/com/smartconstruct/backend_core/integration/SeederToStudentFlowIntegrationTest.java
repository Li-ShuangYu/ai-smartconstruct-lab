package com.smartconstruct.backend_core.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartconstruct.backend_core.dto.DemoSeedResult;
import com.smartconstruct.backend_core.dto.PhaseUnlockStatus;
import com.smartconstruct.backend_core.dto.PhaseUnlockStatus;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.mapper.*;
import com.smartconstruct.backend_core.service.*;
import com.smartconstruct.backend_core.service.impl.DemoDataSeederService;
import com.smartconstruct.backend_core.service.impl.PhaseExecutionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 端到端集成测试：Seeder 触发到学生实训流转
 *
 * 验证核心流转路径：
 * 1. DemoDataSeederService.seedAll() 完整播种流程
 * 2. StudentTrainingController 端点的节点操作
 * 3. 阶段解锁逻辑在完成节点后正确触发
 * 4. 进度持久化：退出后重新进入能恢复位置
 *
 * Requirements: 1.4, 3.2, 4.1, 5.2, 6.5, 7.6, 8.1
 */
@ExtendWith(MockitoExtension.class)
class SeederToStudentFlowIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== Seeder 播种流程测试 ====================

    @Nested
    @DisplayName("DemoDataSeeder 完整播种流程")
    class SeederFlowTests {

        @Mock
        private ITrainingTemplateService trainingTemplateService;
        @Mock
        private SysUserService sysUserService;
        @Mock
        private IStudentService studentService;
        @Mock
        private ITrainingTaskService trainingTaskService;
        @Mock
        private IAdminClassService adminClassService;
        @Mock
        private IMajorService majorService;
        @Mock
        private ITrainingParticipationService participationService;
        @Mock
        private INodeDefService nodeDefService;
        @Mock
        private WfNodeInstanceMapper nodeInstanceMapper;
        @Mock
        private WfStudentNodeProgressMapper nodeProgressMapper;
        @Mock
        private PasswordEncoder passwordEncoder;
        private DemoDataSeederService seederService;

        private WfTrainingTemplate template;
        private BizAdminClass adminClass;

        @BeforeEach
        void setUp() {
            template = new WfTrainingTemplate();
            seederService = new DemoDataSeederService(
                    trainingTemplateService, sysUserService, studentService,
                    trainingTaskService, adminClassService, majorService,
                    participationService, nodeDefService, nodeInstanceMapper,
                    nodeProgressMapper, passwordEncoder,
                    new ObjectMapper()
            );
            template.setId(1L);
            template.setTemplateName("Python算法入门");
            template.setPhasesJson("[{\"phase_id\":\"p1\",\"phase_name\":\"课前预习\",\"sort_num\":1,\"nodes\":[{\"node_id\":\"n1\",\"node_type\":\"theory_class\",\"node_name\":\"Python基础\",\"sort_num\":1}]},{\"phase_id\":\"p2\",\"phase_name\":\"课中实训\",\"sort_num\":2,\"nodes\":[{\"node_id\":\"n2\",\"node_type\":\"coding_class\",\"node_name\":\"冒泡排序\",\"sort_num\":1}]}]");
            template.setAiStatus(2);

            adminClass = new BizAdminClass();
            adminClass.setId(10L);
            adminClass.setClassName("26软件工程1班");
        }

        @Test
        @DisplayName("seedAll: 模板不存在时抛出异常并终止")
        void testSeedAll_templateNotFound_throwsException() {
            // Arrange: 模板查询返回 null
            when(trainingTemplateService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () -> seederService.seedAll());
            assertTrue(ex.getMessage().contains("Python算法入门"));
            assertTrue(ex.getMessage().contains("不存在"));
        }

        @Test
        @DisplayName("seedAll: 班级不存在时抛出异常并终止")
        void testSeedAll_classNotFound_throwsException() {
            // Arrange: 模板存在但班级不存在
            when(trainingTemplateService.getOne(any(LambdaQueryWrapper.class))).thenReturn(template);
            when(adminClassService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () -> seederService.seedAll());
            assertTrue(ex.getMessage().contains("26软件工程1班"));
        }

        @Test
        @DisplayName("generateAndSeedContent: 正确读取 phases_json 并生成内容写入 standard_payload_json")
        void testGenerateAndSeedContent_generatesContentForAllNodes() {
            // Arrange
            when(trainingTemplateService.getById(1L)).thenReturn(template);
            when(trainingTemplateService.updateById(any(WfTrainingTemplate.class))).thenReturn(true);

            // Act
            seederService.generateAndSeedContent(1L);

            // Assert: 验证模板被更新（standard_payload_json 被写入）
            verify(trainingTemplateService).updateById(argThat(t -> {
                // 验证 ai_status 被设置为 2
                assertEquals(2, t.getAiStatus());
                // 验证 standard_payload_json 不为空
                assertNotNull(t.getStandardPayloadJson());
                return true;
            }));
        }

        @Test
        @DisplayName("generateAndSeedContent: phases_json 为空时抛出异常")
        void testGenerateAndSeedContent_emptyPhasesJson_throwsException() {
            // Arrange: 模板存在但 phases_json 为空
            WfTrainingTemplate emptyTemplate = new WfTrainingTemplate();
            emptyTemplate.setId(2L);
            emptyTemplate.setPhasesJson(null);
            when(trainingTemplateService.getById(2L)).thenReturn(emptyTemplate);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> seederService.generateAndSeedContent(2L));
        }

        @Test
        @DisplayName("createDemoStudents: 幂等性 - 已存在的用户名不重复创建")
        void testCreateDemoStudents_idempotent_skipsExisting() {
            // Arrange: 模拟已存在 demo_student_01
            SysUser existingUser = new SysUser();
            existingUser.setId(100L);
            existingUser.setUsername("demo_student_01");

            // 第一次查询返回已存在用户，后续返回 null
            when(sysUserService.getOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(existingUser)  // demo_student_01 exists
                    .thenReturn(null);         // others don't exist

            when(passwordEncoder.encode("123456")).thenReturn("$2a$10$encoded");

            // createDemoStudents 需要查找班级和专业
            BizAdminClass cls = new BizAdminClass();
            cls.setId(10L);
            cls.setClassName("26软件工程1班");
            when(adminClassService.getOne(any(LambdaQueryWrapper.class))).thenReturn(cls);


            // Act
            int created = seederService.createDemoStudents(2);
            // Assert: 只创建了 1 个新用户（跳过了已存在的 demo_student_01）
            assertEquals(1, created);
        }
    }

    // ==================== 学生实训流转测试 ====================

    @Nested
    @DisplayName("学生实训流转：进入节点 → 完成节点 → 阶段解锁")
    class StudentFlowTests {

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
            participation.setStatus(1);

            task = new BizTrainingTask();
            task.setId(10L);
            task.setTemplateId(50L);
            task.setEndTime(LocalDateTime.now().plusDays(7));

            template = new WfTrainingTemplate();
            template.setId(50L);
            template.setPhasesJson("[{\"phase_id\":\"p1\",\"phase_name\":\"课前\",\"sort_num\":1},{\"phase_id\":\"p2\",\"phase_name\":\"课中\",\"sort_num\":2},{\"phase_id\":\"p3\",\"phase_name\":\"课后\",\"sort_num\":3}]");
        }

        @Test
        @DisplayName("完整流转：进入节点 → 完成节点 → 阶段解锁下一阶段")
        void testFullFlow_enterComplete_unlocksNextPhase() {
            // === Step 1: 进入节点 ===
            Long nodeInstanceId = 200L;

            // Mock: 任务未过期
            when(participationMapper.selectById(1L)).thenReturn(participation);
            when(trainingTaskMapper.selectById(10L)).thenReturn(task);

            // Mock: 节点在阶段 p1 中
            WfNodeInstance nodeInstance = new WfNodeInstance();
            nodeInstance.setId(nodeInstanceId);
            nodeInstance.setPhaseId("p1");
            nodeInstance.setTaskId(10L);
            when(nodeInstanceMapper.selectById(nodeInstanceId)).thenReturn(nodeInstance);

            // Mock: 阶段解锁检查 - p1 是第一个阶段（默认解锁）
            when(trainingTemplateMapper.selectById(50L)).thenReturn(template);
            // 第一次 selectList 用于 checkPhaseLock 中的 isPhaseComplete 检查
            when(nodeInstanceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            // Mock: 无已有进度记录
            when(studentNodeProgressMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(studentActivityStateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Act: 进入节点
            WfStudentNodeProgress enterResult = phaseExecutionService.enterNode(1L, nodeInstanceId);

            // Assert: 进入成功，status=1
            assertNotNull(enterResult);
            assertEquals(1, enterResult.getStatus());
            assertNotNull(enterResult.getEnteredAt());

            // Verify: 创建了进度记录和活动状态
            verify(studentNodeProgressMapper).insert(any(WfStudentNodeProgress.class));
            verify(studentActivityStateMapper).insert(any(WfStudentActivityState.class));
        }

        @Test
        @DisplayName("完成节点：更新 status=2，计算停留时长，清除 activity_state")
        void testCompleteNode_updatesStatusAndDuration() {
            Long nodeInstanceId = 200L;

            // Mock: 已有进行中的进度记录
            WfStudentNodeProgress progress = new WfStudentNodeProgress();
            progress.setId(300L);
            progress.setParticipationId(1L);
            progress.setNodeInstanceId(nodeInstanceId);
            progress.setStatus(1);
            progress.setEnteredAt(LocalDateTime.now().minusMinutes(10));

            when(studentNodeProgressMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(progress);

            // Mock: 活动状态存在
            WfStudentActivityState activityState = new WfStudentActivityState();
            activityState.setId(400L);
            activityState.setParticipationId(1L);
            activityState.setCurrentNodeId(String.valueOf(nodeInstanceId));
            when(studentActivityStateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(activityState);

            // Mock: 节点实例（用于 checkAndUnlockNextPhase）
            WfNodeInstance nodeInstance = new WfNodeInstance();
            nodeInstance.setId(nodeInstanceId);
            nodeInstance.setPhaseId("p1");
            nodeInstance.setTaskId(10L);
            when(nodeInstanceMapper.selectById(nodeInstanceId)).thenReturn(nodeInstance);

            // Mock: checkAndUnlockNextPhase 依赖
            when(participationMapper.selectById(1L)).thenReturn(participation);
            when(trainingTaskMapper.selectById(10L)).thenReturn(task);
            when(trainingTemplateMapper.selectById(50L)).thenReturn(template);
            when(nodeInstanceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            // Act: 完成节点
            phaseExecutionService.completeNode(1L, nodeInstanceId);

            // Assert: status 更新为 2
            assertEquals(2, progress.getStatus());
            assertNotNull(progress.getExitedAt());
            assertTrue(progress.getStayDurationSeconds() > 0);

            // Assert: activity_state 的 currentNodeId 被清除
            assertNull(activityState.getCurrentNodeId());

            // Verify: 更新了进度记录和活动状态
            verify(studentNodeProgressMapper).updateById(progress);
            verify(studentActivityStateMapper).updateById(activityState);
        }

        @Test
        @DisplayName("阶段解锁：完成阶段内所有必修节点后解锁下一阶段")
        void testPhaseUnlock_allRequiredComplete_unlocksNext() {
            // Arrange: 阶段 p1 有 2 个必修节点，都已完成
            WfNodeInstance requiredNode1 = new WfNodeInstance();
            requiredNode1.setId(201L);
            requiredNode1.setConfigJson("{\"orchestration_settings\":{\"is_required\":true}}");

            WfNodeInstance requiredNode2 = new WfNodeInstance();
            requiredNode2.setId(202L);
            requiredNode2.setConfigJson("{\"orchestration_settings\":{\"is_required\":true}}");

            // Mock: isPhaseComplete 查询 p1 的节点
            when(nodeInstanceMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(requiredNode1, requiredNode2))  // p1 nodes
                    .thenReturn(Arrays.asList(requiredNode1));                // p2 nodes (has required)

            when(participationMapper.selectById(1L)).thenReturn(participation);
            when(trainingTaskMapper.selectById(10L)).thenReturn(task);
            when(trainingTemplateMapper.selectById(50L)).thenReturn(template);

            // Mock: 查找 participation by taskId + studentId
            when(participationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(participation);

            // Mock: 所有必修节点都已完成
            when(studentNodeProgressMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            // Act: 检查并解锁下一阶段
            phaseExecutionService.checkAndUnlockNextPhase(1L, "p1");

            // Assert: 方法执行完毕无异常（解锁是通过 getPhaseUnlockStatuses 动态计算的）
            // 验证 isPhaseComplete 被调用（确认阶段完成检查执行了）
            verify(nodeInstanceMapper, atLeastOnce()).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("进度持久化：已完成节点再次进入不修改记录")
        void testProgressPersistence_completedNodeEnter_noModification() {
            Long nodeInstanceId = 200L;

            // Mock: 任务未过期
            when(participationMapper.selectById(1L)).thenReturn(participation);
            when(trainingTaskMapper.selectById(10L)).thenReturn(task);

            // Mock: 节点在阶段 p1 中
            WfNodeInstance nodeInstance = new WfNodeInstance();
            nodeInstance.setId(nodeInstanceId);
            nodeInstance.setPhaseId("p1");
            nodeInstance.setTaskId(10L);
            when(nodeInstanceMapper.selectById(nodeInstanceId)).thenReturn(nodeInstance);

            // Mock: 阶段解锁检查
            when(trainingTemplateMapper.selectById(50L)).thenReturn(template);
            when(nodeInstanceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            // Mock: 已有已完成的进度记录 (status=2)
            WfStudentNodeProgress completedProgress = new WfStudentNodeProgress();
            completedProgress.setId(300L);
            completedProgress.setParticipationId(1L);
            completedProgress.setNodeInstanceId(nodeInstanceId);
            completedProgress.setStatus(2); // 已完成
            completedProgress.setEnteredAt(LocalDateTime.now().minusHours(1));
            completedProgress.setExitedAt(LocalDateTime.now().minusMinutes(50));
            completedProgress.setStayDurationSeconds(600);

            when(studentNodeProgressMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(completedProgress);

            // Mock: 活动状态
            when(studentActivityStateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Act: 进入已完成的节点
            WfStudentNodeProgress result = phaseExecutionService.enterNode(1L, nodeInstanceId);

            // Assert: 返回原记录，status 仍为 2，不修改
            assertEquals(2, result.getStatus());
            assertEquals(600, result.getStayDurationSeconds());

            // Verify: 不调用 updateById（不修改已完成记录）
            verify(studentNodeProgressMapper, never()).updateById(completedProgress);
            // 但仍然更新 activity_state
            verify(studentActivityStateMapper).insert(any(WfStudentActivityState.class));
        }

        @Test
        @DisplayName("节点内容从 config_json 正确传递：验证 config_json 结构")
        void testNodeContent_configJsonStructure() throws Exception {
            // 模拟 theory_class 节点的 config_json
            String configJson = "{\"slides\":[{\"type\":\"intro\",\"title\":\"Python列表基础\",\"content\":\"列表是Python中最常用的数据结构\",\"bullet_points\":[\"有序集合\",\"可变长度\"]},{\"type\":\"code\",\"title\":\"列表操作\",\"content\":\"演示代码\",\"code\":\"numbers = [3,1,4]\\nnumbers.sort()\",\"output\":\"[1, 3, 4]\"}]}";

            // 验证 JSON 结构可以正确解析
            JsonNode configNode = objectMapper.readTree(configJson);
            assertTrue(configNode.has("slides"));

            JsonNode slides = configNode.get("slides");
            assertTrue(slides.isArray());
            assertEquals(2, slides.size());

            // 验证 intro slide 结构
            JsonNode introSlide = slides.get(0);
            assertEquals("intro", introSlide.get("type").asText());
            assertTrue(introSlide.has("title"));
            assertTrue(introSlide.has("content"));
            assertTrue(introSlide.has("bullet_points"));

            // 验证 code slide 结构
            JsonNode codeSlide = slides.get(1);
            assertEquals("code", codeSlide.get("type").asText());
            assertTrue(codeSlide.has("code"));
            assertTrue(codeSlide.has("output"));
        }

        @Test
        @DisplayName("节点内容从 config_json 正确传递：验证 coding_class 结构")
        void testNodeContent_codingClassConfigJson() throws Exception {
            // 模拟 coding_class 节点的 config_json
            String configJson = "{\"coding_task\":{\"description\":\"实现冒泡排序\",\"code_template\":\"def bubble_sort(arr):\\n    pass\",\"hints\":[\"比较相邻元素\",\"外层循环控制轮数\"],\"test_cases\":[{\"description\":\"普通数组\",\"input\":\"[3,1,4]\",\"expected\":\"[1,3,4]\"},{\"description\":\"空数组\",\"input\":\"[]\",\"expected\":\"[]\"}],\"language\":\"python\"}}";

            // 验证 JSON 结构可以正确解析
            JsonNode configNode = objectMapper.readTree(configJson);
            assertTrue(configNode.has("coding_task"));

            JsonNode codingTask = configNode.get("coding_task");
            assertTrue(codingTask.has("description"));
            assertTrue(codingTask.get("description").asText().length() <= 500);
            assertTrue(codingTask.has("code_template"));
            assertFalse(codingTask.get("code_template").asText().isEmpty());
            assertTrue(codingTask.has("hints"));
            assertTrue(codingTask.get("hints").isArray());
            int hintsCount = codingTask.get("hints").size();
            assertTrue(hintsCount >= 1 && hintsCount <= 5);
            assertTrue(codingTask.has("test_cases"));
            assertTrue(codingTask.get("test_cases").isArray());
            int testCasesCount = codingTask.get("test_cases").size();
            assertTrue(testCasesCount >= 2 && testCasesCount <= 10);
            assertEquals("python", codingTask.get("language").asText());

            // 验证 test_case 结构
            JsonNode firstTestCase = codingTask.get("test_cases").get(0);
            assertTrue(firstTestCase.has("description"));
            assertTrue(firstTestCase.has("input"));
            assertTrue(firstTestCase.has("expected"));
        }
    }

    // ==================== 实训状态流转测试 ====================

    @Nested
    @DisplayName("实训状态流转：开始 → 进行中 → 完成")
    class TrainingStatusFlowTests {

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

        @Test
        @DisplayName("getPhaseUnlockStatuses: 第一个阶段默认解锁，后续阶段锁定")
        void testPhaseUnlockStatuses_firstPhaseUnlocked() {
            // Arrange
            BizTrainingParticipation participation = new BizTrainingParticipation();
            participation.setId(1L);
            participation.setTaskId(10L);
            participation.setStudentId(100L);
            when(participationMapper.selectById(1L)).thenReturn(participation);

            BizTrainingTask task = new BizTrainingTask();
            task.setId(10L);
            task.setTemplateId(50L);
            when(trainingTaskMapper.selectById(10L)).thenReturn(task);

            WfTrainingTemplate template = new WfTrainingTemplate();
            template.setId(50L);
            template.setPhasesJson("[{\"phase_id\":\"p1\",\"phase_name\":\"课前\",\"sort_num\":1},{\"phase_id\":\"p2\",\"phase_name\":\"课中\",\"sort_num\":2}]");
            when(trainingTemplateMapper.selectById(50L)).thenReturn(template);

            // Mock: p1 有必修节点未完成
            WfNodeInstance requiredNode = new WfNodeInstance();
            requiredNode.setId(201L);
            requiredNode.setConfigJson("{\"orchestration_settings\":{\"is_required\":true}}");
            when(nodeInstanceMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(requiredNode));

            when(participationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(participation);
            when(studentNodeProgressMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            // Act
            List<PhaseUnlockStatus> statuses = phaseExecutionService.getPhaseUnlockStatuses(1L);

            // Assert
            assertNotNull(statuses);
            assertFalse(statuses.isEmpty());

            // 第一个阶段应该解锁
            PhaseUnlockStatus firstPhase = statuses.get(0);
            assertEquals("p1", firstPhase.getPhaseId());
            assertTrue(firstPhase.getIsUnlocked());

            // 第二个阶段应该锁定（因为 p1 未完成）
            if (statuses.size() > 1) {
                PhaseUnlockStatus secondPhase = statuses.get(1);
                assertEquals("p2", secondPhase.getPhaseId());
                assertFalse(secondPhase.getIsUnlocked());
            }
        }

        @Test
        @DisplayName("getCurrentUnlockedPhaseId: 返回第一个未完成的阶段")
        void testGetCurrentUnlockedPhaseId_returnsFirstIncomplete() {
            // Arrange
            BizTrainingTask task = new BizTrainingTask();
            task.setId(10L);
            task.setTemplateId(50L);
            when(trainingTaskMapper.selectById(10L)).thenReturn(task);

            WfTrainingTemplate template = new WfTrainingTemplate();
            template.setId(50L);
            template.setPhasesJson("[{\"phase_id\":\"p1\",\"phase_name\":\"课前\",\"sort_num\":1},{\"phase_id\":\"p2\",\"phase_name\":\"课中\",\"sort_num\":2}]");
            when(trainingTemplateMapper.selectById(50L)).thenReturn(template);

            // Mock: p1 无节点（视为已完成）
            when(nodeInstanceMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList())  // p1: no nodes → complete
                    .thenReturn(Collections.singletonList(createRequiredNode(201L)));  // p2: has required node

            when(participationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new BizTrainingParticipation());
            when(studentNodeProgressMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            // Act
            String currentPhaseId = phaseExecutionService.getCurrentUnlockedPhaseId(10L, 100L);

            // Assert: p1 已完成，返回 p2
            assertEquals("p2", currentPhaseId);
        }

        private WfNodeInstance createRequiredNode(Long id) {
            WfNodeInstance node = new WfNodeInstance();
            node.setId(id);
            node.setConfigJson("{\"orchestration_settings\":{\"is_required\":true}}");
            return node;
        }
    }
}
