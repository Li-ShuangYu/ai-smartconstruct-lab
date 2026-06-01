package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.CourseRequest;
import com.smartconstruct.backend_core.dto.CourseVO;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.BizCourse;
import com.smartconstruct.backend_core.entity.BizStudent;
import com.smartconstruct.backend_core.entity.BizStudentCourse;
import com.smartconstruct.backend_core.entity.SysUser;
import com.smartconstruct.backend_core.service.ICourseService;
import com.smartconstruct.backend_core.service.IStudentCourseService;
import com.smartconstruct.backend_core.service.IStudentService;
import com.smartconstruct.backend_core.service.ITeacherService;
import com.smartconstruct.backend_core.service.SysUserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.smartconstruct.backend_core.util.Java8Compat;

@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseController {

    private final ICourseService courseService;
    private final SysUserService sysUserService;
    private final IStudentCourseService studentCourseService;
    private final IStudentService studentService;

    public AdminCourseController(ICourseService courseService, SysUserService sysUserService,
                                 IStudentCourseService studentCourseService, IStudentService studentService) {
        this.courseService = courseService;
        this.sysUserService = sysUserService;
        this.studentCourseService = studentCourseService;
        this.studentService = studentService;
    }

    private String generateEnrollCode() {
        Random r = new Random();
        String code;
        do {
            code = String.format("%04d", r.nextInt(10000));
        } while (courseService.count(new LambdaQueryWrapper<BizCourse>().eq(BizCourse::getEnrollCode, code)) > 0);
        return code;
    }

    @GetMapping
    public ApiResult<PageResult<CourseVO>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<BizCourse> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !Java8Compat.isBlank(keyword)) {
            qw.like(BizCourse::getCourseName, keyword);
        }
        qw.orderByDesc(BizCourse::getCreatedAt);
        Page<BizCourse> p = courseService.page(new Page<>(page, pageSize), qw);

        Map<Long, SysUser> userMap = Collections.emptyMap();
        Set<Long> teacherIds = p.getRecords().stream()
                .filter(c -> c.getTeacherId() != null)
                .map(BizCourse::getTeacherId)
                .collect(Collectors.toSet());
        if (!teacherIds.isEmpty()) {
            userMap = sysUserService.list(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, teacherIds))
                    .stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        }

        List<CourseVO> voList = new ArrayList<>();
        for (BizCourse c : p.getRecords()) {
            SysUser u = userMap.get(c.getTeacherId());
            CourseVO vo = new CourseVO();
            vo.setId(c.getId());
            vo.setCourseName(c.getCourseName());
            vo.setDescription(c.getDescription());
            vo.setStatus(c.getStatus());
            vo.setNeedEnrollCode(c.getNeedEnrollCode());
            vo.setEnrollCode(c.getEnrollCode());
            vo.setTeacherId(c.getTeacherId());
            vo.setTeacherName(u != null ? u.getUsername() : "");
            vo.setCreatedAt(c.getCreatedAt());
            vo.setUpdatedAt(c.getUpdatedAt());
            voList.add(vo);
        }
        return ApiResult.ok(new PageResult<>(voList, p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @OperationLog(action = "新增课程")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> create(@Valid @RequestBody CourseRequest req) {
        long dup = courseService.count(new LambdaQueryWrapper<BizCourse>().eq(BizCourse::getCourseName, req.getCourseName()));
        if (dup > 0) return ApiResult.error("课程名称已存在");

        BizCourse course = new BizCourse();
        course.setCourseName(req.getCourseName());
        course.setDescription(req.getDescription());
        course.setStatus(req.getStatus() != null ? req.getStatus() : 0);
        course.setNeedEnrollCode(req.getNeedEnrollCode() != null ? req.getNeedEnrollCode() : 0);
        if (req.getNeedEnrollCode() != null && req.getNeedEnrollCode() == 1) {
            course.setEnrollCode(generateEnrollCode());
        }
        course.setTeacherId(req.getTeacherId() != null ? req.getTeacherIdAsLong() : null);
        LocalDateTime now = LocalDateTime.now();
        course.setCreatedAt(now);
        course.setUpdatedAt(now);
        courseService.save(course);
        return ApiResult.ok();
    }

    @OperationLog(action = "编辑课程")
    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> update(@PathVariable String id, @Valid @RequestBody CourseRequest req) {
        Long courseId = Long.parseLong(id);
        BizCourse course = courseService.getById(courseId);
        if (course == null) return ApiResult.error("课程不存在");

        long dup = courseService.count(new LambdaQueryWrapper<BizCourse>()
                .eq(BizCourse::getCourseName, req.getCourseName())
                .ne(BizCourse::getId, courseId));
        if (dup > 0) return ApiResult.error("课程名称已存在");

        course.setCourseName(req.getCourseName());
        if (req.getDescription() != null) course.setDescription(req.getDescription());
        course.setStatus(req.getStatus());

        int oldNeed = course.getNeedEnrollCode() != null ? course.getNeedEnrollCode() : 0;
        int newNeed = req.getNeedEnrollCode() != null ? req.getNeedEnrollCode() : 0;
        course.setNeedEnrollCode(newNeed);
        if (oldNeed == 0 && newNeed == 1) {
            course.setEnrollCode(generateEnrollCode());
        } else if (oldNeed == 1 && newNeed == 0) {
            course.setEnrollCode(null);
        }
        course.setTeacherId(req.getTeacherId() != null ? req.getTeacherIdAsLong() : null);
        course.setUpdatedAt(LocalDateTime.now());
        courseService.updateById(course);
        return ApiResult.ok();
    }

    @OperationLog(action = "删除课程")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable String id) {
        courseService.removeById(Long.parseLong(id));
        return ApiResult.ok();
    }

    @OperationLog(action = "切换课程状态")
    @PutMapping("/{id}/status")
    public ApiResult<Void> toggleStatus(@PathVariable String id, @RequestParam Integer status) {
        BizCourse course = courseService.getById(Long.parseLong(id));
        if (course == null) return ApiResult.error("课程不存在");
        course.setStatus(status);
        course.setUpdatedAt(LocalDateTime.now());
        courseService.updateById(course);
        return ApiResult.ok();
    }

    @GetMapping("/{courseId}/students")
    public ApiResult<List<Map<String, Object>>> getCourseStudents(@PathVariable String courseId) {
        Long cId = Long.parseLong(courseId);
        List<BizStudentCourse> scList = studentCourseService.list(
                new LambdaQueryWrapper<BizStudentCourse>().eq(BizStudentCourse::getCourseId, cId));
        if (scList.isEmpty()) return ApiResult.ok(Java8Compat.emptyList());

        Set<Long> studentIds = scList.stream().map(BizStudentCourse::getStudentId).collect(Collectors.toSet());
        List<BizStudent> students = studentService.list(new LambdaQueryWrapper<BizStudent>().in(BizStudent::getUserId, studentIds));

        List<Map<String, Object>> result = new ArrayList<>();
        for (BizStudent s : students) {
            SysUser u = sysUserService.getById(s.getUserId());
            if (u == null) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", String.valueOf(s.getUserId()));
            m.put("realName", s.getRealName());
            m.put("username", u.getUsername());
            m.put("createdAt", u.getCreatedAt());
            result.add(m);
        }
        return ApiResult.ok(result);
    }
}