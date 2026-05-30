package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartconstruct.backend_core.entity.BizAdminClass;
import com.smartconstruct.backend_core.entity.BizMajor;
import com.smartconstruct.backend_core.entity.BizStudent;
import com.smartconstruct.backend_core.entity.SysUser;
import com.smartconstruct.backend_core.mapper.WfNodeInstanceMapper;
import com.smartconstruct.backend_core.mapper.WfStudentNodeProgressMapper;
import com.smartconstruct.backend_core.service.*;
import net.jqwik.api.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property 3: Seeder 幂等性
 *
 * <b>Validates: Requirements 2.5</b>
 *
 * For any set of existing demo student usernames, repeatedly executing
 * DemoDataSeeder.createDemoStudents() SHALL NOT produce duplicate records
 * and SHALL NOT throw exceptions. Existing records are skipped based on
 * username uniqueness check.
 *
 * Label: Feature: student-training-demo, Property 3: Seeder 幂等性
 */
class DemoDataSeederIdempotencyPropertyTest {

    /**
     * Creates a DemoDataSeederService with a simulated in-memory "database"
     * that properly tracks which users have been created, enabling true
     * idempotency testing across multiple invocations.
     *
     * The mock uses a sequential counter to determine which username is being
     * queried. Since createDemoStudents iterates from 1..count sequentially,
     * we track the current position within each batch call.
     */
    private DemoDataSeederService createSeederWithSimulatedDb(
            Set<String> simulatedDb,
            AtomicInteger saveUserCount,
            AtomicInteger saveStudentCount) {

        SysUserService sysUserService = mock(SysUserService.class);
        IStudentService studentService = mock(IStudentService.class);
        IAdminClassService adminClassService = mock(IAdminClassService.class);
        IMajorService majorService = mock(IMajorService.class);
        ITrainingTemplateService trainingTemplateService = mock(ITrainingTemplateService.class);
        ITrainingTaskService trainingTaskService = mock(ITrainingTaskService.class);
        ITrainingParticipationService participationService = mock(ITrainingParticipationService.class);
        INodeDefService nodeDefService = mock(INodeDefService.class);
        WfNodeInstanceMapper nodeInstanceMapper = mock(WfNodeInstanceMapper.class);
        WfStudentNodeProgressMapper nodeProgressMapper = mock(WfStudentNodeProgressMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        ObjectMapper objectMapper = new ObjectMapper();

        // Mock adminClassService to return a valid class
        BizAdminClass adminClass = new BizAdminClass();
        adminClass.setId(1L);
        adminClass.setMajorId(100L);
        adminClass.setClassName("26软件工程1班");
        when(adminClassService.getOne(any(LambdaQueryWrapper.class))).thenReturn(adminClass);

        // Mock majorService
        BizMajor major = new BizMajor();
        major.setDeptId(10L);
        when(majorService.getById(100L)).thenReturn(major);

        // Mock passwordEncoder
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$mockBCryptHash");

        // Use a sequential counter to track which username is being queried.
        // createDemoStudents iterates i=1..count, calling getOne for each username.
        // We use a global counter and derive the username index from it.
        final AtomicInteger queryCounter = new AtomicInteger(0);
        when(sysUserService.getOne(any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            int callIndex = queryCounter.getAndIncrement();
            // The createDemoStudents method queries usernames sequentially:
            // demo_student_01, demo_student_02, ..., demo_student_N
            // Each call to createDemoStudents(count) makes exactly 'count' getOne calls.
            // We need to figure out which username this call corresponds to.
            // Since we can't extract from LambdaQueryWrapper, we track via a separate
            // mechanism: we know the pattern is always sequential within each invocation.
            // We'll use a secondary list to track the expected query sequence.
            return null; // Will be overridden below
        });

        // Better approach: track which username is being queried by intercepting
        // the adminClassService.getOne call (called once per createDemoStudents invocation)
        // and then using a per-invocation counter for sysUserService.getOne calls.
        // 
        // Actually, the simplest correct approach: since we can't extract the username
        // from LambdaQueryWrapper in a mock, we use the fact that createDemoStudents
        // always queries usernames in order (demo_student_01, 02, ..., N).
        // We maintain a "current batch position" that resets at the start of each batch.
        // We detect batch boundaries by tracking the adminClassService.getOne calls.
        final AtomicInteger batchQueryIndex = new AtomicInteger(0);

        // Reset and re-mock with proper tracking
        reset(sysUserService);
        reset(adminClassService);

        // adminClassService.getOne is called once at the start of each createDemoStudents call
        when(adminClassService.getOne(any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            batchQueryIndex.set(0); // Reset the per-batch counter
            return adminClass;
        });

        when(sysUserService.getOne(any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            int idx = batchQueryIndex.getAndIncrement() + 1; // 1-based index
            String username = String.format("demo_student_%02d", idx);

            if (simulatedDb.contains(username)) {
                SysUser existing = new SysUser();
                existing.setId((long) idx);
                existing.setUsername(username);
                return existing;
            }
            return null;
        });

        // Track saves and add to simulated DB
        when(sysUserService.save(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId((long) (saveUserCount.incrementAndGet()));
            simulatedDb.add(user.getUsername());
            return true;
        });

        when(studentService.save(any(BizStudent.class))).thenAnswer(invocation -> {
            saveStudentCount.incrementAndGet();
            return true;
        });

        return new DemoDataSeederService(
                trainingTemplateService,
                sysUserService,
                studentService,
                trainingTaskService,
                adminClassService,
                majorService,
                participationService,
                nodeDefService,
                nodeInstanceMapper,
                nodeProgressMapper,
                passwordEncoder,
                objectMapper
        );
    }

    @Provide
    Arbitrary<Set<String>> existingUsernameSubsets() {
        // Generate random subsets of demo_student_01 through demo_student_20
        return Arbitraries.integers().between(1, 20)
                .set().ofMinSize(0).ofMaxSize(20)
                .map(indices -> indices.stream()
                        .map(i -> String.format("demo_student_%02d", i))
                        .collect(Collectors.toSet()));
    }

    @Provide
    Arbitrary<Integer> studentCounts() {
        return Arbitraries.integers().between(1, 20);
    }

    /**
     * Property: Repeated execution of createDemoStudents with the same count
     * produces no additional records after the first execution.
     *
     * Simulates: first call creates all users, second call finds them all existing
     * and skips all — resulting in zero additional saves.
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 3: Seeder 幂等性 - repeated execution no duplicates")
    void repeatedExecutionProducesNoDuplicates(
            @ForAll("studentCounts") int count
    ) {
        // Simulated DB starts empty
        Set<String> simulatedDb = new HashSet<>();
        AtomicInteger saveUserCount = new AtomicInteger(0);
        AtomicInteger saveStudentCount = new AtomicInteger(0);

        DemoDataSeederService seeder = createSeederWithSimulatedDb(
                simulatedDb, saveUserCount, saveStudentCount);

        // First execution: should create 'count' users
        assertDoesNotThrow(() -> seeder.createDemoStudents(count));
        int firstRunSaves = saveUserCount.get();
        assertEquals(count, firstRunSaves,
                "First execution should create exactly 'count' users");

        // Record saves after first run
        int savesAfterFirstRun = saveUserCount.get();

        // Second execution: should skip all existing users (no new saves)
        assertDoesNotThrow(() -> seeder.createDemoStudents(count));
        int secondRunSaves = saveUserCount.get() - savesAfterFirstRun;
        assertEquals(0, secondRunSaves,
                "Second execution should not create any additional users (idempotent)");

        // Total user saves should equal student saves (1:1 relationship)
        assertEquals(saveUserCount.get(), saveStudentCount.get(),
                "Each user save should have a corresponding student save");
    }

    /**
     * Property: When all usernames in the requested range already exist,
     * createDemoStudents creates zero new records and throws no exceptions.
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 3: Seeder 幂等性 - all existing skipped")
    void allExistingUsernamesAreSkipped(
            @ForAll("studentCounts") int count
    ) {
        // Pre-populate simulated DB with all usernames that will be requested
        Set<String> simulatedDb = new HashSet<>();
        for (int i = 1; i <= count; i++) {
            simulatedDb.add(String.format("demo_student_%02d", i));
        }

        AtomicInteger saveUserCount = new AtomicInteger(0);
        AtomicInteger saveStudentCount = new AtomicInteger(0);

        DemoDataSeederService seeder = createSeederWithSimulatedDb(
                simulatedDb, saveUserCount, saveStudentCount);

        // Execution should skip all and not throw
        assertDoesNotThrow(() -> seeder.createDemoStudents(count));

        assertEquals(0, saveUserCount.get(),
                "No users should be created when all already exist");
        assertEquals(0, saveStudentCount.get(),
                "No students should be created when all users already exist");
    }

    /**
     * Property: For any subset of pre-existing usernames, createDemoStudents
     * creates exactly (count - existingCount) new records and skips the rest.
     * The total record count after execution equals 'count' (no duplicates).
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 3: Seeder 幂等性 - partial existing correct count")
    void partialExistingProducesCorrectCount(
            @ForAll("existingUsernameSubsets") Set<String> existingUsernames
    ) {
        int count = 20; // Always request 20 students
        Set<String> simulatedDb = new HashSet<>(existingUsernames);

        AtomicInteger saveUserCount = new AtomicInteger(0);
        AtomicInteger saveStudentCount = new AtomicInteger(0);

        DemoDataSeederService seeder = createSeederWithSimulatedDb(
                simulatedDb, saveUserCount, saveStudentCount);

        // Calculate expected new creates
        int expectedNewCreates = count - existingUsernames.size();

        // Execution should not throw
        assertDoesNotThrow(() -> seeder.createDemoStudents(count));

        assertEquals(expectedNewCreates, saveUserCount.get(),
                "Should create exactly (count - existing) new users");
        assertEquals(expectedNewCreates, saveStudentCount.get(),
                "Should create exactly (count - existing) new students");

        // After execution, simulated DB should contain all 20 usernames
        assertEquals(count, simulatedDb.size(),
                "After execution, all 20 usernames should exist (no duplicates)");
    }

    /**
     * Property: createDemoStudents never throws an exception regardless of
     * which usernames already exist in the database.
     */
    @Property(tries = 100)
    @Label("Feature: student-training-demo, Property 3: Seeder 幂等性 - no exceptions thrown")
    void noExceptionsThrownRegardlessOfExistingState(
            @ForAll("existingUsernameSubsets") Set<String> existingUsernames,
            @ForAll("studentCounts") int count
    ) {
        Set<String> simulatedDb = new HashSet<>(existingUsernames);
        AtomicInteger saveUserCount = new AtomicInteger(0);
        AtomicInteger saveStudentCount = new AtomicInteger(0);

        DemoDataSeederService seeder = createSeederWithSimulatedDb(
                simulatedDb, saveUserCount, saveStudentCount);

        // Should never throw regardless of existing state
        assertDoesNotThrow(() -> seeder.createDemoStudents(count),
                "createDemoStudents should never throw exceptions for any existing username state");
    }
}
