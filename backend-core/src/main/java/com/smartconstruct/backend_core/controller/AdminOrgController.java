package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.ClassRequest;
import com.smartconstruct.backend_core.dto.DeptRequest;
import com.smartconstruct.backend_core.dto.MajorRequest;
import com.smartconstruct.backend_core.entity.BizAdminClass;
import com.smartconstruct.backend_core.entity.BizDepartment;
import com.smartconstruct.backend_core.entity.BizMajor;
import com.smartconstruct.backend_core.service.IAdminClassService;
import com.smartconstruct.backend_core.service.IDepartmentService;
import com.smartconstruct.backend_core.service.IMajorService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/org")
public class AdminOrgController {

    private final IDepartmentService departmentService;
    private final IMajorService majorService;
    private final IAdminClassService adminClassService;

    public AdminOrgController(IDepartmentService departmentService, IMajorService majorService, IAdminClassService adminClassService) {
        this.departmentService = departmentService;
        this.majorService = majorService;
        this.adminClassService = adminClassService;
    }

    @GetMapping("/depts")
    public ApiResult<List<BizDepartment>> listDepts() {
        return ApiResult.ok(departmentService.list());
    }

    @OperationLog(action = "新增院系")
    @PostMapping("/depts")
    public ApiResult<Void> addDept(@Valid @RequestBody DeptRequest req) {
        if (departmentService.count(new LambdaQueryWrapper<BizDepartment>().eq(BizDepartment::getDeptName, req.getDeptName())) > 0) {
            return ApiResult.error("院系名称已存在");
        }
        BizDepartment dept = new BizDepartment();
        dept.setDeptName(req.getDeptName());
        dept.setCreatedAt(LocalDateTime.now());
        departmentService.save(dept);
        return ApiResult.ok();
    }

    @OperationLog(action = "编辑院系")
    @PutMapping("/depts/{id}")
    public ApiResult<Void> updateDept(@PathVariable String id, @Valid @RequestBody DeptRequest req) {
        Long deptId = Long.parseLong(id);
        BizDepartment exists = departmentService.getById(deptId);
        if (exists == null) return ApiResult.error("院系不存在");
        if (departmentService.count(new LambdaQueryWrapper<BizDepartment>()
                .eq(BizDepartment::getDeptName, req.getDeptName()).ne(BizDepartment::getId, deptId)) > 0) {
            return ApiResult.error("院系名称已存在");
        }
        exists.setDeptName(req.getDeptName());
        departmentService.updateById(exists);
        return ApiResult.ok();
    }

    @OperationLog(action = "删除院系")
    @DeleteMapping("/depts/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> deleteDept(@PathVariable String id) {
        Long deptId = Long.parseLong(id);
        long majorCount = majorService.count(new LambdaQueryWrapper<BizMajor>().eq(BizMajor::getDeptId, deptId));
        if (majorCount > 0) {
            return ApiResult.error("该院系下存在 " + majorCount + " 个专业，请先删除相关专业后再删除院系");
        }
        departmentService.removeById(deptId);
        return ApiResult.ok();
    }

    @GetMapping("/majors")
    public ApiResult<List<BizMajor>> listMajors(@RequestParam(required = false) Long deptId) {
        LambdaQueryWrapper<BizMajor> qw = new LambdaQueryWrapper<>();
        if (deptId != null) {
            qw.eq(BizMajor::getDeptId, deptId);
        }
        qw.orderByAsc(BizMajor::getId);
        return ApiResult.ok(majorService.list(qw));
    }

    @OperationLog(action = "新增专业")
    @PostMapping("/majors")
    public ApiResult<Void> addMajor(@Valid @RequestBody MajorRequest req) {
        if (majorService.count(new LambdaQueryWrapper<BizMajor>()
                .eq(BizMajor::getDeptId, req.getDeptIdAsLong())
                .eq(BizMajor::getMajorName, req.getMajorName())) > 0) {
            return ApiResult.error("该院系下已存在同名专业");
        }
        BizMajor major = new BizMajor();
        major.setDeptId(req.getDeptIdAsLong());
        major.setMajorName(req.getMajorName());
        major.setCreatedAt(LocalDateTime.now());
        majorService.save(major);
        return ApiResult.ok();
    }

