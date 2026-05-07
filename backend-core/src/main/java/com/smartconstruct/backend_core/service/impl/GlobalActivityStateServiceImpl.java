package com.smartconstruct.backend_core.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.WfGlobalActivityState;
import com.smartconstruct.backend_core.mapper.WfGlobalActivityStateMapper;
import com.smartconstruct.backend_core.service.IGlobalActivityStateService;
import org.springframework.stereotype.Service;
@Service
public class GlobalActivityStateServiceImpl extends ServiceImpl<WfGlobalActivityStateMapper, WfGlobalActivityState> implements IGlobalActivityStateService {}
