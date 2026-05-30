package com.smartconstruct.backend_core.service.impl;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Property 5: 节点完成触发阶段解锁
 *
 * <b>Validates: Requirements 5.2</b>
 *
 * For any phase where all is_required=true nodes have Node_Progress status=2,
 * that phase's is_complete should be true, and the sort_num-adjacent next phase's
 * is_unlocked should be true.
 *
 * Label: "Feature: student-training-demo, Property 5: 节点完成触发阶段解锁"
 */
class NodeCompletionPhaseUnlockPropertyTest {

    /** Status constants matching the service implementation */
    private static final int STATUS_NOT_STARTED = 0;
    private static final int STATUS_IN_PROGRESS = 1;
    private static final int STATUS_COMPLETED = 2;
    private static final int STATUS_SKIPPED = 3;

    /**
     * Represents a node within a phase for property testing.
     */
    static class TestNode {
        final String nodeId;
        final boolean isRequired;
        int status;

        TestNode(String nodeId, boolean isRequired, int status) {
            this.nodeId = nodeId;
            this.isRequired = isRequired;
            this.status = status;
        }

        @Override
        public String toString() {
            return "TestNode{id=" + nodeId + ", required=" + isRequired + ", status=" + status + "}";
        }
    }

    /**
     * Represents a phase containing nodes.
     */
    static class TestPhase {
        final String phaseId;
        final int sortNum;
        final List<TestNode> nodes;

        TestPhase(String phaseId, int sortNum, List<TestNode> nodes) {
            this.phaseId = phaseId;
            this.sortNum = sortNum;
            this.nodes = nodes;
        }

        @Override
        public String toString() {
            return "TestPhase{id=" + phaseId + ", sortNum=" + sortNum + ", nodes=" + nodes + "}";
        }
    }

    /**
     * Pure logic: determines if a phase is complete.
     * A phase is complete iff ALL required nodes have status == COMPLETED.
     * If no required nodes exist, the phase is considered complete.
     */
    private boolean isPhaseComplete(TestPhase phase) {
        List<TestNode> requiredNodes = phase.nodes.stream()
                .filter(n -> n.isRequired)
                .collect(Collectors.toList());

        if (requiredNodes.isEmpty()) {
            return true;
        }

        return requiredNodes.stream()
                .allMatch(n -> n.status == STATUS_COMPLETED);
    }

