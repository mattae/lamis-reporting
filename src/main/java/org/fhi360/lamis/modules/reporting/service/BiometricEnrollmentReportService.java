package org.fhi360.lamis.modules.reporting.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.fhi360.lamis.modules.reporting.service.converter.RunningPortService;
import org.lamisplus.modules.base.config.ContextProvider;
import org.lamisplus.modules.base.service.PrinceXMLService;
import org.lamisplus.modules.lamis.legacy.domain.entities.Patient;
import org.lamisplus.modules.lamis.legacy.service.PatientCurrentStatusService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;

import static org.fhi360.lamis.modules.reporting.service.AppointmentReportsService.*;

@Service
@RequiredArgsConstructor
public class BiometricEnrollmentReportService {
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");
    private final JdbcTemplate jdbcTemplate;
    private final ITemplateEngine templateEngine;
    private final PrinceXMLService princeXMLService;
    private final RunningPortService runningPortService;

    public ByteArrayOutputStream fingerprintEnrollmentReport(Long facilityId, LocalDate start, LocalDate end) {
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

            Context context = new Context();
            context.setVariables(facilityInfo(facilityId));
            context.setVariables(parameters);
            context.setVariable("today", new Date());
            context.setVariable("datasource", dataSource);
            context.setVariable("css", "http://localhost:" + runningPortService.getPort() + "/across/resources/static/reporting/css/style.css");
            String output = templateEngine.process("templates/biometric_report", context);
            princeXMLService.convert(IOUtils.toInputStream(output), baos);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos;
    }

}
