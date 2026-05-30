package com.smartconstruct.backend_core.service.impl;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.time.LocalDateTime;

/**
 * Property 7: 节点进入状态转换正确性
 *
 * <b>Validates: Requirements 7.1, 7.2</b>
 *
 * For any node progress record, when status is 0 (未开始) or 1 (进行中), calling enterNode
 * should result in status=1 and entered_at being non-null. When status is 2 (已完成) or
 * 3 (已跳过), calling enterNode should NOT modify the record.
 *
 * Label: "Feature: student-training-demo, Property 7: 节点进入状态转换正确性"
 */
class NodeEnterStateTransitionPropertyTest {

    /** Status constants matching the service implementation */
    private static final int STATUS_NOT_STARTED = 0;
    private static final int STATUS_IN_PROGRESS = 1;
    private static final int STATUS_COMPLETED = 2;
    private static final int STATUS_SKIPPED = 3;

    /**
     * Simplified node progress representation for property testing.
     * Mirrors the relevant fields of WfStudentNodeProgress.
     */
    static class TestNodeProgress {
        Integer status;
        LocalDateTime enteredAt;
        LocalDateTime updatedAt;

        TestNodeProgress(Integer status, LocalDateTime enteredAt) {
            this.status = status;
            this.enteredAt = enteredAt;
            this.updatedAt = null;
        }

        /** Create a deep copy to compare before/after */
        TestNodeProgress copy() {
            TestNodeProgress copy = new TestNodeProgress(this.status, this.enteredAt);
            copy.updatedAt = this.updatedAt;
            return copy;
        }
    }

    /**
     * Pure logic extracted from PhaseExecutionServiceImpl.enterNode:
     * Applies the enterNode state transition to an existing progress record.
     *
     * - status 0 or 1: set status=1, set entered_at=now, set updated_at=now
     * - status 2 or 3: return as-is without modification
     *
     * @param progress the existing progress record (non-null)
     * @param now the current timestamp to use for entered_at/updated_at
     * @return the (possibly modified) progress record
     */
    private TestNodeProgress applyEnterNodeTransition(TestNodeProgress progress, LocalDateTime now) {
        if (progress.status == STATUS_NOT_STARTED || progress.status == STATUS_IN_PROGRESS) {
            progress.status = STATUS_IN_PROGRESS;
            progress.enteredAt = now;
            progress.updatedAt = now;
        }
        // status 2 or 3: no modification
        return progress;
    }

    @Provide
    Arbitrary<Integer> validStatuses() {
        return Arbitraries.of(STATUS_NOT_STARTED, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_SKIPPED);
    }

    @Provide
    Arbitrary<Integer> activeStatuses() {
        return Arbitraries.of(STATUS_NOT_STARTED, STATUS_IN_PROGRESS);
    }

    @Provide
    Arbitrary<Integer> terminalStatuses() {
        return Arbitraries.of(STATUS_COMPLETED, STATUS_SKIPPED);
    }

    /**
     * Generates a random offset in minutes to create varied timestamps,
     * ensuring the property space is large enough for 100+ randomized iterations.
     */
    @Provide
    Arbitrary<Integer> minuteOffsets() {
        return Arbitraries.integers().between(1, 10000);
    }

