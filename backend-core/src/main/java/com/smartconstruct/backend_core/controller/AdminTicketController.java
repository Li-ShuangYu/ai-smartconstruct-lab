package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.SysTicket;
import com.smartconstruct.backend_core.service.ITicketService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/tickets")
public class AdminTicketController {

    private final ITicketService ticketService;

    public AdminTicketController(ITicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ApiResult<PageResult<SysTicket>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysTicket> qw = new LambdaQueryWrapper<>();
        if (status != null) qw.eq(SysTicket::getStatus, status);
        qw.orderByDesc(SysTicket::getCreatedAt);
        Page<SysTicket> p = ticketService.page(new Page<>(page, pageSize), qw);
        return ApiResult.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResult<SysTicket> detail(@PathVariable Long id) {
        return ApiResult.ok(ticketService.getById(id));
    }

    @OperationLog(action = "变更工单状态")
    @PutMapping("/{id}/status")
    public ApiResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysTicket ticket = new SysTicket();
        ticket.setId(id);
        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketService.updateById(ticket);
        return ApiResult.ok();
    }

    @OperationLog(action = "删除工单")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        ticketService.removeById(id);
        return ApiResult.ok();
    }
}
