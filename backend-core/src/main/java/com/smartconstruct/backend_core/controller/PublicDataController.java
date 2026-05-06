package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
@RequestMapping("/api/public")
public class PublicDataController {

    private final IDepartmentService departmentService;
    private final IMajorService majorService;
    private final IAdminClassService adminClassService;

    public PublicDataController(IDepartmentService departmentService, IMajorService majorService, IAdminClassService adminClassService) {
        this.departmentService = departmentService;
        this.majorService = majorService;
        this.adminClassService = adminClassService;
    }

    @GetMapping("/departments")
    public ApiResult<List<BizDepartment>> getDepartments() {
        return ApiResult.ok(departmentService.list());
    }

    @GetMapping("/majors")
    public ApiResult<List<BizMajor>> getMajors(@RequestParam Long deptId) {
        List<BizMajor> list = majorService.list(new LambdaQueryWrapper<BizMajor>().eq(BizMajor::getDeptId, deptId));
        return ApiResult.ok(list);
    }

    @GetMapping("/classes")
    public ApiResult<List<BizAdminClass>> getClasses(@RequestParam Long majorId) {
        List<BizAdminClass> list = adminClassService.list(new LambdaQueryWrapper<BizAdminClass>().eq(BizAdminClass::getMajorId, majorId));
        return ApiResult.ok(list);
    }
}
