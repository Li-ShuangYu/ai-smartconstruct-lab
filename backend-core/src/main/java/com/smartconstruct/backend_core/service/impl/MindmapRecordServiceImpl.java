package com.smartconstruct.backend_core.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizMindmapRecord;
import com.smartconstruct.backend_core.mapper.BizMindmapRecordMapper;
import com.smartconstruct.backend_core.service.IMindmapRecordService;
import org.springframework.stereotype.Service;
@Service
public class MindmapRecordServiceImpl extends ServiceImpl<BizMindmapRecordMapper, BizMindmapRecord> implements IMindmapRecordService {}