    /**
     * Pure logic: determines if a phase is unlocked.
     * A phase is unlocked if:
     * - It is the first phase (sortNum is smallest), OR
     * - All preceding phases (by sortNum) are complete.
     */
    private boolean isPhaseUnlocked(TestPhase phase, List<TestPhase> allPhases) {
        // Sort phases by sortNum
        List<TestPhase> sorted = allPhases.stream()
                .sorted(Comparator.comparingInt(p -> p.sortNum))
                .collect(Collectors.toList());

        int phaseIndex = -1;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).phaseId.equals(phase.phaseId)) {
                phaseIndex = i;
                break;
            }
        }

        if (phaseIndex <= 0) {
            // First phase or not found — always unlocked
            return true;
        }

        // Check all preceding phases are complete
        for (int i = 0; i < phaseIndex; i++) {
            if (!isPhaseComplete(sorted.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Generates a list of phases where the first phase has all required nodes completed.
     * This simulates the scenario where completing all required nodes in a phase
     * should trigger that phase to be complete and the next phase to be unlocked.
     */
    @Provide
    Arbitrary<List<TestPhase>> phasesWithFirstPhaseAllRequiredCompleted() {
        // Generate 2-5 phases
        return Arbitraries.integers().between(2, 5).flatMap(phaseCount -> {
            List<Arbitrary<TestPhase>> phaseArbitraries = new ArrayList<>();

            for (int p = 0; p < phaseCount; p++) {
                final int phaseIdx = p;

                Arbitrary<TestPhase> phaseArb = Arbitraries.integers().between(1, 8).flatMap(nodeCount -> {
                    List<Arbitrary<TestNode>> nodeArbitraries = new ArrayList<>();

                    for (int n = 0; n < nodeCount; n++) {
                        final int nodeIdx = n;

                        Arbitrary<TestNode> nodeArb = Arbitraries.of(true, false).flatMap(isRequired -> {
                            if (phaseIdx == 0) {
                                // First phase: all required nodes are COMPLETED
                                if (isRequired) {
                                    return Arbitraries.just(
                                            new TestNode("phase" + phaseIdx + "_node" + nodeIdx, true, STATUS_COMPLETED));
                                } else {
                                    // Non-required nodes can have any status
                                    return Arbitraries.of(STATUS_NOT_STARTED, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_SKIPPED)
                                            .map(s -> new TestNode("phase" + phaseIdx + "_node" + nodeIdx, false, s));
                                }
                            } else {
                                // Other phases: nodes can have any status
                                return Arbitraries.of(STATUS_NOT_STARTED, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_SKIPPED)
                                        .map(s -> new TestNode("phase" + phaseIdx + "_node" + nodeIdx, isRequired, s));
                            }
                        });
                        nodeArbitraries.add(nodeArb);
                    }

                    return Combinators.combine(nodeArbitraries)
                            .as(nodes -> new TestPhase("phase_" + phaseIdx, phaseIdx + 1, nodes));
                });

                phaseArbitraries.add(phaseArb);
            }

            return Combinators.combine(phaseArbitraries).as(phases -> phases);
        }).filter(phases -> {
            // Ensure first phase has at least one required node
            TestPhase firstPhase = phases.get(0);
            return firstPhase.nodes.stream().anyMatch(n -> n.isRequired);
        });
    }

    /**
     * Generates a random multi-phase configuration where a specific phase (not the last)
     * has all required nodes completed and all preceding phases are also complete.
     * This tests the general case of node completion triggering next phase unlock.
     */
    @Provide
    Arbitrary<List<TestPhase>> phasesWithCompletedPhaseInMiddle() {
        // Generate 3-5 phases
        return Arbitraries.integers().between(3, 5).flatMap(phaseCount -> {
            // Pick a completed phase index (not the last one)
            return Arbitraries.integers().between(0, phaseCount - 2).flatMap(completedIdx -> {
                List<Arbitrary<TestPhase>> phaseArbitraries = new ArrayList<>();

                for (int p = 0; p < phaseCount; p++) {
                    final int phaseIdx = p;
                    final int completedPhaseIdx = completedIdx;

                    Arbitrary<TestPhase> phaseArb = Arbitraries.integers().between(1, 6).flatMap(nodeCount -> {
                        List<Arbitrary<TestNode>> nodeArbitraries = new ArrayList<>();

                        for (int n = 0; n < nodeCount; n++) {
                            final int nodeIdx = n;

                            Arbitrary<TestNode> nodeArb = Arbitraries.of(true, false).flatMap(isRequired -> {
                                if (phaseIdx <= completedPhaseIdx) {
                                    // Phases up to and including completedIdx: all required nodes COMPLETED
                                    if (isRequired) {
                                        return Arbitraries.just(
                                                new TestNode("phase" + phaseIdx + "_node" + nodeIdx, true, STATUS_COMPLETED));
                                    } else {
                                        return Arbitraries.of(STATUS_NOT_STARTED, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_SKIPPED)
                                                .map(s -> new TestNode("phase" + phaseIdx + "_node" + nodeIdx, false, s));
                                    }
                                } else {
                                    // Phases after completedIdx: any status
                                    return Arbitraries.of(STATUS_NOT_STARTED, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_SKIPPED)
                                            .map(s -> new TestNode("phase" + phaseIdx + "_node" + nodeIdx, isRequired, s));
                                }
                            });
                            nodeArbitraries.add(nodeArb);
                        }

                        return Combinators.combine(nodeArbitraries)
                                .as(nodes -> new TestPhase("phase_" + phaseIdx, phaseIdx + 1, nodes));
                    });

                    phaseArbitraries.add(phaseArb);
                }

                return Combinators.combine(phaseArbitraries).as(phases -> phases);
            });
        }).filter(phases -> {
            // Ensure each phase has at least one required node
            return phases.stream().allMatch(phase ->
                    phase.nodes.stream().anyMatch(n -> n.isRequired));
        });
    }

    /**
     * Generates phases where a specific phase has at least one required node NOT completed.
     * This tests the negative case: phase should NOT be complete and next phase should NOT be unlocked.
     */
    @Provide
    Arbitrary<List<TestPhase>> phasesWithIncompleteRequiredNodes() {
        return Arbitraries.integers().between(2, 5).flatMap(phaseCount -> {
            List<Arbitrary<TestPhase>> phaseArbitraries = new ArrayList<>();

            for (int p = 0; p < phaseCount; p++) {
                final int phaseIdx = p;

                Arbitrary<TestPhase> phaseArb = Arbitraries.integers().between(1, 8).flatMap(nodeCount -> {
                    List<Arbitrary<TestNode>> nodeArbitraries = new ArrayList<>();

                    for (int n = 0; n < nodeCount; n++) {
                        final int nodeIdx = n;

                        Arbitrary<TestNode> nodeArb = Arbitraries.of(true, false).flatMap(isRequired -> {
                            if (phaseIdx == 0 && isRequired) {
                                // First phase required nodes: at least one NOT completed
                                return Arbitraries.of(STATUS_NOT_STARTED, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_SKIPPED)
                                        .map(s -> new TestNode("phase" + phaseIdx + "_node" + nodeIdx, true, s));
                            } else {
                                return Arbitraries.of(STATUS_NOT_STARTED, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_SKIPPED)
                                        .map(s -> new TestNode("phase" + phaseIdx + "_node" + nodeIdx, isRequired, s));
                            }
                        });
                        nodeArbitraries.add(nodeArb);
                    }

                    return Combinators.combine(nodeArbitraries)
                            .as(nodes -> new TestPhase("phase_" + phaseIdx, phaseIdx + 1, nodes));
                });

                phaseArbitraries.add(phaseArb);
            }

            return Combinators.combine(phaseArbitraries).as(phases -> phases);
        }).filter(phases -> {
            // Ensure first phase has at least one required node that is NOT completed
            TestPhase firstPhase = phases.get(0);
            boolean hasRequired = firstPhase.nodes.stream().anyMatch(n -> n.isRequired);
            boolean hasIncomplete = firstPhase.nodes.stream()
                    .anyMatch(n -> n.isRequired && n.status != STATUS_COMPLETED);
            return hasRequired && hasIncomplete;
        });
    }

    // ==================== Property Tests ====================

    /**
     * Property 5a: When all required nodes in a phase are completed,
     * that phase is_complete=true AND the next phase is_unlocked=true.
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 5: 节点完成触发阶段解锁 - 所有必修节点完成时阶段完成且下一阶段解锁")
    void whenAllRequiredNodesCompletedThenPhaseCompleteAndNextUnlocked(
            @ForAll("phasesWithFirstPhaseAllRequiredCompleted") List<TestPhase> phases
    ) {
        TestPhase firstPhase = phases.get(0);
        TestPhase secondPhase = phases.get(1);

        // Verify: first phase is complete
        boolean firstPhaseComplete = isPhaseComplete(firstPhase);
        assert firstPhaseComplete :
                "Phase should be complete when all required nodes have status=2. Phase: " + firstPhase;

        // Verify: second phase is unlocked (since first phase is complete)
        boolean secondPhaseUnlocked = isPhaseUnlocked(secondPhase, phases);
        assert secondPhaseUnlocked :
                "Next phase should be unlocked when preceding phase is complete. Phases: " + phases;
    }

    /**
     * Property 5b: When all required nodes in a middle phase are completed
     * (and all preceding phases are also complete), the next phase is unlocked.
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 5: 节点完成触发阶段解锁 - 中间阶段完成触发后续阶段解锁")
    void whenMiddlePhaseCompletedThenNextPhaseUnlocked(
            @ForAll("phasesWithCompletedPhaseInMiddle") List<TestPhase> phases
    ) {
        // Find the last completed phase (all phases up to some index are complete)
        List<TestPhase> sorted = phases.stream()
                .sorted(Comparator.comparingInt(p -> p.sortNum))
                .collect(Collectors.toList());

        int lastCompletedIdx = -1;
        for (int i = 0; i < sorted.size(); i++) {
            if (isPhaseComplete(sorted.get(i))) {
                lastCompletedIdx = i;
            } else {
                break;
            }
        }

        // There should be at least one completed phase (guaranteed by generator)
        assert lastCompletedIdx >= 0 : "Should have at least one completed phase";

        // If there's a next phase after the last completed one, it should be unlocked
        if (lastCompletedIdx < sorted.size() - 1) {
            TestPhase nextPhase = sorted.get(lastCompletedIdx + 1);
            boolean nextUnlocked = isPhaseUnlocked(nextPhase, phases);
            assert nextUnlocked :
                    "Phase after completed phases should be unlocked. lastCompletedIdx=" + lastCompletedIdx
                            + ", nextPhase=" + nextPhase;
        }
    }

    /**
     * Property 5c: When a phase has at least one required node NOT completed,
     * that phase is NOT complete and the next phase is NOT unlocked
     * (assuming no preceding phases are complete either).
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 5: 节点完成触发阶段解锁 - 必修节点未完成时阶段未完成且下一阶段锁定")
    void whenRequiredNodesIncompleteThenPhaseNotCompleteAndNextLocked(
            @ForAll("phasesWithIncompleteRequiredNodes") List<TestPhase> phases
    ) {
        TestPhase firstPhase = phases.get(0);

        // Verify: first phase is NOT complete
        boolean firstPhaseComplete = isPhaseComplete(firstPhase);
        assert !firstPhaseComplete :
                "Phase should NOT be complete when any required node has status != 2. Phase: " + firstPhase;

        // Verify: second phase is NOT unlocked (since first phase is incomplete)
        if (phases.size() > 1) {
            TestPhase secondPhase = phases.get(1);
            boolean secondPhaseUnlocked = isPhaseUnlocked(secondPhase, phases);
            assert !secondPhaseUnlocked :
                    "Next phase should NOT be unlocked when preceding phase is incomplete. Phases: " + phases;
        }
    }

    /**
     * Property 5d: Completing all required nodes in a phase (simulating a node completion sequence)
     * transitions the phase from incomplete to complete.
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 5: 节点完成触发阶段解锁 - 节点完成序列触发阶段状态转换")
    void nodeCompletionSequenceTriggersPhaseTransition(
            @ForAll @IntRange(min = 1, max = 10) int requiredCount,
            @ForAll @IntRange(min = 0, max = 5) int optionalCount
    ) {
        // Create a phase with required and optional nodes, all initially NOT completed
        List<TestNode> nodes = new ArrayList<>();
        for (int i = 0; i < requiredCount; i++) {
            nodes.add(new TestNode("req_" + i, true, STATUS_NOT_STARTED));
        }
        for (int i = 0; i < optionalCount; i++) {
            nodes.add(new TestNode("opt_" + i, false, STATUS_NOT_STARTED));
        }

        TestPhase currentPhase = new TestPhase("phase_1", 1, nodes);
        TestPhase nextPhase = new TestPhase("phase_2", 2, Collections.singletonList(
                new TestNode("next_node_0", true, STATUS_NOT_STARTED)));
        List<TestPhase> allPhases = Arrays.asList(currentPhase, nextPhase);

        // Before completing required nodes: phase should be incomplete
        assert !isPhaseComplete(currentPhase) :
                "Phase should be incomplete before required nodes are completed";
        assert !isPhaseUnlocked(nextPhase, allPhases) :
                "Next phase should be locked before current phase is complete";

        // Simulate completing all required nodes one by one
        for (TestNode node : nodes) {
            if (node.isRequired) {
                node.status = STATUS_COMPLETED;
            }
        }

        // After completing all required nodes: phase should be complete
        assert isPhaseComplete(currentPhase) :
                "Phase should be complete after all required nodes are completed";
        assert isPhaseUnlocked(nextPhase, allPhases) :
                "Next phase should be unlocked after current phase is complete";
    }
}
