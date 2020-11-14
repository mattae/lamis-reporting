package org.fhi360.lamis.modules.reporting.service.converter;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PharmacyDataConverter {
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
            cell[0].setCellValue("Regimen Line");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Regimen");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Refill");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Next Appointment");
            String query = "select distinct ph.facility_id, f.name facility, patient_id, hospital_num, p.uuid puuid, " +
                    "date_visit, t.description regimen_type, r.description regimen, jsonb_extract_path_text(l,'duration') refill, " +
                    "next_appointment from pharmacy ph join patient p on p.id = ph.patient_id join facility f on f.id = " +
                    "ph.facility_id, jsonb_array_elements(lines) with ordinality a(l) join regimen_type t on " +
                    "cast(jsonb_extract_path_text(l,'regimen_type_id') as integer) = t.id join regimen r on " +
                    "cast(jsonb_extract_path_text(l,'regimen_id') as integer) = r.id  where ph.archived = false and " +
                    "p.archived = false and p.facility_id in (:facilities) and cast(p.extra->>'art' as boolean) = true order by 2, 3, 6";
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
                        cell[0].setCellValue(rs.getString("regimen_type"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("regimen"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("refill"));
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
