package com.smartconstruct.backend_core.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.entity.BizCourse;
import com.smartconstruct.backend_core.entity.BizStudentCourse;
import com.smartconstruct.backend_core.entity.SysUser;
import com.smartconstruct.backend_core.service.ICourseService;
import com.smartconstruct.backend_core.service.IStudentCourseService;
import com.smartconstruct.backend_core.service.SysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 课程数据种子初始化器
 *
 * 在应用启动时检查课程表是否为空，若为空则自动插入演示数据
 */
@Component
public class CourseDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CourseDataSeeder.class);

    @Autowired
    private ICourseService courseService;

    @Autowired
    private IStudentCourseService studentCourseService;

    @Autowired
    private SysUserService sysUserService;

    @Override
    public void run(String... args) {
        seedCoursesIfEmpty();
        seedStudentCoursesIfEmpty();
    }

    private void seedCoursesIfEmpty() {
        LambdaQueryWrapper<BizCourse> qw = new LambdaQueryWrapper<>();
        if (courseService.count(qw) == 0) {
            log.info("CourseDataSeeder: No courses found, seeding demo data...");

            List<BizCourse> courses = Arrays.asList(
                createCourse(1L, 2L, "Python算法入门", "本课程将带你从零开始学习Python编程和基础算法，包括数据结构、排序算法、查找算法等核心内容。", 1, 0, null),
                createCourse(2L, 2L, "数据结构与算法", "深入学习常用数据结构（链表、树、图等）和经典算法（动态规划、贪心算法等）。", 1, 1, "ABC123"),
                createCourse(3L, 3L, "人工智能导论", "介绍人工智能的基本概念、发展历程和核心技术，包括机器学习、深度学习等。", 1, 0, null),
                createCourse(4L, 3L, "机器学习实战", "通过实际案例学习机器学习算法的应用，包括分类、回归、聚类等。", 1, 1, "ML2024"),
                createCourse(5L, 2L, "软件工程实践", "学习软件开发的全过程，包括需求分析、设计、编码、测试和维护。", 1, 0, null)
            );

            courseService.saveBatch(courses);
            log.info("CourseDataSeeder: Seeded {} demo courses", courses.size());
        }
    }

    private void seedStudentCoursesIfEmpty() {
        LambdaQueryWrapper<BizStudentCourse> qw = new LambdaQueryWrapper<>();
        studentCourseService.remove(qw);
        
        log.info("CourseDataSeeder: Cleared existing student courses, seeding demo data...");

        LambdaQueryWrapper<SysUser> userQw = new LambdaQueryWrapper<>();
        userQw.eq(SysUser::getRoleType, 3);
        List<SysUser> students = sysUserService.list(userQw);

        if (students.isEmpty()) {
            log.warn("CourseDataSeeder: No students found, skipping student course seeding");
            return;
        }

        LambdaQueryWrapper<BizCourse> courseQw = new LambdaQueryWrapper<>();
        courseQw.orderByAsc(BizCourse::getId);
        List<BizCourse> courses = courseService.list(courseQw);
        
        if (courses.size() < 3) {
            log.warn("CourseDataSeeder: Not enough courses found, skipping student course seeding");
            return;
        }

        Long course1Id = courses.get(0).getId();
        Long course2Id = courses.get(1).getId();
        Long course3Id = courses.get(2).getId();

        List<BizStudentCourse> studentCourses = new ArrayList<>();
        long idCounter = 1;

        for (int i = 0; i < students.size(); i++) {
            Long studentId = students.get(i).getId();
            if (i == 0) {
                studentCourses.add(createStudentCourse(idCounter++, studentId, course1Id));
            } else if (i == 1) {
                studentCourses.add(createStudentCourse(idCounter++, studentId, course2Id));
            } else if (i == 2) {
                studentCourses.add(createStudentCourse(idCounter++, studentId, course3Id));
            }
        }

        studentCourseService.saveBatch(studentCourses);
        log.info("CourseDataSeeder: Seeded {} demo student courses for {} students", studentCourses.size(), students.size());
    }

    private BizCourse createCourse(Long id, Long teacherId, String courseName, String description,
                                   Integer status, Integer needEnrollCode, String enrollCode) {
        BizCourse course = new BizCourse();
        course.setId(id);
        course.setTeacherId(teacherId);
        course.setCourseName(courseName);
        course.setDescription(description);
        course.setStatus(status);
        course.setNeedEnrollCode(needEnrollCode);
        course.setEnrollCode(enrollCode);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        return course;
    }

    private BizStudentCourse createStudentCourse(Long id, Long studentId, Long courseId) {
        BizStudentCourse sc = new BizStudentCourse();
        sc.setId(id);
        sc.setStudentId(studentId);
        sc.setCourseId(courseId);
        sc.setCreatedAt(LocalDateTime.now());
        return sc;
    }
}
