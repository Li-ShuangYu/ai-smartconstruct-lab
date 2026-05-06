package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.SysTicket;
import com.smartconstruct.backend_core.mapper.SysTicketMapper;
import com.smartconstruct.backend_core.service.ITicketService;
import org.springframework.stereotype.Service;

@Service
public class TicketServiceImpl extends ServiceImpl<SysTicketMapper, SysTicket> implements ITicketService {
}
