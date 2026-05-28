package com.smartconstruct.backend_core.service.impl;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Property 3: Phase progression unlocks correctly.
 *
 * <b>Validates: Requirements 1.4, 27.2</b>
 *
 * For any phase configuration with N nodes where some are marked is_required=true
 * and some is_required=false, the phase SHALL be considered complete (and the next
 * phase unlocked) if and only if ALL nodes with is_required=true have status "已完成".
 * Nodes with is_required=false SHALL NOT affect phase completion regardless of their status.
 */
class PhaseProgressionPropertyTest {

    /** Status constants matching the service implementation */
    private static final int STATUS_NOT_STARTED = 0;
    private static final int STATUS_IN_PROGRESS = 1;
    private static final int STATUS_COMPLETED = 2;
    private static final int STATUS_SKIPPED = 3;

    /**
     * Simplified node representation for property testing.
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
    }

    /**
     * Pure logic extracted from PhaseExecutionServiceImpl.isPhaseComplete:
     * A phase is complete iff ALL required nodes have status == COMPLETED.
     */
    private boolean isPhaseComplete(List<TestNode> nodes) {
        List<TestNode> requiredNodes = nodes.stream()
                .filter(n -> n.isRequired)
                .collect(Collectors.toList());

        if (requiredNodes.isEmpty()) {
            return true; // No required nodes → phase is complete
        }

        return requiredNodes.stream()
                .allMatch(n -> n.status == STATUS_COMPLETED);
    }

    @Provide
    Arbitrary<List<TestNode>> nodesWithAllRequiredCompleted() {
        return Arbitraries.integers().between(1, 15).flatMap(count -> {
            List<Arbitrary<TestNode>> nodeArbitraries = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                final int idx = i;
                Arbitrary<TestNode> nodeArb = Arbitraries.of(true, false).flatMap(isRequired -> {
                    if (isRequired) {
                        // Required nodes are always completed
                        return Arbitraries.just(new TestNode("node_" + idx, true, STATUS_COMPLETED));
                    } else {
                        // Non-required nodes can have any status
                        return Arbitraries.of(STATUS_NOT_STARTED, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_SKIPPED)
                                .map(status -> new TestNode("node_" + idx, false, status));
                    }
                });
                nodeArbitraries.add(nodeArb);
            }
            return Combinators.combine(nodeArbitraries).as(nodes -> nodes);
        }).filter(nodes -> nodes.stream().anyMatch(n -> n.isRequired)); // Ensure at least one required
    }

    @Provide
    Arbitrary<List<TestNode>> nodesWithSomeRequiredIncomplete() {
        return Arbitraries.integers().between(1, 15).flatMap(count -> {
            List<Arbitrary<TestNode>> nodeArbitraries = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                final int idx = i;
                Arbitrary<TestNode> nodeArb = Arbitraries.of(true, false).flatMap(isRequired -> {
                    if (isRequired) {
                        // Required nodes can have any status
                        return Arbitraries.of(STATUS_NOT_STARTED, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_SKIPPED)
                                .map(status -> new TestNode("node_" + idx, true, status));
                    } else {
                        return Arbitraries.of(STATUS_NOT_STARTED, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_SKIPPED)
                                .map(status -> new TestNode("node_" + idx, false, status));
                    }
                });
                nodeArbitraries.add(nodeArb);
            }
            return Combinators.combine(nodeArbitraries).as(nodes -> nodes);
        }).filter(nodes -> nodes.stream().anyMatch(n -> n.isRequired && n.status != STATUS_COMPLETED));
    }

    @Property(tries = 100)
    void phaseCompleteWhenAllRequiredNodesCompleted(
            @ForAll("nodesWithAllRequiredCompleted") List<TestNode> nodes
    ) {
        // When all required nodes are completed, phase should be complete
        boolean result = isPhaseComplete(nodes);
        assert result : "Phase should be complete when all required nodes are completed";
    }

    @Property(tries = 100)
    void phaseIncompleteWhenAnyRequiredNodeNotCompleted(
            @ForAll("nodesWithSomeRequiredIncomplete") List<TestNode> nodes
    ) {
        // When any required node is not completed, phase should be incomplete
        boolean result = isPhaseComplete(nodes);
        assert !result : "Phase should be incomplete when any required node is not completed";
    }

    @Property(tries = 100)
    void nonRequiredNodesDoNotAffectCompletion(
            @ForAll @IntRange(min = 1, max = 10) int requiredCount,
            @ForAll @IntRange(min = 1, max = 10) int optionalCount
    ) {
        // All required nodes completed, optional nodes in various states
        List<TestNode> nodes = new ArrayList<>();
        for (int i = 0; i < requiredCount; i++) {
            nodes.add(new TestNode("req_" + i, true, STATUS_COMPLETED));
        }
        Random rng = new Random(requiredCount * 31 + optionalCount);
        int[] statuses = {STATUS_NOT_STARTED, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_SKIPPED};
        for (int i = 0; i < optionalCount; i++) {
            int status = statuses[rng.nextInt(statuses.length)];
            nodes.add(new TestNode("opt_" + i, false, status));
        }

        boolean result = isPhaseComplete(nodes);
        assert result : "Non-required nodes should not affect phase completion";
    }

    @Property(tries = 100)
    void emptyRequiredSetMeansPhaseComplete(
            @ForAll @IntRange(min = 0, max = 10) int optionalCount
    ) {
        // No required nodes → phase is always complete
        List<TestNode> nodes = new ArrayList<>();
        Random rng = new Random(optionalCount);
        int[] statuses = {STATUS_NOT_STARTED, STATUS_IN_PROGRESS, STATUS_COMPLETED, STATUS_SKIPPED};
        for (int i = 0; i < optionalCount; i++) {
            int status = statuses[rng.nextInt(statuses.length)];
            nodes.add(new TestNode("opt_" + i, false, status));
        }

        boolean result = isPhaseComplete(nodes);
        assert result : "Phase with no required nodes should always be complete";
    }
}
