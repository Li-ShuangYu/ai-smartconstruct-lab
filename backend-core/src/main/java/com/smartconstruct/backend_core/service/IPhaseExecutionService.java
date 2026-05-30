package com.smartconstruct.backend_core.service;

import com.smartconstruct.backend_core.dto.PhaseUnlockStatus;
import com.smartconstruct.backend_core.entity.WfStudentNodeProgress;

import java.util.List;

/**
 * 阶段执行服务接口
 *
 * 负责实训运行时的阶段推进逻辑，包括判断阶段完成状态、确定当前可进入阶段、
 * 管理学生节点进入和完成操作。实现课后自主实训模式下的阶段顺序推进机制：
 * 当前阶段所有必须完成的节点（is_required=true）均达到"已完成"状态后，
 * 自动解锁下一阶段。
 *
 * @author SmartConstruct
 * @see WfStudentNodeProgress
 */
public interface IPhaseExecutionService {

    /**
     * 判断指定阶段是否已完成
     *
     * 完成条件：该阶段内所有标记为 is_required=true 的节点实例（wf_node_instance），
     * 其对应的学生节点进度记录（wf_student_node_progress）status 均为 2（已完成）。
     * 未标记为必须完成的节点（is_required=false）不影响阶段完成判定。
     *
     * @param taskId    实训任务ID（对应 biz_training_task.id）
     * @param phaseId   阶段ID（对应 wf_node_instance.phase_id）
     * @param studentId 学生ID
     * @return true 表示该阶段已完成（所有必须节点均已完成），false 表示尚未完成
     */
    boolean isPhaseComplete(Long taskId, String phaseId, Long studentId);

    /**
     * 获取学生当前可进入的阶段ID
     *
     * 按阶段排序号（sort_num）从小到大遍历所有阶段，返回第一个尚未完成的阶段ID。
     * 如果所有阶段均已完成，则返回最后一个阶段的ID。
     * 第一个阶段始终为已解锁状态（学生进入实训即可访问）。
     *
     * @param taskId    实训任务ID（对应 biz_training_task.id）
     * @param studentId 学生ID
     * @return 当前已解锁的阶段ID（phase_id）
     */
    String getCurrentUnlockedPhaseId(Long taskId, Long studentId);

    /**
     * 学生进入节点，创建或恢复进度记录
     *
     * 行为说明：
     * - 若该学生在该节点无进度记录，则创建新记录，status设为1（进行中），entered_at设为当前时间
     * - 若已有进度记录且status为1（进行中），则恢复该记录（更新entered_at为当前时间）
     * - 同时更新 wf_student_activity_state 表的 current_node_instance_id 为当前节点实例ID
     *
     * 前置校验：
     * - 节点所属阶段必须已解锁（否则抛出 PHASE_LOCKED 异常）
     * - 实训任务未过期（否则抛出 TASK_EXPIRED 异常）
     *
     * @param participationId 学生实训参与ID（对应 biz_training_participation.id）
     * @param nodeInstanceId  节点实例ID（对应 wf_node_instance.id）
     * @return 创建或更新后的学生节点进度记录
     */
    WfStudentNodeProgress enterNode(Long participationId, Long nodeInstanceId);

    /**
     * 学生完成节点
     *
     * 将该节点的 wf_student_node_progress 状态更新为 2（已完成），
     * 记录 exited_at 为当前时间，计算 stay_duration_seconds（该节点所有访问时段的累计停留秒数）。
     * 同时将 wf_student_activity_state 的 current_node_instance_id 设为 null。
     *
     * 完成后自动检查阶段推进：若当前阶段所有必须节点均已完成，则下一阶段自动解锁。
     *
     * @param participationId 学生实训参与ID（对应 biz_training_participation.id）
     * @param nodeInstanceId  节点实例ID（对应 wf_node_instance.id）
     */
    void completeNode(Long participationId, Long nodeInstanceId);

    /**
     * 检查并解锁下一阶段
     *
     * 当指定阶段完成时（所有 is_required=true 节点的 Node_Progress status 均为 2），
     * 将 sort_num 紧邻的下一阶段设为 is_unlocked=true。
     * 如果下一阶段无 is_required 节点，则自动标记为 complete 并继续解锁后续阶段。
     *
     * @param participationId 学生实训参与ID（对应 biz_training_participation.id）
     * @param currentPhaseId  当前阶段ID（刚完成的阶段）
     */
    void checkAndUnlockNextPhase(Long participationId, String currentPhaseId);

    /**
     * 获取学生所有阶段的解锁状态列表
     *
     * 返回该学生在指定实训任务中所有阶段的解锁和完成状态，按 sort_num 升序排列。
     * 第一个阶段（sort_num 最小）始终为已解锁状态。
     * 如果所有阶段均已完成，则所有阶段均为已解锁状态。
     *
     * @param participationId 学生实训参与ID（对应 biz_training_participation.id）
     * @return 阶段解锁状态列表，按 sort_num 升序排列
     */
    List<PhaseUnlockStatus> getPhaseUnlockStatuses(Long participationId);
}
