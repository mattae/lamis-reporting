package org.fhi360.lamis.modules.reporting.service.converter;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.fhi360.lamis.modules.reporting.service.PatientLineList;
import org.fhi360.lamis.modules.reporting.util.RegimenHistory;
import org.fhi360.lamis.modules.reporting.util.RegimenIntrospector;
import org.lamisplus.modules.lamis.legacy.domain.entities.Patient;
import org.lamisplus.modules.lamis.legacy.domain.entities.enumerations.ClientStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PatientDataConverter {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final PatientLineList patientLineList;


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
            cell[0].setCellValue("Facility Name");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("State");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("LGA");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Patient Id");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Hospital Num");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Unique ID");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Surname");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Other Names");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Date Birth");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Age");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Gender");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Marital Status");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Education");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Occupation");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("State of Residence");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Lga of Residence");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Address");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Phone");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Care Entry Point");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Date of Confirmed HIV Test");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Date Registration");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Status at Registration");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Current Status");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Date Current Status");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("ART Start Date");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Baseline CD4");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Baseline CD4p");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Systolic BP");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Diastolic BP");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Baseline Clinic Stage");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Baseline Functional Status");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Baseline Weight (kg)");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Baseline Height (cm)");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("First Regimen Line");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("First Regimen");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("First NRTI");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("First NNRTI");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Current Regimen Line");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Current Regimen");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Current NRTI");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Current NNRTI");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Date Subsituted/Switched");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Date of Last Refill");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Last Refill Duration (days)");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Date of Next Refill");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Last Clinic Stage");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Date of Last Clinic");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Date of Next Clinic");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Last CD4");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Last CD4p");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Date of Last CD4");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Last Viral Load");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Date of Last Viral Load");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Viral Load Due Date");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Viral Load Type");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("DMOC Type");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Date Devolved");
            cell[0] = row[0].createCell(cellnum[0]++);
            cell[0].setCellValue("Date Returned to Facility");

            String query = getQuery();
            SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("facilities", facilityIds);
            try {
                namedParameterJdbcTemplate.query(query, namedParameters, rs -> {
                    while (rs.next()) {
                        cellnum[0] = 0;
                        row[0] = sheet.createRow(rownum[0]++);
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getLong("facility_id"));
                        long facilityId = rs.getLong("facility_id");
                        Map<String, Object> facility = getFacility(facilityId);
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((String) facility.get("name"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((String) facility.get("state"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((String) facility.get("lga"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("uuid"));
                        long patientId = rs.getLong("id");
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("hospital_num"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("unique_id"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        String surname = rs.getString("surname");
                        cell[0].setCellValue(surname);
                        cell[0] = row[0].createCell(cellnum[0]++);
                        String otherNames = rs.getString("other_names");
                        cell[0].setCellValue(otherNames);
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("date_birth") == null) ? "" :
                                dateFormatExcel.format(rs.getDate("date_birth")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(Integer.toString(rs.getInt("age")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(StringUtils.equals(rs.getString("gender"), "FEMALE") ? "Female" : "Male");
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("marital_status"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("education"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("occupation"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("state"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("lga"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        String address = rs.getString("address");
                        cell[0].setCellValue(address);
                        cell[0] = row[0].createCell(cellnum[0]++);
                        String phone = rs.getString("phone");
                        cell[0].setCellValue(phone);
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("entry_point"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("date_confirmed_hiv") == null) ? "" :
                                dateFormatExcel.format(rs.getDate("date_confirmed_hiv")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("date_registration") == null) ? "" :
                                dateFormatExcel.format(rs.getDate("date_registration")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        try {
                            cell[0].setCellValue(ClientStatus.valueOf(rs.getString("status_at_registration")).getStatus());
                        } catch (Exception ignored) {
                        }
                        cell[0] = row[0].createCell(cellnum[0]++);
                        Patient patient = new Patient();
                        patient.setId(patientId);
                        try {
                            cell[0].setCellValue(ClientStatus.valueOf(patientLineList.getCurrentStatus(patient)).getStatus());
                        } catch (Exception ignored) {
                        }
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("date_current_status") == null) ? "" : dateFormatExcel.format(rs.getDate("date_current_status")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("date_started") == null) ? "" : dateFormatExcel.format(rs.getDate("date_started")));

                        //Adding baseline data
                        String q = "SELECT c.*, r.description regimen, t.description regimen_type FROM clinic c join regimen_type t on t.id = regimen_type_id join regimen r on " +
                                "r.id = regimen_id WHERE archived = false AND commence = true AND patient_id = ? LIMIT 1";
                        boolean[] found = {false};
                        final String[] regimen1 = {""};
                        final String[] regimenType1 = {""};
                        jdbcTemplate.query(q, rs1 -> {
                            found[0] = true;
                            cell[0] = row[0].createCell(cellnum[0]++);
                            try {
                                cell[0].setCellValue(rs1.getDouble("cd4") != 0 ? Double.toString(rs1.getDouble("cd4")) : "");
                            } catch (Exception ignored) {
                            }
                            cell[0] = row[0].createCell(cellnum[0]++);
                            try {
                                cell[0].setCellValue(rs1.getDouble("cd4p") != 0 ? Double.toString(rs1.getDouble("cd4p")) : "");
                            } catch (Exception ignored) {
                            }
                            //Solve the BP
                            String[] bpData = (!"".equals(rs1.getString("bp")) && rs1.getString("bp") != null) ? rs1.getString("bp").split("/") : new String[]{};
                            if (bpData.length == 2) {
                                cell[0] = row[0].createCell(cellnum[0]++);
                                cell[0].setCellValue(bpData[0]);
                                cell[0] = row[0].createCell(cellnum[0]++);
                                cell[0].setCellValue(bpData[1]);
                            } else {
                                cell[0] = row[0].createCell(cellnum[0]++);
                                cell[0].setCellValue("");
                                cell[0] = row[0].createCell(cellnum[0]++);
                                cell[0].setCellValue("");
                            }
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue(rs1.getString("clinic_stage"));
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue(rs1.getString("func_status"));
                            cell[0] = row[0].createCell(cellnum[0]++);
                            try {
                                cell[0].setCellValue(Double.toString(rs1.getDouble("body_weight")));
                            } catch (Exception ignored) {
                            }
                            cell[0] = row[0].createCell(cellnum[0]++);
                            try {
                                cell[0].setCellValue(Double.toString(rs1.getDouble("height")));
                            } catch (Exception ignored) {
                            }
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue(StringUtils.trimToEmpty(rs1.getString("regimen_type")));
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue(RegimenIntrospector.resolveRegimen(rs1.getString("regimen")));
                            cell[0] = row[0].createCell(cellnum[0]++);
                            String nrti = StringUtils.trimToEmpty(rs1.getString("regimen"));
                            cell[0].setCellValue(nrti);
                            cell[0] = row[0].createCell(cellnum[0]++);
                            String nnrti = StringUtils.trimToEmpty(rs1.getString("regimen"));
                            cell[0].setCellValue(nnrti);
                            regimenType1[0] = StringUtils.trimToEmpty(rs1.getString("regimen_type"));
                            regimen1[0] = StringUtils.trimToEmpty(rs1.getString("regimen"));
                        }, patientId);

                        if (!found[0]) {
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue("");
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue("");
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue("");
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue("");
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue("");
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue("");
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue("");
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue("");
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue("");
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue("");
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue("");
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue("");
                        }
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(StringUtils.trimToEmpty(rs.getString("regimen_type")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(RegimenIntrospector.resolveRegimen(rs.getString("regimen")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        String nrti = getNrti(StringUtils.trimToEmpty(rs.getString("regimen")));
                        cell[0].setCellValue(nrti);
                        cell[0] = row[0].createCell(cellnum[0]++);
                        String nnrti = getNnrti(StringUtils.trimToEmpty(rs.getString("regimen")));
                        cell[0].setCellValue(nnrti);

                        //Determining when regimen was substituted or switched
                        cell[0] = row[0].createCell(cellnum[0]++);
                        String regimen_type2 = StringUtils.trimToEmpty(rs.getString("regimen_type"));
                        String regimen2 = StringUtils.trimToEmpty(rs.getString("regimen"));
                        if (!regimenType1[0].isEmpty() && !regimen_type2.isEmpty() && !regimen1[0].isEmpty() && !regimen2.isEmpty()) {
                            //If regimen was substituted or switched, get the date of change
                            if (RegimenIntrospector.substitutedOrSwitched(regimen1[0], regimen2)) {
                                RegimenHistory regimenhistory = RegimenIntrospector.getRegimenHistory(patientId, regimen_type2, regimen2);
                                cell[0].setCellValue(regimenhistory.getDateVisit() == null ? "" : dateFormatExcel.format(regimenhistory.getDateVisit()));
                            }
                        }
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("date_last_refill") == null) ? "" : dateFormatExcel.format(rs.getDate("date_last_refill")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        try {
                            cell[0].setCellValue(rs.getInt("last_refill_duration") != 0 ? Double.toString(rs.getInt("last_refill_duration")) : "");
                        } catch (Exception ignored) {
                        }
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("date_next_refill") == null) ? "" : dateFormatExcel.format(rs.getDate("date_next_refill")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("clinic_stage"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("date_last_clinic") == null) ? "" : dateFormatExcel.format(rs.getDate("date_last_clinic")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("date_next_clinic") == null) ? "" : dateFormatExcel.format(rs.getDate("date_next_clinic")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        try {
                            cell[0].setCellValue(rs.getDouble("last_cd4") != 0 ? Double.toString(rs.getDouble("last_cd4")) : "");
                        } catch (Exception ignored) {
                        }
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue("");
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("date_last_cd4") == null) ? "" : dateFormatExcel.format(rs.getDate("date_last_cd4")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        try {
                            cell[0].setCellValue(rs.getDouble("last_viral_load") != 0 ? Double.toString(rs.getDouble("last_viral_load")) : "");
                        } catch (Exception ignored) {
                        }
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue((rs.getDate("date_last_viral_load") == null) ? "" : dateFormatExcel.format(rs.getDate("date_last_viral_load")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        //cell[0].setCellValue((rs.getDate("viral_load_due_date") == null) ? "" : dateFormatExcel.format(rs.getDate("viral_load_due_date")));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        cell[0].setCellValue(rs.getString("viral_load_type"));
                        cell[0] = row[0].createCell(cellnum[0]++);
                        Date dateReturnedToFacility = rs.getDate("date_returned_to_facility");
                        if (dateReturnedToFacility != null) {
                            cell[0].setCellValue("");
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue("");
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue(dateFormatExcel.format(dateReturnedToFacility));
                        } else {
                            cell[0].setCellValue(rs.getString("dmoc_type"));
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue((rs.getDate("date_devolved") == null) ? "" : dateFormatExcel.format(rs.getDate("date_devolved")));
                            cell[0] = row[0].createCell(cellnum[0]++);
                            cell[0].setCellValue("");
                        }
                    }
                    return null;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception ignored) {
        }
        try {
            workbook.write(baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos;
    }


    private Map<String, Object> getFacility(long facilityId) {
        Map<String, Object> facilityMap = new HashMap<>();
        // fetch the required records from the database
        String query = "SELECT f.name, f.address1, f.address2, f.phone1, f.phone2, "
                + "f.email, l.name AS lga, s.name AS state FROM facility f JOIN lga l ON f.lga_id = l.id "
                + "JOIN state s ON f.state_id = s.id WHERE f.id = ?";
        jdbcTemplate.query(query, rs -> {
            while (rs.next()) {
                facilityMap.put("name", rs.getString("name"));
                facilityMap.put("lga", rs.getString("lga"));
                facilityMap.put("state", rs.getString("state"));
            }
            return null;
        }, facilityId);
        return facilityMap;
    }

    private String getNrti(String regimen) {
        String nrti = regimen == null ? "" : "Other";
        if (StringUtils.indexOfIgnoreCase(regimen, "d4T") != -1) {
            nrti = "D4T (Stavudine)";
        } else {
            if (StringUtils.indexOfIgnoreCase(regimen, "AZT") != -1) {
                nrti = "AZT (Zidovudine)";
            } else {
                if (StringUtils.indexOfIgnoreCase(regimen, "TDF") != -1) {
                    nrti = "TDF (Tenofovir)";
                } else {
                    if (StringUtils.indexOfIgnoreCase(regimen, "DDI") != -1) {
                        nrti = "DDI (Didanosine)";
                    }
                }
            }
        }
        return nrti;
    }

    private String getNnrti(String regimen) {
        String nnrti = regimen == null ? "" : "Other";
        if (StringUtils.indexOfIgnoreCase(regimen, "EFV") != -1) {
            nnrti = " EFV (Efavirenz)";
        } else {
            if (StringUtils.indexOfIgnoreCase(regimen, "NVP") != -1) {
                nnrti = " NVP (Nevirapine)";
            }
        }
        return nnrti;
    }

    private String getQuery() {
        return "SELECT * FROM (" +
                "                              SELECT p.id, uuid, facility_id, unique_id, education,occupation, hospital_num, surname, other_names, gender, date_birth, marital_status, status_at_registration, " +
                "                              date_confirmed_hiv, entry_point, address, phone, date_started, date_registration, datediff('year', date_birth, current_date) age, l.name lga, s.name state," +
                "                              cast(extra->>'art' as boolean) art," +
                "                              (SELECT clinic_stage FROM clinic WHERE patient_id = p.id AND archived = false AND date_visit <= CURRENT_DATE ORDER BY date_visit DESC LIMIT 1)," +
                "                              (SELECT date_visit FROM clinic WHERE patient_id = p.id AND archived = false AND date_visit <= CURRENT_DATE ORDER BY date_visit DESC LIMIT 1) date_last_clinic," +
                "                              (SELECT next_appointment FROM clinic WHERE patient_id = p.id AND archived = false AND date_visit <= CURRENT_DATE ORDER BY date_visit DESC LIMIT 1) date_next_clinic," +
                "                              (SELECT dmoc_type FROM devolve WHERE patient_id = p.id AND archived = false AND date_devolved <= CURRENT_DATE ORDER BY date_devolved DESC LIMIT 1) dmoc_type," +
                "                              (SELECT date_devolved FROM devolve WHERE patient_id = p.id AND archived = false AND date_devolved <= CURRENT_DATE ORDER BY date_devolved DESC LIMIT 1) date_devolved," +
                "                              (SELECT date_returned_to_facility FROM devolve WHERE patient_id = p.id AND archived = false AND date_devolved <= CURRENT_DATE ORDER BY date_devolved DESC LIMIT 1) date_returned_to_facility," +
                "                              lga_id, DATEDIFF('YEAR', date_birth, CURRENT_DATE) AS age, (SELECT status FROM status_history WHERE patient_id = p.id " +
                "                              AND date_status <= CURRENT_DATE AND archived = false ORDER BY date_status DESC, id DESC LIMIT 1) current_status, (SELECT date_status FROM status_history WHERE patient_id = p.id " +
                "                              AND date_status <= CURRENT_DATE AND archived = false ORDER BY date_status DESC, id DESC LIMIT 1) date_current_status, (SELECT jsonb_extract_path_text(l,'result') result FROM laboratory " +
                "                              , jsonb_array_elements(lines) with ordinality a(l) WHERE patient_id = p.id AND archived = false AND cast(jsonb_extract_path_text(l,'lab_test_id') as integer) = 16 " +
                "                              AND date_result_received <= CURRENT_DATE ORDER BY date_result_received DESC LIMIT 1) last_viral_load, (SELECT date_result_received FROM laboratory " +
                "                              , jsonb_array_elements(lines) with ordinality a(l) WHERE patient_id = p.id AND archived = false AND cast(jsonb_extract_path_text(l,'lab_test_id') as integer) = 16 " +
                "                              AND date_result_received <= CURRENT_DATE ORDER BY date_result_received DESC LIMIT 1) date_last_viral_load, (SELECT date_visit + cast(jsonb_extract_path_text(l,'duration') as integer) +" +
                "                             INTERVAL '28 DAYS' < CURRENT_DATE FROM pharmacy ph, jsonb_array_elements(lines) with ordinality a(l) WHERE cast(jsonb_extract_path_text(l,'regimen_type_id') as integer) in (1,2,3,4,14) AND ph.patient_id = p.id " +
                "                              AND date_visit <= CURRENT_DATE AND ph.archived = false ORDER BY date_visit DESC, cast(jsonb_extract_path_text(l,'duration') as integer) DESC LIMIT 1) ltfu, (SELECT date_visit " +
                "                              FROM pharmacy ph, jsonb_array_elements(lines) with ordinality a(l) WHERE cast(jsonb_extract_path_text(l,'regimen_type_id') as integer) in (1,2,3,4,14) AND ph.patient_id = p.id" +
                "                              AND date_visit <= CURRENT_DATE AND ph.archived = false ORDER BY date_visit DESC, cast(jsonb_extract_path_text(l,'duration') as integer) DESC LIMIT 1) date_last_refill, (SELECT next_appointment " +
                "                              FROM pharmacy ph, jsonb_array_elements(lines) with ordinality a(l) WHERE cast(jsonb_extract_path_text(l,'regimen_type_id') as integer) in (1,2,3,4,14) AND ph.patient_id = p.id " +
                "                              AND date_visit <= CURRENT_DATE AND ph.archived = false ORDER BY date_visit DESC, cast(jsonb_extract_path_text(l,'duration') as integer) DESC LIMIT 1) date_next_refill,  (SELECT cast(jsonb_extract_path_text(l,'duration') as integer)" +
                "                              FROM pharmacy ph, jsonb_array_elements(lines) with ordinality a(l) WHERE cast(jsonb_extract_path_text(l,'regimen_type_id') as integer) in (1,2,3,4,14) AND ph.patient_id = p.id " +
                "                             AND date_visit <= CURRENT_DATE AND ph.archived = false ORDER BY date_visit DESC, cast(jsonb_extract_path_text(l,'duration') as integer) DESC LIMIT 1) last_refill_duration," +
                "                              (SELECT r.description FROM pharmacy ph, jsonb_array_elements(lines) with ordinality a(l) JOIN regimen r ON r.id = cast(jsonb_extract_path_text(l,'regimen_id') as integer) WHERE r.regimen_type_id " +
                "                              IN (1,2,3,4,14) AND ph.patient_id = p.id AND date_visit <= CURRENT_DATE AND ph.archived = false ORDER BY date_visit DESC, cast(jsonb_extract_path_text(l,'duration') as integer) DESC LIMIT 1) regimen," +
                "                              (SELECT t.description FROM pharmacy ph, jsonb_array_elements(lines) with ordinality a(l) JOIN regimen_type t ON t.id = cast(jsonb_extract_path_text(l,'regimen_type_id') as integer) WHERE t.id " +
                "                              IN (1,2,3,4,14) AND ph.patient_id = p.id AND date_visit <= CURRENT_DATE AND ph.archived = false ORDER BY date_visit DESC, cast(jsonb_extract_path_text(l,'duration') as integer) DESC LIMIT 1) regimen_type," +
                "                              (SELECT status FROM status_history WHERE patient_id = p.id AND date_status <= CURRENT_DATE AND archived = false ORDER BY date_status DESC, id ASC LIMIT 1) alt_status," +
                "                              (SELECT jsonb_extract_path_text(l,'result') result FROM laboratory, jsonb_array_elements(lines) with ordinality a(l) WHERE patient_id = p.id AND archived = false AND cast(jsonb_extract_path_text(l,'lab_test_id') as integer) = 1 " +
                "                              AND date_result_received <= CURRENT_DATE ORDER BY date_result_received DESC LIMIT 1) last_cd4, (SELECT date_result_received FROM laboratory " +
                "                              , jsonb_array_elements(lines) with ordinality a(l) WHERE patient_id = p.id AND archived = false AND cast(jsonb_extract_path_text(l,'lab_test_id') as integer) = 1 " +
                "                              AND date_result_received <= CURRENT_DATE ORDER BY date_result_received DESC LIMIT 1) date_last_cd4, (SELECT jsonb_extract_path_text(l,'indication') indication FROM laboratory l " +
                "                              , jsonb_array_elements(lines) with ordinality a(l) WHERE patient_id = p.id AND archived = false AND cast(jsonb_extract_path_text(l,'lab_test_id') as integer) = 16 " +
                "                              AND date_result_received <= CURRENT_DATE ORDER BY date_result_received DESC LIMIT 1) viral_load_type" +
                "                              FROM patient p LEFT JOIN lga l on l.id = lga_id LEFT JOIN state s ON s.id = l.state_id WHERE facility_id in (:facilities) AND archived = false" +
                "                              ) as pl where art = true";
    }
}
