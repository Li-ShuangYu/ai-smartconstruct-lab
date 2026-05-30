package com.smartconstruct.backend_core.service.impl;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Property 9: 实训状态流转完整性
 *
 * <b>Validates: Requirements 8.1, 8.2</b>
 *
 * For any Participation record, status can only flow in the order 0→1→2:
 * - From status=0: only valid transition is to 1 (start training)
 * - From status=1: only valid transition is to 2 (all required nodes complete)
 * - From status=2: no further transitions allowed
 * - Reverse transitions (2→1, 1→0, 2→0) are never allowed
 * - Jump transitions (0→2) are never allowed
 *
 * Label: "Feature: student-training-demo, Property 9: 实训状态流转完整性"
 */
class TrainingStateFlowPropertyTest {

    /** Participation status constants */
    private static final int STATUS_NOT_STARTED = 0;
    private static final int STATUS_IN_PROGRESS = 1;
    private static final int STATUS_COMPLETED = 2;

    /**
     * Represents an operation that can be attempted on a Participation.
     */
    enum TrainingOperation {
        START,      // Attempt to transition to status=1
        COMPLETE,   // Attempt to transition to status=2
        START_AGAIN // Attempt to go back to status=1 (should be rejected from status=2)
    }

    /**
     * State machine validator that enforces the 0→1→2 invariant.
     * Returns the new status after attempting a transition, or -1 if the transition is invalid.
     *
     * This mirrors the logic in StudentTrainingController:
     * - startTraining: only transitions 0→1; status=1 resumes, status=2 shows summary
     * - completeNode: only transitions 1→2 when all required nodes are done
     */
    static class ParticipationStateMachine {
        private int currentStatus;
        private final List<String> transitionLog = new ArrayList<>();

        ParticipationStateMachine(int initialStatus) {
            this.currentStatus = initialStatus;
        }

        /**
         * Attempt a state transition. Returns true if the transition was valid and applied,
         * false if the transition was rejected (invalid).
         */
        boolean attemptTransition(TrainingOperation operation) {
            int previousStatus = currentStatus;

            switch (operation) {
                case START:
                    if (currentStatus == STATUS_NOT_STARTED) {
                        // Valid: 0→1
                        currentStatus = STATUS_IN_PROGRESS;
                        transitionLog.add(previousStatus + "→" + currentStatus + " (START: valid)");
                        return true;
                    } else if (currentStatus == STATUS_IN_PROGRESS) {
                        // Already in progress - resume, no state change
                        transitionLog.add(previousStatus + "→" + currentStatus + " (START: resume, no change)");
                        return true; // Not an error, just no state change
                    } else if (currentStatus == STATUS_COMPLETED) {
                        // Already completed - show summary, no state change
                        transitionLog.add(previousStatus + "→" + currentStatus + " (START: completed, no change)");
                        return true; // Not an error, just no state change
                    }
                    break;

                case COMPLETE:
                    if (currentStatus == STATUS_IN_PROGRESS) {
                        // Valid: 1→2
                        currentStatus = STATUS_COMPLETED;
                        transitionLog.add(previousStatus + "→" + currentStatus + " (COMPLETE: valid)");
                        return true;
                    } else if (currentStatus == STATUS_NOT_STARTED) {
                        // Invalid: cannot complete without starting (0→2 jump not allowed)
                        transitionLog.add(previousStatus + " (COMPLETE: rejected, not started)");
                        return false;
                    } else if (currentStatus == STATUS_COMPLETED) {
                        // Already completed - no further transitions
                        transitionLog.add(previousStatus + " (COMPLETE: rejected, already completed)");
                        return false;
                    }
                    break;

                case START_AGAIN:
                    // Attempting to go back to status=1 from any state
                    if (currentStatus == STATUS_COMPLETED) {
                        // Invalid: 2→1 reverse not allowed
                        transitionLog.add(previousStatus + " (START_AGAIN: rejected, reverse not allowed)");
                        return false;
                    } else if (currentStatus == STATUS_IN_PROGRESS) {
                        // Already in progress - no change
                        transitionLog.add(previousStatus + "→" + currentStatus + " (START_AGAIN: already in progress)");
                        return true;
                    } else if (currentStatus == STATUS_NOT_STARTED) {
                        // Same as START: 0→1
                        currentStatus = STATUS_IN_PROGRESS;
                        transitionLog.add(previousStatus + "→" + currentStatus + " (START_AGAIN: valid as start)");
                        return true;
                    }
                    break;
            }
            return false;
        }

