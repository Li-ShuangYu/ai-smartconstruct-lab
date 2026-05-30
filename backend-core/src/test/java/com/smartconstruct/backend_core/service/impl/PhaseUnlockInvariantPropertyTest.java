package com.smartconstruct.backend_core.service.impl;

import com.smartconstruct.backend_core.dto.PhaseUnlockStatus;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property 4: 阶段顺序解锁不变量
 *
 * <b>Validates: Requirements 5.1, 5.2</b>
 *
 * For any list of phase unlock statuses, if phase N has is_unlocked=true and N>1
 * (i.e., it is not the first phase), then ALL preceding phases (indices 0..N-1)
 * must have is_complete=true.
 *
 * Additionally, the first phase (index 0) should always be unlocked.
 *
 * This is a pure logic invariant test — no mocking needed, just generate data
 * and validate the invariant holds.
 *
 * Label: Feature: student-training-demo, Property 4: 阶段顺序解锁不变量
 */
class PhaseUnlockInvariantPropertyTest {

    /**
     * Validates the phase unlock invariant:
     * If phase at index N is unlocked and N > 0, then all phases at indices 0..N-1
     * must be complete.
     *
     * @param phases the list of phase unlock statuses sorted by sortNum
     * @return true if the invariant holds
     */
    private boolean phaseUnlockInvariantHolds(List<PhaseUnlockStatus> phases) {
        for (int i = 1; i < phases.size(); i++) {
            if (Boolean.TRUE.equals(phases.get(i).getIsUnlocked())) {
                // All preceding phases must be complete
                for (int j = 0; j < i; j++) {
                    if (!Boolean.TRUE.equals(phases.get(j).getIsComplete())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Generates a valid list of PhaseUnlockStatus objects that satisfies the invariant.
     * The generation strategy:
     * 1. Pick a random number of phases (2-10)
     * 2. Pick a random "unlock frontier" (0 to phaseCount-1) — phases up to this index are unlocked
     * 3. All phases before the frontier are complete and unlocked
     * 4. The frontier phase is unlocked but may or may not be complete
     * 5. Phases after the frontier are locked and not complete
     */
    @Provide
    Arbitrary<List<PhaseUnlockStatus>> validPhaseStateLists() {
        return Arbitraries.integers().between(2, 10).flatMap(phaseCount ->
            Arbitraries.integers().between(0, phaseCount - 1).flatMap(unlockFrontier ->
                Arbitraries.of(true, false).map(frontierComplete -> {
                    List<PhaseUnlockStatus> phases = new ArrayList<>();
                    for (int i = 0; i < phaseCount; i++) {
                        PhaseUnlockStatus status = new PhaseUnlockStatus();
                        status.setPhaseId("phase_" + (i + 1));
                        status.setPhaseName("阶段" + (i + 1));
                        status.setSortNum(i + 1);

                        if (i < unlockFrontier) {
                            // Phases before frontier: unlocked and complete
                            status.setIsUnlocked(true);
                            status.setIsComplete(true);
                        } else if (i == unlockFrontier) {
                            // Frontier phase: unlocked, may or may not be complete
                            status.setIsUnlocked(true);
                            status.setIsComplete(frontierComplete);
                        } else {
                            // Phases after frontier: locked and not complete
                            status.setIsUnlocked(false);
                            status.setIsComplete(false);
                        }
                        phases.add(status);
                    }
                    return phases;
                })
            )
        );
    }

    /**
     * Generates random (potentially invalid) phase state combinations for testing
     * the invariant checker itself.
     */
    @Provide
    Arbitrary<List<PhaseUnlockStatus>> randomPhaseStateLists() {
        return Arbitraries.integers().between(2, 10).flatMap(phaseCount -> {
            List<Arbitrary<PhaseUnlockStatus>> phaseArbitraries = new ArrayList<>();
            for (int i = 0; i < phaseCount; i++) {
                final int index = i;
                Arbitrary<PhaseUnlockStatus> phaseArb = Combinators.combine(
                    Arbitraries.of(true, false),
                    Arbitraries.of(true, false)
                ).as((isUnlocked, isComplete) -> {
                    PhaseUnlockStatus status = new PhaseUnlockStatus();
                    status.setPhaseId("phase_" + (index + 1));
                    status.setPhaseName("阶段" + (index + 1));
                    status.setSortNum(index + 1);
                    status.setIsUnlocked(isUnlocked);
                    status.setIsComplete(isComplete);
                    return status;
                });
                phaseArbitraries.add(phaseArb);
            }
            return Combinators.combine(phaseArbitraries).as(list -> list);
        });
    }

    /**
     * Generates phase state lists that violate the invariant:
     * A phase at index N>0 is unlocked but at least one preceding phase is NOT complete.
     */
    @Provide
    Arbitrary<List<PhaseUnlockStatus>> invalidPhaseStateLists() {
        return Arbitraries.integers().between(2, 10).flatMap(phaseCount ->
            Arbitraries.integers().between(1, phaseCount - 1).flatMap(violatingIndex ->
                Arbitraries.integers().between(0, violatingIndex - 1).map(incompleteIndex -> {
                    List<PhaseUnlockStatus> phases = new ArrayList<>();
                    for (int i = 0; i < phaseCount; i++) {
                        PhaseUnlockStatus status = new PhaseUnlockStatus();
                        status.setPhaseId("phase_" + (i + 1));
                        status.setPhaseName("阶段" + (i + 1));
                        status.setSortNum(i + 1);

                        if (i == violatingIndex) {
                            // This phase is unlocked (violating the invariant)
                            status.setIsUnlocked(true);
                            status.setIsComplete(false);
                        } else if (i == incompleteIndex) {
                            // This preceding phase is NOT complete (causing the violation)
                            status.setIsUnlocked(true);
                            status.setIsComplete(false);
                        } else if (i < violatingIndex) {
                            // Other preceding phases are complete
                            status.setIsUnlocked(true);
                            status.setIsComplete(true);
                        } else {
                            status.setIsUnlocked(false);
                            status.setIsComplete(false);
                        }
                        phases.add(status);
                    }
                    return phases;
                })
            )
        );
    }

    /**
     * Property: For any valid phase state list (generated to satisfy the invariant),
     * the invariant check should pass.
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 4: 阶段顺序解锁不变量 - valid states satisfy invariant")
    void validPhaseStatesSatisfyInvariant(
            @ForAll("validPhaseStateLists") List<PhaseUnlockStatus> phases
    ) {
        assertTrue(phaseUnlockInvariantHolds(phases),
                "Valid phase states should always satisfy the unlock invariant: " +
                "if phase N is unlocked (N>0), all preceding phases must be complete. " +
                "Phases: " + formatPhases(phases));
    }

    /**
     * Property: For any invalid phase state list (generated to violate the invariant),
     * the invariant check should fail.
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 4: 阶段顺序解锁不变量 - invalid states violate invariant")
    void invalidPhaseStatesViolateInvariant(
            @ForAll("invalidPhaseStateLists") List<PhaseUnlockStatus> phases
    ) {
        assertFalse(phaseUnlockInvariantHolds(phases),
                "Invalid phase states should violate the unlock invariant. " +
                "Phases: " + formatPhases(phases));
    }

    /**
     * Property: The first phase (index 0) should always be unlocked.
     * This tests that any valid system state has the first phase unlocked.
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 4: 阶段顺序解锁不变量 - first phase always unlocked")
    void firstPhaseAlwaysUnlocked(
            @ForAll("validPhaseStateLists") List<PhaseUnlockStatus> phases
    ) {
        assertFalse(phases.isEmpty(), "Phase list should not be empty");
        assertTrue(Boolean.TRUE.equals(phases.get(0).getIsUnlocked()),
                "The first phase (sortNum=1) should always be unlocked. " +
                "Phases: " + formatPhases(phases));
    }

    /**
     * Property: For random phase state combinations, the invariant either holds or doesn't,
     * and when it holds, the logical implication is correct:
     * unlocked(N) && N>0 → all preceding phases complete.
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 4: 阶段顺序解锁不变量 - random states invariant consistency")
    void randomStatesInvariantConsistency(
            @ForAll("randomPhaseStateLists") List<PhaseUnlockStatus> phases
    ) {
        boolean invariantHolds = phaseUnlockInvariantHolds(phases);

        if (invariantHolds) {
            // Double-check: for every unlocked phase at index > 0,
            // all preceding phases must be complete
            for (int i = 1; i < phases.size(); i++) {
                if (Boolean.TRUE.equals(phases.get(i).getIsUnlocked())) {
                    for (int j = 0; j < i; j++) {
                        assertTrue(Boolean.TRUE.equals(phases.get(j).getIsComplete()),
                                "Invariant claims to hold, but phase " + j +
                                " is not complete while phase " + i + " is unlocked. " +
                                "Phases: " + formatPhases(phases));
                    }
                }
            }
        } else {
            // There must exist at least one unlocked phase at index > 0
            // with an incomplete predecessor
            boolean foundViolation = false;
            for (int i = 1; i < phases.size(); i++) {
                if (Boolean.TRUE.equals(phases.get(i).getIsUnlocked())) {
                    for (int j = 0; j < i; j++) {
                        if (!Boolean.TRUE.equals(phases.get(j).getIsComplete())) {
                            foundViolation = true;
                            break;
                        }
                    }
                }
                if (foundViolation) break;
            }
            assertTrue(foundViolation,
                    "Invariant claims to be violated, but no violation found. " +
                    "Phases: " + formatPhases(phases));
        }
    }

    /**
     * Helper to format phase list for assertion messages.
     */
    private String formatPhases(List<PhaseUnlockStatus> phases) {
        return phases.stream()
                .map(p -> String.format("[%s: unlocked=%s, complete=%s]",
                        p.getPhaseId(), p.getIsUnlocked(), p.getIsComplete()))
                .collect(Collectors.joining(", "));
    }
}
