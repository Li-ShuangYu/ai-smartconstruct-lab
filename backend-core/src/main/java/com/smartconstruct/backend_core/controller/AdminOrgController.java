package com.smartconstruct.backend_core.controller;

import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.entity.BizAdminClass;
import com.smartconstruct.backend_core.entity.BizDepartment;
import com.smartconstruct.backend_core.entity.BizMajor;
import com.smartconstruct.backend_core.service.IAdminClassService;
import com.smartconstruct.backend_core.service.IDepartmentService;
import com.smartconstruct.backend_core.service.IMajorService;
import org.springframework.web.bind.annotation.*;

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

    // ===== 院系 CRUD =====
    @GetMapping("/depts")
    public ApiResult<List<BizDepartment>> listDepts() {
        return ApiResult.ok(departmentService.list());
    }

    @PostMapping("/depts")
    public ApiResult<Void> addDept(@RequestBody BizDepartment dept) {
        departmentService.save(dept);
        return ApiResult.ok();
    }

    @PutMapping("/depts/{id}")
    public ApiResult<Void> updateDept(@PathVariable Long id, @RequestBody BizDepartment dept) {
        dept.setId(id);
        departmentService.updateById(dept);
        return ApiResult.ok();
    }

    @DeleteMapping("/depts/{id}")
    public ApiResult<Void> deleteDept(@PathVariable Long id) {
        departmentService.removeById(id);
        return ApiResult.ok();
    }

    // ===== 专业 CRUD =====
    @GetMapping("/majors")
    public ApiResult<List<BizMajor>> listMajors() {
        return ApiResult.ok(majorService.list());
    }

    @PostMapping("/majors")
    public ApiResult<Void> addMajor(@RequestBody BizMajor major) {
        majorService.save(major);
        return ApiResult.ok();
    }

    @PutMapping("/majors/{id}")
    public ApiResult<Void> updateMajor(@PathVariable Long id, @RequestBody BizMajor major) {
        major.setId(id);
        majorService.updateById(major);
        return ApiResult.ok();
    }

    @DeleteMapping("/majors/{id}")
    public ApiResult<Void> deleteMajor(@PathVariable Long id) {
        majorService.removeById(id);
        return ApiResult.ok();
    }

    // ===== 班级 CRUD =====
    @GetMapping("/classes")
    public ApiResult<List<BizAdminClass>> listClasses() {
        return ApiResult.ok(adminClassService.list());
    }

    @PostMapping("/classes")
    public ApiResult<Void> addClass(@RequestBody BizAdminClass clz) {
        adminClassService.save(clz);
        return ApiResult.ok();
    }

    @PutMapping("/classes/{id}")
    public ApiResult<Void> updateClass(@PathVariable Long id, @RequestBody BizAdminClass clz) {
        clz.setId(id);
        adminClassService.updateById(clz);
        return ApiResult.ok();
    }

    @DeleteMapping("/classes/{id}")
    public ApiResult<Void> deleteClass(@PathVariable Long id) {
        adminClassService.removeById(id);
        return ApiResult.ok();
    }
}