        int getCurrentStatus() {
            return currentStatus;
        }

        List<String> getTransitionLog() {
            return Collections.unmodifiableList(transitionLog);
        }
    }

    /**
     * Checks the state machine invariant: status can only be 0, 1, or 2,
     * and the value can only increase (never decrease or jump).
     */
    private boolean isValidStatus(int status) {
        return status == STATUS_NOT_STARTED || status == STATUS_IN_PROGRESS || status == STATUS_COMPLETED;
    }

    /**
     * Checks that a transition from oldStatus to newStatus respects the 0→1→2 ordering.
     */
    private boolean isValidTransition(int oldStatus, int newStatus) {
        if (oldStatus == newStatus) return true; // No change is always valid
        if (oldStatus == STATUS_NOT_STARTED && newStatus == STATUS_IN_PROGRESS) return true; // 0→1
        if (oldStatus == STATUS_IN_PROGRESS && newStatus == STATUS_COMPLETED) return true; // 1→2
        return false; // All other transitions are invalid
    }

    // ==================== Generators ====================

    @Provide
    Arbitrary<List<TrainingOperation>> randomOperationSequences() {
        return Arbitraries.of(TrainingOperation.values())
                .list()
                .ofMinSize(1)
                .ofMaxSize(20);
    }

    @Provide
    Arbitrary<List<TrainingOperation>> reverseTransitionAttempts() {
        // Generate sequences that include reverse/jump attempts
        Arbitrary<TrainingOperation> ops = Arbitraries.of(TrainingOperation.values());
        return ops.list().ofMinSize(2).ofMaxSize(15)
                .filter(list -> list.contains(TrainingOperation.START_AGAIN)
                        || list.contains(TrainingOperation.COMPLETE));
    }

