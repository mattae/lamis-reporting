package org.fhi360.lamis.modules.reporting.service.converter;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
public class ClinicDataConverter {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ByteArrayOutputStream convertExcel(List<Long> facilityIds) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DateFormat dateFormatExcel = new SimpleDateFormat("dd-MMM-yyyy");
        Workbook workbook = new SXSSFWorkbook(100);  // turn off auto-flushing and accumulate all rows in memory
        Sheet sheet = workbook.createSheet();

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
            cell[0].setCellValue("Date Visit");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Clinic Stage");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Function Status");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("TB Status");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Body Weight (kg)");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Height (cm)");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("BP (mmHg)");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Pregnant");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("LMP");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Breastfeeding");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Next Appointment");
            String query = "SELECT DISTINCT c.facility_id, patient_id, hospital_num, f.name facility, p.uuid puuid, c.date_visit, c.clinic_stage, c.func_status, " +
                    "   c.tb_status, c.body_weight, c.height, c.bp, c.pregnant, c.lmp, c.breastfeeding, " +
                    "   c.next_appointment, hospital_num FROM clinic c JOIN patient p ON c.patient_id = p.id JOIN facility f on f.id = c.facility_id " +
                    "   WHERE c.archived = false AND  p.archived = false AND c.facility_id IN (:facilities) AND commence = false  and " +
                    "   cast(p.extra->>'art' as boolean) = true " +
                    "   ORDER BY c.facility_id, patient_id, date_visit desc";
            SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("facilities", facilityIds);
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
                        cell[0].setCellValue((rs.getDate("date_visit") == null) ? "" :
                                dateFormatExcel.format(rs.getDate("date_visit")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("clinic_stage"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("func_status"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("tb_status"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(Double.toString(rs.getDouble("body_weight")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(Double.toString(rs.getDouble("height")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("bp"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getBoolean("pregnant"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("lmp") == null) ? "" :
                                dateFormatExcel.format(rs.getDate("lmp")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getBoolean("breastfeeding"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("next_appointment") == null) ? "" :
                                dateFormatExcel.format(rs.getDate("next_appointment")));
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
