package org.fhi360.lamis.modules.reporting.service.converter;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LabDataConverter {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ByteArrayOutputStream convertExcel(List<Long> facilityIds, Long labTest) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DateFormat dateFormatExcel = new SimpleDateFormat("dd-MMM-yyyy");
        Workbook workbook = new SXSSFWorkbook(100);  // turn off auto-flushing and accumulate all rows in memory
        Sheet sheet = workbook.createSheet();
        CellStyle numericStyle = workbook.createCellStyle();
        numericStyle.setDataFormat((short) BuiltinFormats.getBuiltinFormat("#,##0"));

        try {
            int[] rownum = {0};
            int[] cellnum = {0};
            Row[] row = {sheet.createRow(rownum[0]++)};
            Cell[] cell = {row[0].createCell(cellnum[0]++)};
            cell[0].setCellValue("Facility Id");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Facility");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Patient Id");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Hospital Num");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Test");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Sample Collect");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Date Report");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Result");
            if (Objects.equals(labTest, 16L)) {
                sheet.setDefaultColumnStyle(7, numericStyle);
            }
            String query = "" +
                    "SELECT DISTINCT lb.facility_id, f.name facility, patient_id, p.uuid puuid, date_result_received, " +
                    "   jsonb_extract_path_text(l,'result') result, description, date_sample_collected, hospital_num FROM laboratory lb " +
                    "   JOIN patient p ON patient_id = p.id JOIN facility f ON f.id = lb.facility_id , jsonb_array_elements(lines) with ordinality a(l)  " +
                    "   JOIN lab_test t on t.id = cast(jsonb_extract_path_text(l,'lab_test_id') as integer ) WHERE p.archived = false AND lb.archived = false AND lb.facility_id " +
                    "   IN (:facilities) AND t.id = :labTest and cast(p.extra->>'art' as boolean) = true " +
                    "   ORDER BY lb.facility_id, patient_id, date_result_received";
            SqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("facilities", facilityIds)
                    .addValue("labTest", labTest);
            try {
                namedParameterJdbcTemplate.query(query, namedParameters, rs -> {
                    while (rs.next()) {
                        cellnum[0] = 0;
                        row[0] = sheet.createRow(rownum[0]++);
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getLong("facility_id"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("facility"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("puuid"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("hospital_num"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("description"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("date_sample_collected") == null) ? "" :
                                dateFormatExcel.format(rs.getDate("date_sample_collected")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("date_result_received") == null) ? "" :
                                dateFormatExcel.format(rs.getDate("date_result_received")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("result"));
                    }
                    return null;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        try {
            workbook.write(baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos;
    }
}
