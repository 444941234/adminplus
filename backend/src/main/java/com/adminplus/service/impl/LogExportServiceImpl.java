package com.adminplus.service.impl;

import com.adminplus.pojo.dto.req.LogQueryReq;
import com.adminplus.pojo.dto.resp.LogPageResp;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.service.LogExportService;
import com.adminplus.service.LogService;
import com.adminplus.utils.DictUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 日志导出服务实现
 *
 * @author AdminPlus
 * @since 2026-03-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogExportServiceImpl implements LogExportService {

    private final LogService logService;
    private final DictUtils dictUtils;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Override
    public ResponseEntity<byte[]> exportToExcel(LogQueryReq query) throws IOException {
        // 限制导出数量
        query.setSize(10000);
        query.setPage(1);

        PageResultResp<LogPageResp> result = logService.findPage(query);
        List<LogPageResp> logs = result.records();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("日志数据");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] columnNames = {"日志ID", "用户名", "模块", "日志类型", "操作类型", "描述",
                    "请求方法", "IP地址", "执行时长(ms)", "状态", "操作时间"};
            for (int i = 0; i < columnNames.length; i++) {
                headerRow.createCell(i).setCellValue(columnNames[i]);
            }

            // 填充数据
            for (int i = 0; i < logs.size(); i++) {
                LogPageResp log = logs.get(i);
                Row row = sheet.createRow(i + 1);
                int col = 0;
                row.createCell(col++).setCellValue(log.id());
                row.createCell(col++).setCellValue(log.username());
                row.createCell(col++).setCellValue(log.module());
                row.createCell(col++).setCellValue(getLogTypeDesc(log.logType()));
                row.createCell(col++).setCellValue(getOperationTypeDesc(log.operationType()));
                row.createCell(col++).setCellValue(log.description());
                row.createCell(col++).setCellValue(log.method() != null ? log.method() : "");
                row.createCell(col++).setCellValue(log.ip() != null ? log.ip() : "");
                row.createCell(col++).setCellValue(log.costTime() != null ? log.costTime() : 0);
                row.createCell(col++).setCellValue(log.status() != null && log.status() == 1 ? "成功" : "失败");
                row.createCell(col++).setCellValue(formatDate(log.createTime()));
            }

            // 自动调整列宽
            for (int i = 0; i < columnNames.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            String filename = "logs_" + System.currentTimeMillis() + ".xlsx";
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            httpHeaders.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(httpHeaders)
                    .body(outputStream.toByteArray());
        }
    }

    @Override
    public ResponseEntity<byte[]> exportToCsv(LogQueryReq query) throws IOException {
        // 限制导出数量
        query.setSize(10000);
        query.setPage(1);

        PageResultResp<LogPageResp> result = logService.findPage(query);
        List<LogPageResp> logs = result.records();

        StringBuilder csv = new StringBuilder();
        // CSV 头部
        csv.append("日志ID,用户名,模块,日志类型,操作类型,描述,请求方法,IP地址,执行时长(ms),状态,操作时间\n");

        // CSV 数据
        for (LogPageResp log : logs) {
            csv.append(escapeCsv(log.id())).append(",");
            csv.append(escapeCsv(log.username())).append(",");
            csv.append(escapeCsv(log.module())).append(",");
            csv.append(escapeCsv(getLogTypeDesc(log.logType()))).append(",");
            csv.append(escapeCsv(getOperationTypeDesc(log.operationType()))).append(",");
            csv.append(escapeCsv(log.description())).append(",");
            csv.append(escapeCsv(log.method() != null ? log.method() : "")).append(",");
            csv.append(escapeCsv(log.ip() != null ? log.ip() : "")).append(",");
            csv.append(log.costTime() != null ? log.costTime() : 0).append(",");
            csv.append(log.status() != null && log.status() == 1 ? "成功" : "失败").append(",");
            csv.append(escapeCsv(formatDate(log.createTime()))).append("\n");
        }

        String filename = "logs_" + System.currentTimeMillis() + ".csv";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        httpHeaders.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(csv.toString().getBytes("UTF-8"));
    }

    private String getLogTypeDesc(Integer type) {
        if (type == null) return "未知";
        return dictUtils.getDictLabel("log_type", String.valueOf(type));
    }

    private String getOperationTypeDesc(Integer type) {
        if (type == null) return "未知";
        return dictUtils.getDictLabel("operation_type", String.valueOf(type));
    }

    private String formatDate(Instant instant) {
        if (instant == null) return "";
        return DATE_FORMATTER.format(instant);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
