package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应封装
 *
 * 用于列表查询接口的分页返回
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    /** 分页数据列表 */
    private List<T> records;
    /** 总记录数 */
    private long total;
    /** 当前页码（从1开始） */
    private long page;
    /** 每页条数 */
    private long pageSize;
}
