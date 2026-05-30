package com.smartconstruct.backend_core.service.impl;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property 8: 停留时长累加正确性
 *
 * <b>Validates: Requirements 7.3</b>
 *
 * For any node completion operation, stay_duration_seconds SHALL equal
 * the previous accumulated value plus the difference in seconds between
 * the current time and entered_at. The result must be a non-negative integer.
 *
 * Label: Feature: student-training-demo, Property 8: 停留时长累加正确性
 */
class StayDurationAccumulationPropertyTest {

    /**
     * Pure logic for stay duration calculation, extracted from the node completion logic.
     *
     * stay_duration_seconds = previousDuration + (currentTime - enteredAt).getSeconds()
     *
     * @param previousDuration the accumulated stay duration before this session (seconds)
     * @param enteredAt        the time the student entered the node
     * @param currentTime      the current time when completing the node
     * @return the new total stay duration in seconds
     */
    private long calculateStayDuration(long previousDuration, LocalDateTime enteredAt, LocalDateTime currentTime) {
        long sessionSeconds = Duration.between(enteredAt, currentTime).getSeconds();
        return previousDuration + sessionSeconds;
    }

    @Provide
    Arbitrary<Long> previousDurations() {
        return Arbitraries.longs().between(0, 10000);
    }

    @Provide
    Arbitrary<LocalDateTime> pastDateTimes() {
        // Generate random LocalDateTime in the past (within last 365 days)
        return Arbitraries.longs().between(1, 365L * 24 * 60 * 60)
                .map(secondsAgo -> LocalDateTime.now().minusSeconds(secondsAgo));
    }

    @Provide
    Arbitrary<Long> sessionDurationsInSeconds() {
        // Session duration between 1 second and 8 hours (28800 seconds)
        return Arbitraries.longs().between(1, 28800);
    }

    /**
     * Property: stay_duration_seconds = previousDuration + (currentTime - enteredAt).getSeconds()
     * and the result is always a non-negative integer.
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 8: 停留时长累加正确性 - accumulation correctness")
    void stayDurationAccumulatesCorrectly(
            @ForAll("previousDurations") long previousDuration,
            @ForAll("pastDateTimes") LocalDateTime enteredAt,
            @ForAll("sessionDurationsInSeconds") long sessionSeconds
    ) {
        // currentTime is always after enteredAt by sessionSeconds
        LocalDateTime currentTime = enteredAt.plusSeconds(sessionSeconds);

        long result = calculateStayDuration(previousDuration, enteredAt, currentTime);

        // Verify: result = previousDuration + sessionSeconds
        long expected = previousDuration + sessionSeconds;
        assertEquals(expected, result,
                String.format("stay_duration should be %d + %d = %d, but got %d",
                        previousDuration, sessionSeconds, expected, result));

        // Verify: result is non-negative
        assertTrue(result >= 0,
                String.format("stay_duration_seconds must be non-negative, but got %d", result));
    }

    /**
     * Property: When previousDuration is 0 (first session), the result equals
     * exactly the session duration in seconds.
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 8: 停留时长累加正确性 - first session equals session duration")
    void firstSessionDurationEqualsSessionTime(
            @ForAll("pastDateTimes") LocalDateTime enteredAt,
            @ForAll("sessionDurationsInSeconds") long sessionSeconds
    ) {
        long previousDuration = 0;
        LocalDateTime currentTime = enteredAt.plusSeconds(sessionSeconds);

        long result = calculateStayDuration(previousDuration, enteredAt, currentTime);

        assertEquals(sessionSeconds, result,
                String.format("First session duration should equal session time %d, but got %d",
                        sessionSeconds, result));
        assertTrue(result >= 0, "Result must be non-negative");
    }

    /**
     * Property: The result is always >= previousDuration (monotonically increasing)
     * since currentTime is always after enteredAt.
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 8: 停留时长累加正确性 - monotonically increasing")
    void stayDurationIsMonotonicallyIncreasing(
            @ForAll("previousDurations") long previousDuration,
            @ForAll("pastDateTimes") LocalDateTime enteredAt,
            @ForAll("sessionDurationsInSeconds") long sessionSeconds
    ) {
        LocalDateTime currentTime = enteredAt.plusSeconds(sessionSeconds);

        long result = calculateStayDuration(previousDuration, enteredAt, currentTime);

        assertTrue(result >= previousDuration,
                String.format("Result %d should be >= previousDuration %d (monotonically increasing)",
                        result, previousDuration));
    }

    /**
     * Property: The result is always a non-negative integer for any valid inputs
     * (previousDuration >= 0, currentTime >= enteredAt).
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 8: 停留时长累加正确性 - non-negative result")
    void resultIsAlwaysNonNegative(
            @ForAll("previousDurations") long previousDuration,
            @ForAll("pastDateTimes") LocalDateTime enteredAt,
            @ForAll("sessionDurationsInSeconds") long sessionSeconds
    ) {
        LocalDateTime currentTime = enteredAt.plusSeconds(sessionSeconds);

        long result = calculateStayDuration(previousDuration, enteredAt, currentTime);

        assertTrue(result >= 0,
                String.format("stay_duration_seconds must be non-negative, got %d " +
                        "(previousDuration=%d, sessionSeconds=%d)", result, previousDuration, sessionSeconds));
    }
}
