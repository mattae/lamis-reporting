package org.fhi360.lamis.modules.reporting.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fhi360.lamis.modules.reporting.service.*;
import org.fhi360.lamis.modules.reporting.service.converter.DataConversionService;
import org.fhi360.lamis.modules.reporting.service.vm.PatientQueryParams;
import org.lamisplus.modules.lamis.legacy.domain.entities.Facility;
import org.lamisplus.modules.lamis.legacy.domain.entities.LabTest;
import org.lamisplus.modules.lamis.legacy.domain.entities.RegimenType;
import org.lamisplus.modules.lamis.legacy.domain.repositories.LabTestRepository;
import org.lamisplus.modules.lamis.legacy.domain.repositories.RegimenTypeRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/reporting")
@RequiredArgsConstructor
public class ReportResource {
    private final ARTSummaryService artSummaryService;
    private final PatientLineList patientLineList;
    private final DataConversionService dataConversionService;
    private final BiometricEnrollmentReportService biometricEnrollmentReportService;
    private final AppointmentReportsService appointmentReportsService;
    private final DevolveReportService devolveReportService;
    private final RegimenTypeRepository regimenTypeRepository;
    private final LabTestRepository labTestRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/art-summary")
    public void artSummary(@RequestParam LocalDate reportingPeriod, @RequestParam Long id, @RequestParam(defaultValue = "false") Boolean today,
                           HttpServletResponse response) throws IOException {
        messagingTemplate.convertAndSend("/topic/art-summary/status", "start");
        ByteArrayOutputStream baos = artSummaryService.build(id, reportingPeriod, today);
        setStream(baos, response);
        messagingTemplate.convertAndSend("/topic/art-summary/status", "end");
    }

    @GetMapping("/biometric-report")
    public void biometricReport(@RequestParam LocalDate start, @RequestParam LocalDate end, @RequestParam Long facilityId,
                                HttpServletResponse response) throws IOException {
        messagingTemplate.convertAndSend("/topic/biometric-report/status", "start");
        ByteArrayOutputStream baos = biometricEnrollmentReportService.fingerprintEnrollmentReport(facilityId, start, end);
        setStream(baos, response);
        messagingTemplate.convertAndSend("/topic/biometric-report/status", "end");
    }

    @GetMapping("/appointment-report")
    public void appointmentReport(@RequestParam LocalDate start, @RequestParam LocalDate end, @RequestParam Long facilityId,
                                  @RequestParam AppointmentReportsService.Appointment type, HttpServletResponse response) throws IOException {
        messagingTemplate.convertAndSend("/topic/appointment-report/status", "start");
        ByteArrayOutputStream baos = appointmentReportsService.process(start, end, facilityId, type);
        setStream(baos, response);
        messagingTemplate.convertAndSend("/topic/appointment-report/status", "end");
    }

    @PostMapping("/patient-line-list")
    public void patientLineList(@RequestBody @Valid PatientQueryParams params, HttpServletResponse response) throws IOException {
        messagingTemplate.convertAndSend("/topic/patient-line-list/status", "start");
        ByteArrayOutputStream baos = patientLineList.build(params);
        setStream(baos, response);
        messagingTemplate.convertAndSend("/topic/patient-line-list/status", "end");
    }

    @GetMapping("devolve-report")
    public void devolveReport(@RequestParam Long facilityId, @RequestParam Boolean cparp, HttpServletResponse response) throws IOException {
        messagingTemplate.convertAndSend("/topic/devolve-report/status", "start");
        ByteArrayOutputStream baos;
        if (Objects.equals(cparp, true)) {
            baos = devolveReportService.cparpDevolveReport(facilityId);
        } else {
            baos = devolveReportService.devolveReport(facilityId);
        }
        setStream(baos, response);
        messagingTemplate.convertAndSend("/topic/devolve-report/status", "end");
    }

    @GetMapping("/regimen-types")
    public List<RegimenType> getRegimenTypes() {
        return regimenTypeRepository.findAll();
    }

    @GetMapping("/list-facilities")
    public List<Facility> availableFacilities() {
        return jdbcTemplate.query("select distinct f.id, f.name from patient join facility f on f.id = facility_id order by 2",
                new BeanPropertyRowMapper<>(Facility.class));
    }

    @GetMapping("/lab-tests")
    public List<LabTest> labTests() {
        return labTestRepository.findAll();
    }

    @GetMapping("/convert-data")
    public void convertData(@RequestParam List<Long> ids, @RequestParam Integer report, @RequestParam(required = false) Long labTest,
                            HttpServletResponse response) throws IOException {
        messagingTemplate.convertAndSend("/topic/data-conversion/status", "start");
        ByteArrayOutputStream baos = dataConversionService.convert(ids, report, labTest);
        setStream(baos, response);
        messagingTemplate.convertAndSend("/topic/data-conversion/status", "end");
    }

    private void setStream(ByteArrayOutputStream baos, HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Length", Integer.valueOf(baos.size()).toString());
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(baos.toByteArray());
        outputStream.close();
        response.flushBuffer();
    }
}