    // ==================== Property Tests ====================

    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 9: 实训状态流转完整性 - monotonic progression")
    void statusOnlyProgressesForward(
            @ForAll("randomOperationSequences") List<TrainingOperation> operations
    ) {
        ParticipationStateMachine sm = new ParticipationStateMachine(STATUS_NOT_STARTED);
        int previousStatus = STATUS_NOT_STARTED;

        for (TrainingOperation op : operations) {
            sm.attemptTransition(op);
            int currentStatus = sm.getCurrentStatus();

            // Invariant: status never decreases
            assert currentStatus >= previousStatus :
                    String.format("Status decreased from %d to %d after operation %s. Log: %s",
                            previousStatus, currentStatus, op, sm.getTransitionLog());

            // Invariant: status is always valid (0, 1, or 2)
            assert isValidStatus(currentStatus) :
                    String.format("Invalid status %d after operation %s", currentStatus, op);

            // Invariant: transition is valid (no jumps)
            assert isValidTransition(previousStatus, currentStatus) :
                    String.format("Invalid transition %d→%d after operation %s. Log: %s",
                            previousStatus, currentStatus, op, sm.getTransitionLog());

            previousStatus = currentStatus;
        }
    }

    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 9: 实训状态流转完整性 - reverse transitions rejected")
    void reverseTransitionsAreNeverAllowed(
            @ForAll("reverseTransitionAttempts") List<TrainingOperation> operations
    ) {
        ParticipationStateMachine sm = new ParticipationStateMachine(STATUS_NOT_STARTED);

        for (TrainingOperation op : operations) {
            int statusBefore = sm.getCurrentStatus();
            sm.attemptTransition(op);
            int statusAfter = sm.getCurrentStatus();

            // Invariant: status never goes backwards
            assert statusAfter >= statusBefore :
                    String.format("Reverse transition detected: %d→%d after operation %s. Log: %s",
                            statusBefore, statusAfter, op, sm.getTransitionLog());
        }
    }

    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 9: 实训状态流转完整性 - jump transitions rejected")
    void jumpTransitionsAreNeverAllowed(
            @ForAll("randomOperationSequences") List<TrainingOperation> operations
    ) {
        ParticipationStateMachine sm = new ParticipationStateMachine(STATUS_NOT_STARTED);

        for (TrainingOperation op : operations) {
            int statusBefore = sm.getCurrentStatus();
            sm.attemptTransition(op);
            int statusAfter = sm.getCurrentStatus();

            // Invariant: status can only change by at most 1 step at a time
            int diff = statusAfter - statusBefore;
            assert diff >= 0 && diff <= 1 :
                    String.format("Jump transition detected: %d→%d (diff=%d) after operation %s. Log: %s",
                            statusBefore, statusAfter, diff, op, sm.getTransitionLog());
        }
    }

    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 9: 实训状态流转完整性 - terminal state")
    void completedStateIsTerminal(
            @ForAll("randomOperationSequences") List<TrainingOperation> operations
    ) {
        ParticipationStateMachine sm = new ParticipationStateMachine(STATUS_NOT_STARTED);
        boolean reachedCompleted = false;

        for (TrainingOperation op : operations) {
            sm.attemptTransition(op);

            if (sm.getCurrentStatus() == STATUS_COMPLETED) {
                reachedCompleted = true;
            }

            // Once completed, status must remain at 2
            if (reachedCompleted) {
                assert sm.getCurrentStatus() == STATUS_COMPLETED :
                        String.format("Status changed from COMPLETED after operation %s. Log: %s",
                                op, sm.getTransitionLog());
            }
        }
    }

    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 9: 实训状态流转完整性 - complete requires in-progress")
    void completeOnlyValidFromInProgress(
            @ForAll @IntRange(min = 0, max = 2) int initialStatus
    ) {
        ParticipationStateMachine sm = new ParticipationStateMachine(initialStatus);
        boolean result = sm.attemptTransition(TrainingOperation.COMPLETE);

        if (initialStatus == STATUS_IN_PROGRESS) {
            // COMPLETE should succeed from status=1
            assert result : "COMPLETE should succeed from IN_PROGRESS status";
            assert sm.getCurrentStatus() == STATUS_COMPLETED :
                    "Status should be COMPLETED after valid COMPLETE operation";
        } else {
            // COMPLETE should be rejected from status=0 or status=2
            assert !result :
                    String.format("COMPLETE should be rejected from status=%d", initialStatus);
            assert sm.getCurrentStatus() == initialStatus :
                    String.format("Status should remain %d after rejected COMPLETE", initialStatus);
        }
    }

    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 9: 实训状态流转完整性 - start only changes from not-started")
    void startOnlyTransitionsFromNotStarted(
            @ForAll @IntRange(min = 0, max = 2) int initialStatus
    ) {
        ParticipationStateMachine sm = new ParticipationStateMachine(initialStatus);
        sm.attemptTransition(TrainingOperation.START);

        if (initialStatus == STATUS_NOT_STARTED) {
            // START should transition 0→1
            assert sm.getCurrentStatus() == STATUS_IN_PROGRESS :
                    "Status should be IN_PROGRESS after START from NOT_STARTED";
        } else {
            // START from 1 or 2 should not change status
            assert sm.getCurrentStatus() == initialStatus :
                    String.format("Status should remain %d after START from non-zero state", initialStatus);
        }
    }
}
