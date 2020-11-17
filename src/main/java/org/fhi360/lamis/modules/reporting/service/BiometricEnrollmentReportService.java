package org.fhi360.lamis.modules.reporting.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.fhi360.lamis.modules.reporting.service.converter.RunningPortService;
import org.lamisplus.modules.base.service.PrinceXMLService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.fhi360.lamis.modules.reporting.service.AppointmentReportsService.*;

@Service
@RequiredArgsConstructor
public class BiometricEnrollmentReportService {
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");
    private final JdbcTemplate jdbcTemplate;
    private final ITemplateEngine templateEngine;
    private final PrinceXMLService princeXMLService;
    private final RunningPortService runningPortService;

    public ByteArrayOutputStream fingerprintEnrollmentReport(Long facilityId, LocalDate start, LocalDate end, boolean pdf) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            String title = "Biometric Enrollment Report";
            String query = "select distinct p.id, uuid, other_names, surname, hospital_num, enrollment_date, (case gender when " +
                    "'MALE' then 'Male' else 'Female' end) gender, datediff('year', date_birth, current_date) age, " +
                    "p.address, p.phone from biometric b inner join patient p on b.patient_id = p.uuid where " +
                    "enrollment_date between ? and ? and p.facility_id = ? and p.archived = false and b.archived = false " +
                    "order by enrollment_date desc";
            List<Map<String, Object>> dataSource = jdbcTemplate.query(query, rs -> {
                List<Map<String, Object>> result = new ArrayList<>();
                int count = 1;
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("sn", FORMATTER.format(count++));
                    String name = rs.getString("other_names") + " " + rs.getString("surname");
                    Long fingers = jdbcTemplate.queryForObject("select count(*) from biometric where patient_id = ?",
                            Long.class, rs.getString("uuid"));
                    map.put("fingers", fingers);
                    map.put("name", name);
                    map.put("hospital_num", rs.getString("hospital_num"));
                    map.put("gender", rs.getString("gender"));
                    map.put("age", rs.getInt("age"));
                    map.put("address", rs.getString("address"));
                    map.put("phone", rs.getString("phone"));
                    map.put("enrollment_date", rs.getDate("enrollment_date"));
                    map.put("id", rs.getLong("id"));
                    result.add(map);
                }
                return result;
            }, start, end, facilityId);

            dataSource = getPatientCurrentStatus(dataSource, "id");

            Map<String, Object> parameters = new HashMap<>();

            parameters.put("title", title);
            parameters.put("from", convertToDateViaInstant(start));
            parameters.put("to", convertToDateViaInstant(end));

            if (pdf) {
                Context context = new Context();
                context.setVariables(facilityInfo(facilityId));
                context.setVariables(parameters);
                context.setVariable("today", new Date());
                context.setVariable("datasource", dataSource);
                context.setVariable("css", "http://localhost:" + runningPortService.getPort() + "/across/resources/static/reporting/css/style.css");
                String output = templateEngine.process("templates/biometric_report", context);
                princeXMLService.convert(IOUtils.toInputStream(output), baos);
            } else {
                baos = buildExcel(dataSource, start, end, facilityId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos;
    }

    public ByteArrayOutputStream buildExcel(List<Map<String, Object>> dataSource, LocalDate start, LocalDate end, Long facilityId) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");
        Workbook workbook = new SXSSFWorkbook(100);  // turn off auto-flushing and accumulate all rows in memory
        Sheet sheet = workbook.createSheet();

        int[] rowNum = {0};
        int[] cellNum = {0};
        Row[] row = {sheet.createRow(rowNum[0]++)};
        Cell[] cell = {row[0].createCell(cellNum[0]++)};
        cell[0].setCellValue(facilityInfo(facilityId).get("facility").toString());
        sheet.addMergedRegion(CellRangeAddress.valueOf("A1:J1"));
        CellUtil.setAlignment(cell[0], HorizontalAlignment.CENTER);
        row[0] = sheet.createRow(rowNum[0]++);
        cellNum[0] = 0;
        cell[0] = row[0].createCell(cellNum[0]++);
        cell[0].setCellValue("Start:");
        cell[0] = row[0].createCell(cellNum[0]++);
        cell[0].setCellValue(start.format(DateTimeFormatter.ofPattern("dd MMM, yyyy")));
        row[0] = sheet.createRow(rowNum[0]++);
        cellNum[0] = 0;
        cell[0] = row[0].createCell(cellNum[0]++);
        cell[0].setCellValue("End:");
        cell[0] = row[0].createCell(cellNum[0]++);
        cell[0].setCellValue(end.format(DateTimeFormatter.ofPattern("dd MMM, yyyy")));

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        style.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
        style.setFillPattern(FillPatternType.FINE_DOTS);
        style.setFont(font);

        row[0] = sheet.createRow(rowNum[0]++);
        cellNum[0] = 0;
        cell[0] = row[0].createCell(cellNum[0]++);
        cell[0].setCellValue("SN");
        cell[0].setCellStyle(style);
        cell[0] = row[0].createCell(cellNum[0]++);
        cell[0].setCellValue("Hospital Num");
        cell[0].setCellStyle(style);
        cell[0] = row[0].createCell(cellNum[0]++);
        cell[0].setCellValue("Name");
        cell[0].setCellStyle(style);
        cell[0] = row[0].createCell(cellNum[0]++);
        cell[0].setCellValue("Current Status");
        cell[0].setCellStyle(style);
        cell[0] = row[0].createCell(cellNum[0]++);
        cell[0].setCellValue("Age");
        cell[0].setCellStyle(style);
        cell[0] = row[0].createCell(cellNum[0]++);
        cell[0].setCellValue("Gender");
        cell[0].setCellStyle(style);
        cell[0] = row[0].createCell(cellNum[0]++);
        cell[0].setCellValue("Address");
        cell[0].setCellStyle(style);
        cell[0] = row[0].createCell(cellNum[0]++);
        cell[0].setCellValue("Phone");
        cell[0].setCellStyle(style);
        cell[0] = row[0].createCell(cellNum[0]++);
        cell[0].setCellValue("Enrollment Date");
        cell[0].setCellStyle(style);
        cell[0] = row[0].createCell(cellNum[0]++);
        cell[0].setCellValue("Fingers");
        cell[0].setCellStyle(style);

        AtomicLong count = new AtomicLong(1);
        dataSource.forEach(entry -> {
            cellNum[0] = 0;
            row[0] = sheet.createRow(rowNum[0]++);
            cell[0] = row[0].createCell(cellNum[0]++);
            cell[0].setCellValue(count.getAndIncrement());
            cell[0] = row[0].createCell(cellNum[0]++);
            cell[0].setCellValue(entry.get("hospital_num").toString());
            cell[0] = row[0].createCell(cellNum[0]++);
            cell[0].setCellValue(entry.get("name").toString());
            cell[0] = row[0].createCell(cellNum[0]++);
            cell[0].setCellValue(entry.get("status").toString());
            cell[0] = row[0].createCell(cellNum[0]++);
            cell[0].setCellValue(entry.get("age").toString());
            cell[0] = row[0].createCell(cellNum[0]++);
            cell[0].setCellValue(entry.get("gender").toString());
            cell[0] = row[0].createCell(cellNum[0]++);
            cell[0].setCellValue(entry.get("address").toString());
            cell[0] = row[0].createCell(cellNum[0]++);
            cell[0].setCellValue(entry.get("phone").toString());
            cell[0] = row[0].createCell(cellNum[0]++);
            cell[0].setCellValue(dateFormat.format((Date) entry.get("enrollment_date")));
            cell[0] = row[0].createCell(cellNum[0]++);
            cell[0].setCellValue(entry.get("fingers").toString());
        });
        try {
            workbook.write(baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos;
    }

}
