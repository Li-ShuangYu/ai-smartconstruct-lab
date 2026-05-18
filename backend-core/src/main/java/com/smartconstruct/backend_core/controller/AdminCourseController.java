package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.CourseRequest;
import com.smartconstruct.backend_core.dto.CourseVO;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.BizCourse;
import com.smartconstruct.backend_core.entity.SysUser;
import com.smartconstruct.backend_core.service.ICourseService;
import com.smartconstruct.backend_core.service.ITeacherService;
import com.smartconstruct.backend_core.service.SysUserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseController {

    private final ICourseService courseService;
    private final ITeacherService teacherService;
    private final SysUserService sysUserService;

    public AdminCourseController(ICourseService courseService, ITeacherService teacherService, SysUserService sysUserService) {
        this.courseService = courseService;
        this.teacherService = teacherService;
        this.sysUserService = sysUserService;
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
        if (keyword != null && !keyword.isBlank()) {
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
}