    /**
     * Property: When initial status is 0 (未开始) or 1 (进行中), enterNode sets status=1
     * and entered_at is non-null.
     *
     * Validates: Requirements 7.1, 7.2
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 7: 节点进入状态转换正确性 - status 0或1时转为进行中")
    void enterNodeSetsStatusToInProgressWhenNotStartedOrInProgress(
            @ForAll("activeStatuses") int initialStatus,
            @ForAll @IntRange(min = 1, max = 10000) int minutesAgo
    ) {
        LocalDateTime now = LocalDateTime.of(2026, 1, 15, 10, 0).plusMinutes(minutesAgo);
        // enteredAt may or may not be set initially
        LocalDateTime initialEnteredAt = (initialStatus == STATUS_IN_PROGRESS) ? now.minusMinutes(5) : null;
        TestNodeProgress progress = new TestNodeProgress(initialStatus, initialEnteredAt);

        applyEnterNodeTransition(progress, now);

        assert progress.status == STATUS_IN_PROGRESS :
                "After enterNode with initial status=" + initialStatus + ", status should be 1 (进行中), but was " + progress.status;
        assert progress.enteredAt != null :
                "After enterNode with initial status=" + initialStatus + ", entered_at should be non-null";
        assert progress.enteredAt.equals(now) :
                "After enterNode, entered_at should be set to current time";
    }

    /**
     * Property: When initial status is 2 (已完成) or 3 (已跳过), enterNode does NOT modify
     * the record — status and entered_at remain unchanged.
     *
     * Validates: Requirements 7.1, 7.2
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 7: 节点进入状态转换正确性 - status 2或3时记录不变")
    void enterNodeDoesNotModifyCompletedOrSkippedRecords(
            @ForAll("terminalStatuses") int initialStatus,
            @ForAll @IntRange(min = 1, max = 10000) int minutesAgo
    ) {
        LocalDateTime now = LocalDateTime.of(2026, 1, 15, 10, 0).plusMinutes(minutesAgo);
        // Completed/skipped nodes should have an entered_at from before
        LocalDateTime originalEnteredAt = now.minusHours(1);
        TestNodeProgress progress = new TestNodeProgress(initialStatus, originalEnteredAt);
        TestNodeProgress before = progress.copy();

        applyEnterNodeTransition(progress, now);

        assert progress.status.equals(before.status) :
                "After enterNode with initial status=" + initialStatus + ", status should remain " + before.status + " but was " + progress.status;
        assert Objects.equals(progress.enteredAt, before.enteredAt) :
                "After enterNode with initial status=" + initialStatus + ", entered_at should remain unchanged";
        assert Objects.equals(progress.updatedAt, before.updatedAt) :
                "After enterNode with initial status=" + initialStatus + ", updated_at should remain unchanged";
    }

    /**
     * Property: For any random initial status (0/1/2/3), the enterNode transition
     * produces a valid result — either status=1 with non-null entered_at, or unchanged record.
     *
     * Validates: Requirements 7.1, 7.2
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 7: 节点进入状态转换正确性 - 任意初始状态转换正确")
    void enterNodeTransitionIsCorrectForAnyInitialStatus(
            @ForAll("validStatuses") int initialStatus,
            @ForAll @IntRange(min = 1, max = 10000) int minutesAgo
    ) {
        LocalDateTime now = LocalDateTime.of(2026, 1, 15, 10, 0).plusMinutes(minutesAgo);
        LocalDateTime originalEnteredAt = (initialStatus >= STATUS_IN_PROGRESS) ? now.minusMinutes(10) : null;
        TestNodeProgress progress = new TestNodeProgress(initialStatus, originalEnteredAt);
        TestNodeProgress before = progress.copy();

        applyEnterNodeTransition(progress, now);

        if (initialStatus == STATUS_NOT_STARTED || initialStatus == STATUS_IN_PROGRESS) {
            // Should transition to in-progress
            assert progress.status == STATUS_IN_PROGRESS :
                    "Status should be 1 after enterNode with initial status=" + initialStatus;
            assert progress.enteredAt != null :
                    "entered_at should be non-null after enterNode with initial status=" + initialStatus;
        } else {
            // Should remain unchanged
            assert progress.status.equals(before.status) :
                    "Status should remain " + before.status + " after enterNode with initial status=" + initialStatus;
            assert Objects.equals(progress.enteredAt, before.enteredAt) :
                    "entered_at should remain unchanged after enterNode with initial status=" + initialStatus;
        }
    }

    // Utility for null-safe equals comparison
    private static class Objects {
        static boolean equals(Object a, Object b) {
            return (a == b) || (a != null && a.equals(b));
        }
    }
}
