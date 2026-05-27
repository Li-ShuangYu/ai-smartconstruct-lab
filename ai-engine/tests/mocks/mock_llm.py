"""
Mock LLM Service — returns fixed structured responses for each agent type.

Used in integration tests to validate the orchestration pipeline without
making real LLM API calls. Implements the same interface as LLMService.
"""

from typing import Type, TypeVar

from pydantic import BaseModel

from models.agent_outputs import (
    TextAgentResult,
    StructAgentResult,
    ExamAgentResult,
    CodeAgentResult,
    VideoAgentResult,
    GeneratedQuestion,
    GeneratedMindmap,
    GeneratedChapter,
    GeneratedFlashcard,
)

T = TypeVar("T", bound=BaseModel)


# --- Fixed responses for each agent result type ---

FIXED_TEXT_RESULT = TextAgentResult(
    node_id="mock",
    welcome_message="欢迎来到本次实训课程！在这里你将学习核心技能并完成实践任务。",
    summary="本课程涵盖了软件工程的基础知识，包括需求分析、系统设计和编码实现三个阶段。",
    key_points=[
        {"title": "需求分析", "description": "理解用户需求并转化为系统规格"},
        {"title": "系统设计", "description": "设计合理的架构和数据模型"},
        {"title": "编码实现", "description": "使用最佳实践编写高质量代码"},
        {"title": "测试验证", "description": "通过单元测试和集成测试确保质量"},
        {"title": "部署运维", "description": "将系统部署到生产环境并持续监控"},
    ],
)

FIXED_STRUCT_RESULT = StructAgentResult(
    node_id="mock",
    mindmap=GeneratedMindmap(
        root="软件工程基础",
        children=[
            {"name": "需求工程", "children": [{"name": "用例分析"}, {"name": "需求规格"}]},
            {"name": "系统设计", "children": [{"name": "架构设计"}, {"name": "详细设计"}]},
            {"name": "编码实现", "children": [{"name": "编码规范"}, {"name": "代码审查"}]},
        ],
    ),
    task_breakdown=[
        {"milestone": "需求调研", "duration": "2天", "deliverable": "需求文档"},
        {"milestone": "架构设计", "duration": "3天", "deliverable": "设计文档"},
        {"milestone": "编码开发", "duration": "5天", "deliverable": "源代码"},
        {"milestone": "测试验收", "duration": "2天", "deliverable": "测试报告"},
        {"milestone": "部署上线", "duration": "1天", "deliverable": "部署文档"},
    ],
)

FIXED_EXAM_RESULT = ExamAgentResult(
    node_id="mock",
    questions=[
        GeneratedQuestion(
            question_type=1,
            content='{"stem": "以下哪个不是面向对象的基本特征？", "options": ["A.封装", "B.继承", "C.多态", "D.递归"]}',
            standard_answer="D",
            default_score=5.0,
            knowledge_point="面向对象基础",
        ),
        GeneratedQuestion(
            question_type=2,
            content='{"stem": "以下哪些属于设计模式的分类？", "options": ["A.创建型", "B.结构型", "C.行为型", "D.功能型"]}',
            standard_answer="A,B,C",
            default_score=5.0,
            knowledge_point="设计模式",
        ),
        GeneratedQuestion(
            question_type=4,
            content='{"stem": "接口隔离原则要求客户端不应该依赖它不需要的接口。"}',
            standard_answer="正确",
            default_score=3.0,
            knowledge_point="SOLID原则",
        ),
    ],
    flashcards=[
        GeneratedFlashcard(front_content="什么是单一职责原则？", back_content="一个类应该只有一个引起它变化的原因。", order=1),
        GeneratedFlashcard(front_content="什么是开闭原则？", back_content="软件实体应该对扩展开放，对修改关闭。", order=2),
    ],
)

FIXED_CODE_RESULT = CodeAgentResult(
    node_id="mock",
    review_checkpoints=[
        {"name": "代码规范", "description": "检查命名规范、缩进、注释是否符合标准", "weight": 20},
        {"name": "逻辑正确性", "description": "检查核心业务逻辑是否正确实现", "weight": 40},
        {"name": "异常处理", "description": "检查是否有完善的异常处理机制", "weight": 20},
        {"name": "性能优化", "description": "检查是否存在明显的性能问题", "weight": 20},
    ],
    eval_indicators=[
        {"name": "代码质量", "max_score": 30, "criteria": "代码清晰、可读性强、无冗余"},
        {"name": "功能完整性", "max_score": 40, "criteria": "所有功能需求均已实现"},
        {"name": "测试覆盖", "max_score": 30, "criteria": "关键路径有单元测试覆盖"},
    ],
)

FIXED_VIDEO_RESULT = VideoAgentResult(
    node_id="mock",
    chapters=[
        GeneratedChapter(title="课程导入", start_time="00:00:00", end_time="00:03:00", summary="介绍课程目标和学习路径"),
        GeneratedChapter(title="核心概念", start_time="00:03:00", end_time="00:15:00", summary="讲解核心理论知识"),
        GeneratedChapter(title="实践演示", start_time="00:15:00", end_time="00:30:00", summary="通过实例演示操作步骤"),
        GeneratedChapter(title="总结回顾", start_time="00:30:00", end_time="00:35:00", summary="总结要点并布置练习"),
    ],
)

# Map output schema type to fixed response
_SCHEMA_TO_RESPONSE = {
    TextAgentResult: FIXED_TEXT_RESULT,
    StructAgentResult: FIXED_STRUCT_RESULT,
    ExamAgentResult: FIXED_EXAM_RESULT,
    CodeAgentResult: FIXED_CODE_RESULT,
    VideoAgentResult: FIXED_VIDEO_RESULT,
}


class MockStructuredLLM:
    """
    Mock structured LLM that returns fixed responses based on the output schema.

    Mimics the object returned by LLMService.with_structured_output(schema),
    providing an ainvoke() method that returns the appropriate fixed response.
    """

    def __init__(self, output_schema: Type[T]) -> None:
        self._output_schema = output_schema

    async def ainvoke(self, prompt) -> T:
        """Return a fixed response matching the output schema type."""
        response = _SCHEMA_TO_RESPONSE.get(self._output_schema)
        if response is None:
            raise ValueError(
                f"MockStructuredLLM has no fixed response for schema: {self._output_schema.__name__}"
            )
        return response


class MockLLMService:
    """
    Mock LLM Service implementing the same interface as LLMService.

    Returns fixed, valid responses for each agent type without making
    any real API calls. Used for integration testing.
    """

    def __init__(self) -> None:
        pass

    def get_llm(self):
        """Return None — mock does not provide a raw LLM instance."""
        return None

    def with_structured_output(self, output_schema: Type[T]) -> MockStructuredLLM:
        """
        Return a MockStructuredLLM that produces fixed responses for the given schema.

        Args:
            output_schema: A Pydantic BaseModel subclass defining the expected output.

        Returns:
            A MockStructuredLLM instance with ainvoke() method.
        """
        return MockStructuredLLM(output_schema)