    @OperationLog(action = "编辑专业")
    @PutMapping("/majors/{id}")
    public ApiResult<Void> updateMajor(@PathVariable String id, @Valid @RequestBody MajorRequest req) {
        Long majorId = Long.parseLong(id);
        BizMajor exists = majorService.getById(majorId);
        if (exists == null) return ApiResult.error("专业不存在");
        if (majorService.count(new LambdaQueryWrapper<BizMajor>()
                .eq(BizMajor::getDeptId, req.getDeptIdAsLong())
                .eq(BizMajor::getMajorName, req.getMajorName())
                .ne(BizMajor::getId, majorId)) > 0) {
            return ApiResult.error("该院系同名专业已存在");
        }
        exists.setDeptId(req.getDeptIdAsLong());
        exists.setMajorName(req.getMajorName());
        majorService.updateById(exists);
        return ApiResult.ok();
    }

    @OperationLog(action = "删除专业")
    @DeleteMapping("/majors/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> deleteMajor(@PathVariable String id) {
        Long majorId = Long.parseLong(id);
        if (adminClassService.count(new LambdaQueryWrapper<BizAdminClass>().eq(BizAdminClass::getMajorId, majorId)) > 0) {
            return ApiResult.error("该专业下存在班级，请先删除相关班级后再删除专业");
        }
        majorService.removeById(majorId);
        return ApiResult.ok();
    }

    @GetMapping("/classes")
    public ApiResult<List<BizAdminClass>> listClasses(@RequestParam(required = false) Long majorId) {
        LambdaQueryWrapper<BizAdminClass> qw = new LambdaQueryWrapper<>();
        if (majorId != null) {
            qw.eq(BizAdminClass::getMajorId, majorId);
        }
        qw.orderByAsc(BizAdminClass::getId);
        return ApiResult.ok(adminClassService.list(qw));
    }

    @OperationLog(action = "新增班级")
    @PostMapping("/classes")
    public ApiResult<Void> addClass(@Valid @RequestBody ClassRequest req) {
        if (adminClassService.count(new LambdaQueryWrapper<BizAdminClass>()
                .eq(BizAdminClass::getClassName, req.getClassName())
                .eq(BizAdminClass::getMajorId, req.getMajorIdAsLong())) > 0) {
            return ApiResult.error("该专业同名班级已存在");
        }
        BizAdminClass clz = new BizAdminClass();
        clz.setMajorId(req.getMajorIdAsLong());
        clz.setClassName(req.getClassName());
        clz.setEnrollmentYear(0);
        clz.setCreatedAt(LocalDateTime.now());
        adminClassService.save(clz);
        return ApiResult.ok();
    }

    @OperationLog(action = "编辑班级")
    @PutMapping("/classes/{id}")
    public ApiResult<Void> updateClass(@PathVariable String id, @Valid @RequestBody ClassRequest req) {
        Long classId = Long.parseLong(id);
        BizAdminClass exists = adminClassService.getById(classId);
        if (exists == null) return ApiResult.error("班级不存在");
        if (adminClassService.count(new LambdaQueryWrapper<BizAdminClass>()
                .eq(BizAdminClass::getClassName, req.getClassName())
                .eq(BizAdminClass::getMajorId, req.getMajorIdAsLong())
                .ne(BizAdminClass::getId, classId)) > 0) {
            return ApiResult.error("该专业同名班级已存在");
        }
        exists.setMajorId(req.getMajorIdAsLong());
        exists.setClassName(req.getClassName());
        adminClassService.updateById(exists);
        return ApiResult.ok();
    }

    @OperationLog(action = "删除班级")
    @DeleteMapping("/classes/{id}")
    public ApiResult<Void> deleteClass(@PathVariable String id) {
        adminClassService.removeById(Long.parseLong(id));
        return ApiResult.ok();
    }
}