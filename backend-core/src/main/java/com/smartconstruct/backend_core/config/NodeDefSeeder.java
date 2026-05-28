package com.smartconstruct.backend_core.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartconstruct.backend_core.entity.WfNodeDef;
import com.smartconstruct.backend_core.mapper.WfNodeDefMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 节点定义种子数据初始化器
 *
 * 在应用启动时检查 wf_node_def 表中各节点类型是否已有 ai_spec_json，
 * 若缺失则自动补充。确保 enrichWithAiSpecs() 能为所有节点类型注入 ai_spec。
 */
@Component
public class NodeDefSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(NodeDefSeeder.class);

    @Autowired
    private WfNodeDefMapper nodeDefMapper;

    @Override
    public void run(String... args) {
        seedIfMissing("start", buildSpec("TEXT", 5, "enable_ai_welcome"));
        seedIfMissing("end", buildSpec("TEXT", 8, "enable_ai_report"));
        seedIfMissing("resource_read", buildSpec("TEXT", 3, "enable_ai_summary", "enable_ai_key_points"));
        seedIfMissing("video_watch", buildSpec("VIDEO", 3, "enable_ai_chapter_markers", "enable_ai_video_summary"));
        seedIfMissing("mindmap_preview", buildSpec("STRUCT", 5, "enable_ai_generate_map"));
        seedIfMissing("mindmap_draw", buildSpec("STRUCT", 6, "enable_ai_structure_eval", "allow_ai_qa"));
        seedIfMissing("task_distribute", buildSpec("STRUCT", 4, "enable_ai_task_split"));
        seedIfMissing("req_upload", buildSpec("CODE", 4, "enable_ai_pre_review"));
        seedIfMissing("plan_upload", buildSpec("CODE", 4, "enable_ai_feasibility"));
        seedIfMissing("homework", buildSpec("EXAM", 2, "enable_ai_grading"));
        seedIfMissing("survey", buildSpec("TEXT", 6, "enable_ai_survey_analysis"));
        seedIfMissing("student_peer_review", buildSpec("TEXT", 6, "enable_ai_anomaly_detection"));
        seedIfMissing("teacher_eval", buildSpec("TEXT", 3, "enable_ai_eval_suggestion"));
        seedIfMissing("grouping", buildSpec("STRUCT", 5, "enable_ai_balanced_grouping"));
        seedIfMissing("coding_class", buildSpec("CODE", 4, "enable_ai_code_review"));
        seedIfMissing("ai_practice", buildSpec("TEXT", 1, "enable_ai_system_prompt"));
        seedIfMissing("theory_class", buildSpec("EXAM", 2, "enable_ai_error_book"));

        log.info("NodeDefSeeder: ai_spec_json seed check complete");
    }

    private void seedIfMissing(String nodeType, Map<String, Object> aiSpec) {
        LambdaQueryWrapper<WfNodeDef> q = new LambdaQueryWrapper<>();
        q.eq(WfNodeDef::getNodeType, nodeType);
        WfNodeDef existing = nodeDefMapper.selectOne(q);

        if (existing == null) {
            // Row doesn't exist at all — insert it
            WfNodeDef def = new WfNodeDef();
            def.setNodeType(nodeType);
            def.setNodeName(nodeType);
            def.setIsActive(1);
            def.setAiSpecJson(aiSpec);
            nodeDefMapper.insert(def);
            log.info("NodeDefSeeder: inserted wf_node_def for node_type='{}' with ai_spec_json", nodeType);
        } else if (existing.getAiSpecJson() == null) {
            // Row exists but ai_spec_json is null — update it
            LambdaUpdateWrapper<WfNodeDef> u = new LambdaUpdateWrapper<>();
            u.eq(WfNodeDef::getId, existing.getId())
                    .set(WfNodeDef::getAiSpecJson, aiSpec);
            nodeDefMapper.update(null, u);
            log.info("NodeDefSeeder: updated ai_spec_json for node_type='{}'", nodeType);
        }
    }

    private Map<String, Object> buildSpec(String targetAgent, int priority, String... aiFlags) {
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("target_agent", targetAgent);
        spec.put("priority", priority);
        spec.put("ai_flags", java.util.Arrays.asList(aiFlags));
        return spec;
    }
}
