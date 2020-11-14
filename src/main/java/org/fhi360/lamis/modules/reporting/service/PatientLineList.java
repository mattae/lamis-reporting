package org.fhi360.lamis.modules.reporting.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.fhi360.lamis.modules.reporting.service.vm.PatientQueryParams;
import org.fhi360.lamis.modules.reporting.service.vm.PatientVM;
import org.lamisplus.modules.lamis.legacy.domain.entities.Patient;
import org.lamisplus.modules.lamis.legacy.domain.entities.StatusHistory;
import org.lamisplus.modules.lamis.legacy.domain.entities.enumerations.ClientStatus;
import org.lamisplus.modules.lamis.legacy.domain.repositories.PatientRepository;
import org.lamisplus.modules.lamis.legacy.domain.repositories.PharmacyRepository;
import org.lamisplus.modules.lamis.legacy.domain.repositories.StatusHistoryRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientLineList {
    private final StatusHistoryRepository statusHistoryRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PatientRepository patientRepository;
    private final JdbcTemplate jdbcTemplate;

    @SneakyThrows
    public ByteArrayOutputStream build(PatientQueryParams params) {

        ByteArrayOutputStream baos;
        List<PatientVM> patients = listOfPatients(params);
        if (StringUtils.equals(params.getFormat(), "xlsx")) {
            baos = buildExcel(patients);
        } else {
            baos = buildPdf(patients, params);
        }

        return baos;
    }

    public List<PatientVM> listOfPatients(PatientQueryParams params) {

        String query = "SELECT * FROM (" +
                "               SELECT id, hospital_num, surname, other_names, gender, date_birth, status_at_registration status_registration, address, phone," +
                "               cast(extra->>'art' as boolean) art, date_started, date_registration," +
                "               (SELECT clinic_stage FROM clinic WHERE patient_id = p.id AND archived = false AND date_visit <= CURRENT_DATE ORDER BY date_visit DESC LIMIT 1)," +
                "               lga_id, DATEDIFF('YEAR', date_birth, CURRENT_DATE) AS age, (SELECT status FROM status_history WHERE patient_id = p.id " +
                "               AND date_status <= CURRENT_DATE AND archived = false ORDER BY date_status DESC, id DESC LIMIT 1) current_status, (SELECT date_status FROM status_history WHERE patient_id = p.id " +
                "               AND date_status <= CURRENT_DATE AND archived = false ORDER BY date_status DESC, id DESC LIMIT 1) date_current_status, (SELECT jsonb_extract_path_text(l,'result') result FROM laboratory " +
                "               , jsonb_array_elements(lines) with ordinality a(l) WHERE patient_id = p.id AND archived = false AND cast(jsonb_extract_path_text(l,'lab_test_id') as integer) = 16 " +
                "               AND date_result_received <= CURRENT_DATE ORDER BY date_result_received DESC LIMIT 1) last_viral_load, (SELECT date_result_received FROM laboratory " +
                "               , jsonb_array_elements(lines) with ordinality a(l) WHERE patient_id = p.id AND archived = false AND cast(jsonb_extract_path_text(l,'lab_test_id') as integer) = 16 " +
                "               AND date_result_received <= CURRENT_DATE ORDER BY date_result_received DESC LIMIT 1) date_last_viral_load, (SELECT date_visit + cast(jsonb_extract_path_text(l,'duration') as integer) +" +
                "               INTERVAL '28 DAYS' < CURRENT_DATE FROM pharmacy ph , jsonb_array_elements(lines) with ordinality a(l) WHERE cast(jsonb_extract_path_text(l,'regimen_type_id') as integer) in (1,2,3,4,14) AND patient_id = p.id " +
                "               AND date_visit <= CURRENT_DATE AND ph.archived = false ORDER BY date_visit DESC, jsonb_extract_path_text(l,'duration') DESC LIMIT 1) ltfu," +
                "               (SELECT t.description FROM pharmacy ph, jsonb_array_elements(lines) with ordinality a(l) JOIN regimen_type t ON t.id = cast(jsonb_extract_path_text(l,'regimen_type_id') as integer)" +
                "                WHERE cast(jsonb_extract_path_text(l,'regimen_type_id') as integer) IN (1,2,3,4,14) AND ph.patient_id = p.id AND date_visit <= CURRENT_DATE AND ph.archived = false ORDER BY date_visit DESC, jsonb_extract_path_text(l,'duration') DESC LIMIT 1) regimen_type," +
                "               (SELECT status FROM status_history WHERE patient_id = p.id AND date_status <= CURRENT_DATE AND archived = false ORDER BY date_status DESC, id ASC LIMIT 1) alt_status " +
                "               FROM patient p WHERE facility_id = ? AND archived = false " +
                "       ) as pl WHERE art = true";

        List<PatientVM> patients = jdbcTemplate.query(query, rs -> {
            List<PatientVM> vms = new ArrayList<>();
            while (rs.next()) {
                PatientVM vm = new PatientVM();
                vm.setHospitalNum(rs.getString("hospital_num"));
                vm.setSurname(StringUtils.trimToEmpty(rs.getString("surname")));
                vm.setOtherNames(StringUtils.trimToEmpty(rs.getString("other_names")));
                vm.setGender(rs.getString("gender"));
                vm.setDateBirth(rs.getObject("date_birth", LocalDate.class));
                vm.setStatusRegistration(rs.getString("status_registration"));
                vm.setAddress(rs.getString("address"));
                vm.setPhone(rs.getString("phone"));
                vm.setDateStarted(rs.getObject("date_started", LocalDate.class));
                vm.setDateRegistration(rs.getObject("date_registration", LocalDate.class));
                vm.setClinicStage(rs.getString("clinic_stage"));
                vm.setLgaId(rs.getObject("lga_id", Long.class));
                vm.setAge(rs.getInt("age"));
                Patient p = new Patient();
                p.setId(rs.getLong("id"));
                vm.setCurrentStatus(getCurrentStatus(p));
                vm.setDateCurrentStatus(rs.getObject("date_current_status", LocalDate.class));
                vm.setRegimenType(rs.getString("regimen_type"));
                vm.setLastViralLoad(rs.getString("last_viral_load"));
                vm.setDateLastViralLoad(rs.getObject("date_last_viral_load", LocalDate.class));
                vm.setLtfu(rs.getObject("ltfu", Boolean.class));
                vms.add(vm);
            }
            return vms;
        }, params.getFacilityId());

        patients = patients.stream()
                .filter(patient -> {
                    boolean include = true;
                    if (StringUtils.isNotEmpty(params.getGender()) && !StringUtils.equals(params.getGender(), "--All--")) {
                        include = StringUtils.equalsAnyIgnoreCase(patient.getGender(), params.getGender());
                    }
                    if (params.getAgeBegin() != null) {
                        include = include && patient.getAge() >= params.getAgeBegin();
                    }
                    if (params.getAgeEnd() != null) {
                        include = include && patient.getAge() <= params.getAgeEnd();
                    }
                    if (params.getLgaId() != null) {
                        include = include && Objects.equals(patient.getLgaId(), params.getLgaId());
                    }

                    if (StringUtils.isNotEmpty(params.getCurrentStatus()) && !StringUtils.equals(params.getCurrentStatus(), "--All--")) {
                        if (StringUtils.equals(params.getCurrentStatus(), "Currently Active")) {
                            include = include && patient.getLtfu() != null && !patient.getLtfu();
                        } else {
                            try {
                                include = include && Objects.equals(patient.getCurrentStatus(),
                                        ClientStatus.valueOf(params.getCurrentStatus()).getStatus());
                            } catch (Exception e) {
                                include = false;
                            }
                        }
                    }

                    if (params.getDateCurrentStatusBegin() != null) {
                        include = include && patient.getDateCurrentStatus() != null
                                && !patient.getDateCurrentStatus().isBefore(params.getDateCurrentStatusBegin());
                    }
                    if (StringUtils.isNotEmpty(params.getRegimenType())) {
                        include = include && Objects.equals(patient.getRegimenType(), params.getRegimenType());
                    }
                    if (params.getDateRegistrationBegin() != null) {
                        include = include && patient.getDateRegistration() != null &&
                                !patient.getDateRegistration().isBefore(params.getDateRegistrationBegin());
                    }
                    if (params.getDateRegistrationEnd() != null) {
                        include = include && patient.getDateRegistration() != null &&
                                !patient.getDateRegistration().isAfter(params.getDateRegistrationEnd());
                    }
                    if (params.getDateStartedBegin() != null) {
                        include = include && patient.getDateStarted() != null &&
                                !patient.getDateStarted().isBefore(params.getDateStartedBegin());
                    }
                    if (params.getDateStartedEnd() != null) {
                        include = include && patient.getDateStarted() != null &&
                                !patient.getDateStarted().isAfter(params.getDateStartedEnd());
                    }
                    if (StringUtils.isNotEmpty(params.getClinicStage())) {
                        include = include && Objects.equals(patient.getClinicStage(), params.getClinicStage());
                    }
                    if (params.getViralLoadBegin() != null) {
                        try {
                            double viralLoad = Double.parseDouble(patient.getLastViralLoad());
                            include = include && patient.getLastViralLoad() != null && viralLoad >= params.getViralLoadBegin();
                        } catch (Exception ignored) {
                            include = false;
                        }
                    }
                    if (params.getViralLoadEnd() != null) {
                        try {
                            double viralLoad = Double.parseDouble(patient.getLastViralLoad());
                            include = include && patient.getLastViralLoad() != null && viralLoad <= params.getViralLoadEnd();
                        } catch (Exception ignored) {
                            include = false;
                        }
                    }
                    if (params.getDateLastViralLoadBegin() != null) {
                        include = include && patient.getDateLastViralLoad() != null &&
                                !patient.getDateLastViralLoad().isBefore(params.getDateLastViralLoadBegin());
                    }
                    if (params.getDateLastViralLoadEnd() != null) {
                        include = include && patient.getDateLastViralLoad() != null &&
                                !patient.getDateLastViralLoad().isAfter(params.getDateLastViralLoadEnd());
                    }
                    return include;
                })
                .sorted(Comparator.comparing(PatientVM::getCurrentStatus))
                .collect(Collectors.toList());

        return patients;
    }

    public String getCurrentStatus(Patient patient) {
        Optional<Date> date = pharmacyRepository.getLTFUDate(patient.getId());
        Optional<StatusHistory> statusHistory = statusHistoryRepository.getCurrentStatusForPatientAt(patient, LocalDate.now());
        String status;
        if (date.isPresent()) {
            LocalDate ltfuDate = Instant.ofEpochMilli(date.get().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();

            if (!ltfuDate.isBefore(LocalDate.now())) {
                if (statusHistory.isPresent() && statusHistory.get().getStatus().equals(ClientStatus.KNOWN_DEATH)) {
                    status = ClientStatus.KNOWN_DEATH.name();
                } else if (statusHistory.isPresent() && statusHistory.get().getStatus().equals(ClientStatus.ART_TRANSFER_OUT)) {
                    status = ClientStatus.ART_TRANSFER_OUT.name();
                } else if (statusHistory.isPresent() && statusHistory.get().getStatus().equals(ClientStatus.STOPPED_TREATMENT)) {
                    status = ClientStatus.STOPPED_TREATMENT.name();
                } else if (statusHistory.isPresent()) {
                    StatusHistory history = statusHistory.get();
                    if (history.getStatus().equals(ClientStatus.ART_RESTART)) {
                        status = ClientStatus.ART_RESTART.name();
                    } else if (history.getStatus().equals(ClientStatus.ART_START)) {
                        status = ClientStatus.ART_START.name();
                    } else if (history.getStatus().equals(ClientStatus.ART_TRANSFER_IN)) {
                        status = ClientStatus.ART_TRANSFER_IN.name();
                    } else {
                        status = ClientStatus.ART_START.name();
                    }
                } else {
                    if (patient.getStatusAtRegistration() != null) {
                        status = patient.getStatusAtRegistration().name();
                    } else {
                        status = ClientStatus.ART_START.name();
                    }
                }
            } else {
                if (statusHistory.isPresent() && statusHistory.get().getStatus().equals(ClientStatus.KNOWN_DEATH)) {
                    status = ClientStatus.KNOWN_DEATH.name();
                } else if (statusHistory.isPresent() && statusHistory.get().getStatus().equals(ClientStatus.ART_TRANSFER_OUT)) {
                    status = ClientStatus.ART_TRANSFER_OUT.name();
                } else if (statusHistory.isPresent() && statusHistory.get().getStatus().equals(ClientStatus.STOPPED_TREATMENT)) {
                    status = ClientStatus.STOPPED_TREATMENT.name();
                } else {
                    status = ClientStatus.LOST_TO_FOLLOWUP.name();
                }
            }
        } else {
            if (patient.getStatusAtRegistration() != null) {
                status = patient.getStatusAtRegistration().name();
            } else {
                status = ClientStatus.ART_START.name();
            }
        }
        return status;
    }

    @SneakyThrows
    private ByteArrayOutputStream buildPdf(List<PatientVM> patients, PatientQueryParams params) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();

        Map<String, Object> parameters = new HashMap<>(headers(params.getFacilityId()));
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(patients, false);

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                new ClassPathResource("jasperTemplates/patient_line_list.jasper").getInputStream(), parameters, dataSource);
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));


        SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
        reportConfig.setSizePageToContent(true);
        reportConfig.setForceLineBreakPolicy(false);

        SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
        exportConfig.setMetadataAuthor("mattae");
        exportConfig.setEncrypted(true);
        exportConfig.setAllowedPermissionsHint("PRINTING");

        exporter.setConfiguration(reportConfig);
        exporter.setConfiguration(exportConfig);

        exporter.exportReport();

        return baos;
    }

    private ByteArrayOutputStream buildExcel(List<PatientVM> patients) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Workbook workbook = new SXSSFWorkbook(100);  // turn off auto-flushing and accumulate all rows in memory
        Sheet sheet = workbook.createSheet();

        //Create a new font
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());

        //Create a style and set the font into it
        CellStyle style = getCellStyle(workbook);

        final int[] rowNum = {0};
        final int[] cellNum = {0};
        Row row = sheet.createRow(rowNum[0]++);
        Cell cell = row.createCell(cellNum[0]++);
        cell.setCellValue("S/No.");
        cell.setCellStyle(style);
        cell = row.createCell(cellNum[0]++);
        cell.setCellValue("Hospital Num");
        cell.setCellStyle(style);
        cell = row.createCell(cellNum[0]++);
        cell.setCellValue("Name");
        cell.setCellStyle(style);
        cell = row.createCell(cellNum[0]++);
        cell.setCellValue("Age");
        cell.setCellStyle(style);
        cell = row.createCell(cellNum[0]++);
        cell.setCellValue("Gender");
        cell.setCellStyle(style);
        cell = row.createCell(cellNum[0]++);
        cell.setCellValue("Current Status");
        cell.setCellStyle(style);
        cell = row.createCell(cellNum[0]++);
        cell.setCellValue("Date of Current Status (yyyy-mm-dd)");
        cell.setCellStyle(style);
        cell = row.createCell(cellNum[0]++);
        cell.setCellValue("ART Start Date (yyyy-mm-dd)");
        cell.setCellStyle(style);
        cell = row.createCell(cellNum[0]++);
        cell.setCellValue("Last Viral Load");
        cell.setCellStyle(style);
        cell = row.createCell(cellNum[0]++);
        cell.setCellValue("Date of Last Viral Load (yyyy-mm-dd)");
        cell.setCellStyle(style);
        cell = row.createCell(cellNum[0]++);
        cell.setCellValue("Address");
        cell.setCellStyle(style);
        cell = row.createCell(cellNum[0]);
        cell.setCellValue("Phone");
        cell.setCellStyle(style);

        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

        patients.forEach(patient -> {
            cellNum[0] = 0;
            Row dataRow = sheet.createRow(rowNum[0]++);
            Cell dataCell = dataRow.createCell(cellNum[0]++);
            dataCell.setCellValue(rowNum[0] - 1);
            dataCell = dataRow.createCell(cellNum[0]++);
            dataCell.setCellValue(patient.getHospitalNum());
            dataCell = dataRow.createCell(cellNum[0]++);
            dataCell.setCellValue(patient.getSurname() + ", " + patient.getOtherNames());
            dataCell = dataRow.createCell(cellNum[0]++);
            dataCell.setCellValue(patient.getAge());
            dataCell = dataRow.createCell(cellNum[0]++);
            dataCell.setCellValue(patient.getGender());
            dataCell = dataRow.createCell(cellNum[0]++);
            dataCell.setCellValue(patient.getCurrentStatus());
            dataCell = dataRow.createCell(cellNum[0]++);
            if (patient.getDateCurrentStatus() != null) {
                dataCell.setCellValue(convertToDate(patient.getDateCurrentStatus()));
            }
            dataCell.setCellStyle(dateStyle);
            dataCell = dataRow.createCell(cellNum[0]++);
            if (patient.getDateStarted() != null) {
                dataCell.setCellValue(convertToDate(patient.getDateStarted()));
            }
            dataCell.setCellStyle(dateStyle);
            dataCell = dataRow.createCell(cellNum[0]++);
            dataCell.setCellValue(patient.getLastViralLoad());
            dataCell = dataRow.createCell(cellNum[0]++);
            if (patient.getDateLastViralLoad() != null) {
                dataCell.setCellValue(convertToDate(patient.getDateLastViralLoad()));
            }
            dataCell.setCellStyle(dateStyle);
            dataCell = dataRow.createCell(cellNum[0]++);
            dataCell.setCellValue(patient.getAddress());
            dataCell = dataRow.createCell(cellNum[0]);
            dataCell.setCellValue(patient.getPhone());
        });

        try {
            workbook.write(baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos;
    }

    private CellStyle getCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        style.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
        style.setFillPattern(FillPatternType.FINE_DOTS);
        style.setFont(font);
        return style;
    }

    public Date convertToDate(LocalDate dateToConvert) {
        return java.sql.Date.valueOf(dateToConvert);
    }


    public Map<String, String> headers(Long facilityId) {
        Map<String, String> params = new HashMap<>();

        params.put("reportTitle", "List of all Patients");

        String query = "SELECT DISTINCT f.name, s.name state, l.name lga FROM facility f JOIN state s ON s.id = state_id " +
                "JOIN lga l ON l.id = lga_id WHERE f.id = ?";
        jdbcTemplate.query(query, rs -> {
            while (rs.next()) {
                params.put("facilityName", rs.getString("name"));
                params.put("lga", rs.getString("lga"));
                params.put("state", rs.getString("state"));
            }
            return null;
        }, facilityId);

        return params;
    }
}
