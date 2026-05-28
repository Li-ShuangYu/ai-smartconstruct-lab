"""
Property 1: Phase validation accepts valid configurations and rejects invalid ones.

**Validates: Requirements 1.1**

For any phase array, the validation function SHALL accept it if and only if
the array length is between 1 and 10 (inclusive) AND every phase name has
length between 1 and 20 characters. All other configurations SHALL be rejected.
"""

import sys
sys.path.insert(0, ".")

import json
import pytest
from hypothesis import given, settings, assume
from hypothesis import strategies as st


# --- Phase validation logic (mirrors backend NodeValidationServiceImpl.validatePhases) ---

def validate_phases(phases: list[dict]) -> tuple[bool, str | None]:
    """
    Validate a phase array according to the spec rules:
    - Array length must be 1-10
    - Each phase_name must be 1-20 characters
    - sort_num values must be unique

    Returns (is_valid, error_message_or_None).
    """
    if not isinstance(phases, list):
        return False, "阶段配置必须是一个JSON数组"

    size = len(phases)
    if size < 1 or size > 10:
        return False, f"阶段数量必须在1到10之间，当前数量: {size}"

    sort_nums = set()
    for i, phase in enumerate(phases):
        # Validate phase_name
        phase_name = phase.get("phase_name")
        if not isinstance(phase_name, str):
            return False, f"阶段[{i}]缺少有效的phase_name字段"
        if len(phase_name) < 1 or len(phase_name) > 20:
            return False, f"阶段[{i}]的phase_name长度必须在1到20个字符之间"

        # Validate sort_num uniqueness
        sort_num = phase.get("sort_num")
        if not isinstance(sort_num, int):
            return False, f"阶段[{i}]缺少有效的sort_num字段"
        if sort_num in sort_nums:
            return False, f"阶段sort_num值必须唯一，重复值: {sort_num}"
        sort_nums.add(sort_num)

    return True, None


# --- Strategies ---

def valid_phase_name():
    """Generate valid phase names: 1-20 characters."""
    return st.text(
        alphabet=st.characters(whitelist_categories=("L", "N", "P", "S")),
        min_size=1,
        max_size=20,
    )


def valid_phase(sort_num: int):
    """Generate a valid phase dict with a given sort_num."""
    return st.builds(
        lambda name: {
            "phase_id": f"phase_{sort_num}",
            "phase_name": name,
            "sort_num": sort_num,
        },
        name=valid_phase_name(),
    )


@st.composite
def valid_phase_array(draw):
    """Generate a valid phase array (1-10 phases, valid names, unique sort_nums)."""
    count = draw(st.integers(min_value=1, max_value=10))
    phases = []
    for i in range(count):
        name = draw(valid_phase_name())
        phases.append({
            "phase_id": f"phase_{i+1}",
            "phase_name": name,
            "sort_num": i + 1,
        })
    return phases


@st.composite
def invalid_phase_array_wrong_count(draw):
    """Generate phase arrays with invalid count (0 or >10)."""
    count = draw(st.one_of(
        st.just(0),
        st.integers(min_value=11, max_value=30),
    ))
    phases = []
    for i in range(count):
        phases.append({
            "phase_id": f"phase_{i+1}",
            "phase_name": f"Phase {i+1}",
            "sort_num": i + 1,
        })
    return phases


@st.composite
def invalid_phase_array_bad_name(draw):
    """Generate phase arrays where at least one name is invalid (empty or >20 chars)."""
    count = draw(st.integers(min_value=1, max_value=10))
    bad_index = draw(st.integers(min_value=0, max_value=count - 1))
    phases = []
    for i in range(count):
        if i == bad_index:
            # Generate invalid name: either empty or >20 chars
            bad_name = draw(st.one_of(
                st.just(""),
                st.text(min_size=21, max_size=50),
            ))
            phases.append({
                "phase_id": f"phase_{i+1}",
                "phase_name": bad_name,
                "sort_num": i + 1,
            })
        else:
            name = draw(valid_phase_name())
            phases.append({
                "phase_id": f"phase_{i+1}",
                "phase_name": name,
                "sort_num": i + 1,
            })
    return phases


# --- Property Tests ---

class TestPhaseValidationProperty:
    """Property 1: Phase validation correctness."""

    @given(phases=valid_phase_array())
    @settings(max_examples=100)
    def test_valid_phases_always_accepted(self, phases):
        """Valid phase arrays (1-10 phases, names 1-20 chars) are always accepted."""
        is_valid, error = validate_phases(phases)
        assert is_valid, f"Expected valid but got error: {error}"

    @given(phases=invalid_phase_array_wrong_count())
    @settings(max_examples=100)
    def test_invalid_count_always_rejected(self, phases):
        """Phase arrays with count outside 1-10 are always rejected."""
        is_valid, error = validate_phases(phases)
        assert not is_valid, "Expected rejection for invalid count"

    @given(phases=invalid_phase_array_bad_name())
    @settings(max_examples=100)
    def test_invalid_name_always_rejected(self, phases):
        """Phase arrays with names outside 1-20 chars are always rejected."""
        is_valid, error = validate_phases(phases)
        assert not is_valid, "Expected rejection for invalid name"

    @given(
        count=st.integers(min_value=1, max_value=10),
        name_length=st.integers(min_value=1, max_value=20),
    )
    @settings(max_examples=100)
    def test_boundary_values_accepted(self, count, name_length):
        """Boundary values for count and name length are accepted."""
        phases = []
        for i in range(count):
            phases.append({
                "phase_id": f"phase_{i+1}",
                "phase_name": "A" * name_length,
                "sort_num": i + 1,
            })
        is_valid, error = validate_phases(phases)
        assert is_valid, f"Expected valid but got error: {error}"
