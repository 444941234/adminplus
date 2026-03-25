package com.adminplus.service;

import com.adminplus.pojo.dto.req.LogQueryReq;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

/**
 * 日志导出服务接口
 *
 * @author AdminPlus
 * @since 2026-03-04
 */
public interface LogExportService {

    /**
     * 导出日志为 Excel
     *
     * @param query 查询条件
     * @return Excel 文件响应
     * @throws IOException IO异常
     */
    ResponseEntity<byte[]> exportToExcel(LogQueryReq query) throws IOException;

    /**
     * 导出日志为 CSV
     *
     * @param query 查询条件
     * @return CSV 文件响应
     * @throws IOException IO异常
     */
    ResponseEntity<byte[]> exportToCsv(LogQueryReq query) throws IOException;
}
