package org.fhi360.lamis.modules.reporting.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.fhi360.lamis.modules.reporting.service.converter.RunningPortService;
import org.lamisplus.modules.base.config.ContextProvider;
import org.lamisplus.modules.base.service.PrinceXMLService;
import org.lamisplus.modules.lamis.legacy.domain.entities.Patient;
import org.lamisplus.modules.lamis.legacy.service.PatientCurrentStatusService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentReportsService {
    public enum Appointment {
        MISSED_REFILL, MISSED_CLINIC, MISSED_APPOINTMENT, SCHEDULED_REFILL, SCHEDULED_CLINIC, SCHEDULED_VISIT
    }

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ITemplateEngine templateEngine;
    private final PrinceXMLService princeXMLService;
    private final RunningPortService runningPortService;

    public ByteArrayOutputStream process(LocalDate start, LocalDate end, Long facilityId, Appointment type) {
        switch (type) {
            case MISSED_CLINIC:
                return missedClinicAppointment(facilityId, start, end);
            case MISSED_REFILL:
                return missedRefillAppointment(facilityId, start, end);
            case MISSED_APPOINTMENT:
                return missedAppointment(facilityId, start, end);
            case SCHEDULED_VISIT:
                return scheduledAppointment(facilityId, start, end);
            case SCHEDULED_CLINIC:
                return scheduledClinicAppointment(facilityId, start, end);
            case SCHEDULED_REFILL:
                return scheduledRefillAppointment(facilityId, start, end);
            default:
                return new ByteArrayOutputStream();
        }
    }

    @SneakyThrows
    private ByteArrayOutputStream scheduledRefillAppointment(Long facilityId, LocalDate start, LocalDate end) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String query = "" +
                "SELECT DISTINCT * FROM (" +
                "   SELECT * FROM (" +
                "       SELECT p.id patient_id, hospital_num, trim(from coalesce(other_names, '') || ' ' || coalesce(surname, '')) as name, " +
                "           datediff('year', date_birth, current_date) age, cast(p.extra->>'art' as boolean) art " +
                "           (case gender when 'MALE' then 'Male' else 'Female' end) gender, date_birth, status_at_registration status_registration, " +
                "           address, phone, date_started, (SELECT date_visit date_last_clinic FROM pharmacy WHERE patient_id = p.id AND date_visit <= :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1)," +
                "           (SELECT next_appointment FROM pharmacy WHERE patient_id = p.id AND date_visit <= :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1), " +
                "           (SELECT status FROM status_history WHERE patient_id = p.id AND date_status <= :end AND " +
                "           archived = false ORDER BY date_status DESC LIMIT 1), (select name from case_manager where id = case_manager_id) case_manager " +
                "       FROM patient p WHERE p.facility_id = :facility AND p.archived = false " +
                "   ) ph WHERE ph.next_appointment BETWEEN :start AND :end " +
                ") as pl WHERE status NOT IN ('KNOWN_DEATH', 'STOPPED_TREATMENT', 'ART_TRANSFER_OUT') and art = true" +
                "   ORDER BY case_manager, next_appointment, name";

        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("facility", facilityId)
                .addValue("start", start)
                .addValue("end", end);
        List<Map<String, Object>> list = namedParameterJdbcTemplate.queryForList(query, namedParameters);
        list = getPatientCurrentStatus(list, null);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", "Client Scheduled Refill Appointments");
        parameters.put("from", convertToDateViaInstant(start));
        parameters.put("to", convertToDateViaInstant(end));

        Context context = new Context();
        context.setVariables(parameters);
        context.setVariables(facilityInfo(facilityId));
        context.setVariable("datasource", list);
        context.setVariable("css", "http://localhost:" + runningPortService.getPort() + "/across/resources/static/reporting/css/style.css");
        String output = templateEngine.process("templates/appointment_report", context);
        princeXMLService.convert(IOUtils.toInputStream(output), baos);
        return baos;
    }

    @SneakyThrows
    private ByteArrayOutputStream scheduledClinicAppointment(Long facilityId, LocalDate start, LocalDate end) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String query = "" +
                "SELECT DISTINCT * FROM (" +
                "   SELECT * FROM (" +
                "       SELECT p.id patient_id, hospital_num, trim(from coalesce(other_names, '') || ' ' || coalesce(surname, '')) as name, " +
                "           datediff('year', date_birth, current_date) age, cast(p.extra->>'art' as boolean) art " +
                "           (case gender when 'MALE' then 'Male' else 'Female' end) gender, date_birth, status_at_registration status_registration, " +
                "           address, phone, date_started, (SELECT date_visit date_last_clinic FROM clinic WHERE patient_id = p.id AND date_visit <= :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1)," +
                "           (SELECT next_appointment FROM clinic WHERE patient_id = p.id AND date_visit <= :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1), " +
                "           (SELECT status FROM status_history WHERE patient_id = p.id AND date_status <= :end AND " +
                "           archived = false ORDER BY date_status DESC LIMIT 1),(select name from case_manager where id = case_manager_id) case_manager " +
                "       FROM patient p WHERE p.facility_id = :facility AND p.archived = false" +
                "   ) c WHERE next_appointment BETWEEN :start AND :end " +
                ") as pl WHERE status NOT IN ('KNOWN_DEATH', 'STOPPED_TREATMENT', 'ART_TRANSFER_OUT') and art = true" +
                "   ORDER BY case_manager, next_appointment, name";

        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("facility", facilityId)
                .addValue("start", start)
                .addValue("end", end);
        List<Map<String, Object>> list = namedParameterJdbcTemplate.queryForList(query, namedParameters);
        list = getPatientCurrentStatus(list, null);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", "Client Scheduled Clinic Appointments");
        parameters.put("from", convertToDateViaInstant(start));
        parameters.put("to", convertToDateViaInstant(end));

        Context context = new Context();
        context.setVariables(parameters);
        context.setVariable("datasource", list);
        context.setVariables(facilityInfo(facilityId));
        context.setVariable("css", "http://localhost:" + runningPortService.getPort() + "/across/resources/static/reporting/css/style.css");
        String output = templateEngine.process("templates/appointment_report", context);
        princeXMLService.convert(IOUtils.toInputStream(output), baos);
        return baos;
    }

    @SneakyThrows
    private ByteArrayOutputStream scheduledAppointment(Long facilityId, LocalDate start, LocalDate end) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String query = "" +
                "SELECT DISTINCT * FROM (" +
                "   SELECT * FROM (" +
                "       SELECT datediff('year', date_birth, current_date) age, p.id patient_id, hospital_num, trim(from coalesce(other_names, '') || ' ' || coalesce(surname, '')) as name," +
                "           (case gender when 'MALE' then 'Male' else 'Female' end) gender, date_birth, status_at_registration status_registration, " +
                "           address, phone, date_started, cast(p.extra->>'art' as boolean) art, (SELECT date_visit date_last_clinic FROM pharmacy WHERE patient_id = p.id AND date_visit <= :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1), " +
                "           (SELECT next_appointment FROM pharmacy WHERE patient_id = p.id AND date_visit <= :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1) next_appointment, " +
                "           (SELECT status FROM status_history WHERE patient_id = p.id AND date_status <= :end AND " +
                "           archived = false ORDER BY date_status DESC LIMIT 1),(select name from case_manager where id = case_manager_id) case_manager " +
                "       FROM patient p WHERE p.facility_id = :facility AND p.archived = false " +
                "   ) ph WHERE next_appointment BETWEEN :start AND :end " +
                "   UNION ALL " +
                "   SELECT * FROM (" +
                "       SELECT datediff('year', date_birth, current_date) age, p.id patient_id, hospital_num, trim(from coalesce(other_names, '') || ' ' || coalesce(surname, '')) as name," +
                "           (case gender when 'MALE' then 'Male' else 'Female' end) gender, date_birth, status_at_registration status_registration, " +
                "           address, phone, date_started, cast(p.extra->>'art' as boolean) art, (SELECT date_visit date_last_clinic FROM clinic WHERE patient_id = p.id AND date_visit < :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1)," +
                "           (SELECT next_appointment FROM clinic WHERE patient_id = p.id AND date_visit <= :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1), " +
                "           (SELECT status FROM status_history WHERE patient_id = p.id AND date_status <= :end AND " +
                "           archived = false ORDER BY date_status DESC LIMIT 1),(select name from case_manager where id = case_manager_id) case_manager " +
                "       FROM patient p WHERE p.facility_id = :facility AND p.archived = false " +
                "   ) c WHERE next_appointment BETWEEN :start AND :end  " +
                ") as pl WHERE status NOT IN ('KNOWN_DEATH', 'STOPPED_TREATMENT', 'ART_TRANSFER_OUT') and art = true" +
                "   ORDER BY case_manager, next_appointment, name";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("facility", facilityId)
                .addValue("start", start)
                .addValue("end", end);
        List<Map<String, Object>> list = namedParameterJdbcTemplate.queryForList(query, namedParameters);
        list = getPatientCurrentStatus(list, null);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", "Client Scheduled Appointments");
        parameters.put("from", convertToDateViaInstant(start));
        parameters.put("to", convertToDateViaInstant(end));

        Context context = new Context();
        context.setVariables(parameters);
        context.setVariable("datasource", list);
        context.setVariables(facilityInfo(facilityId));
        context.setVariable("css", "http://localhost:" + runningPortService.getPort() + "/across/resources/static/reporting/css/style.css");
        String output = templateEngine.process("templates/appointment_report", context);
        princeXMLService.convert(IOUtils.toInputStream(output), baos);
        return baos;
    }

    @SneakyThrows
    private ByteArrayOutputStream missedRefillAppointment(Long facilityId, LocalDate start, LocalDate end) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String query = "" +
                "SELECT DISTINCT * FROM (" +
                "   SELECT * FROM (" +
                "       SELECT datediff('year', date_birth, current_date) age, p.id patient_id, hospital_num, trim(from coalesce(other_names, '') || ' ' || coalesce(surname, '')) as name," +
                "           (case gender when 'MALE' then 'Male' else 'Female' end) gender, date_birth, status_at_registration status_registration, " +
                "           address, phone, date_started, cast(p.extra->>'art' as boolean) art, (SELECT date_visit date_last_clinic FROM pharmacy WHERE patient_id = p.id AND date_visit <= :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1), " +
                "           (SELECT next_appointment FROM pharmacy WHERE patient_id = p.id AND date_visit <= :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1), " +
                "           (SELECT status FROM status_history WHERE patient_id = p.id AND date_status <= :end AND " +
                "           archived = false ORDER BY date_status DESC LIMIT 1), (select name from case_manager where id = case_manager_id) case_manager " +
                "           FROM patient p WHERE p.facility_id = :facility AND p.archived = false" +
                "   ) ph WHERE ph.next_appointment BETWEEN :start AND :end " +
                ") as pl WHERE patient_id NOT IN (SELECT DISTINCT patient_id FROM pharmacy WHERE facility_id = :facility AND date_visit BETWEEN :start AND :end " +
                "   AND archived = false) AND status NOT IN ('KNOWN_DEATH', 'STOPPED_TREATMENT', 'ART_TRANSFER_OUT') and art = true" +
                "   ORDER BY case_manager, next_appointment, name";


        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("facility", facilityId)
                .addValue("start", start)
                .addValue("end", end);
        List<Map<String, Object>> list = namedParameterJdbcTemplate.queryForList(query, namedParameters);
        list = getPatientCurrentStatus(list, null);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", "Missed Refill Appointments");
        parameters.put("from", convertToDateViaInstant(start));
        parameters.put("to", convertToDateViaInstant(end));

        Context context = new Context();
        context.setVariables(parameters);
        context.setVariables(facilityInfo(facilityId));
        context.setVariable("datasource", list);
        context.setVariable("css", "http://localhost:" + runningPortService.getPort() + "/across/resources/static/reporting/css/style.css");
        String output = templateEngine.process("templates/appointment_report", context);
        princeXMLService.convert(IOUtils.toInputStream(output), baos);
        return baos;
    }

    @SneakyThrows
    private ByteArrayOutputStream missedClinicAppointment(Long facilityId, LocalDate start, LocalDate end) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String query = "" +
                "SELECT DISTINCT * FROM (" +
                "   SELECT * FROM (" +
                "       SELECT datediff('year', date_birth, current_date) age, p.id patient_id, hospital_num, trim(from coalesce(other_names, '') || ' ' || coalesce(surname, '')) as name, " +
                "           (case gender when 'MALE' then 'Male' else 'Female' end) gender, date_birth, status_at_registration status_registration, " +
                "           address, phone, date_started, cast(p.extra->>'art' as boolean) art, (SELECT date_visit date_last_clinic FROM clinic WHERE patient_id = p.id AND date_visit <= :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1), " +
                "           (SELECT next_appointment FROM clinic WHERE patient_id = p.id AND date_visit <= :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1), " +
                "           (SELECT status FROM status_history WHERE patient_id = p.id AND date_status <= :end AND " +
                "           archived = false ORDER BY date_status DESC LIMIT 1),(select name from case_manager where id = case_manager_id) case_manager " +
                "       FROM patient p WHERE p.facility_id = :facility AND p.archived = false " +
                "   ) c WHERE next_appointment BETWEEN :start AND :end " +
                ") as pl WHERE patient_id NOT IN (SELECT DISTINCT patient_id FROM clinic WHERE facility_id = :facility AND date_visit BETWEEN :start AND :end " +
                "   AND archived = false) AND status NOT IN ('KNOWN_DEATH', 'STOPPED_TREATMENT', 'ART_TRANSFER_OUT') and art = true" +
                "   ORDER BY case_manager, next_appointment, name";

        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("facility", facilityId)
                .addValue("start", start)
                .addValue("end", end);
        List<Map<String, Object>> list = namedParameterJdbcTemplate.queryForList(query, namedParameters);
        list = getPatientCurrentStatus(list, null);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", "Missed Clinic Appointments");
        parameters.put("from", convertToDateViaInstant(start));
        parameters.put("to", convertToDateViaInstant(end));

        Context context = new Context();
        context.setVariables(parameters);
        context.setVariables(facilityInfo(facilityId));
        context.setVariable("datasource", list);
        context.setVariable("css", "http://localhost:" + runningPortService.getPort() + "/across/resources/static/reporting/css/style.css");
        String output = templateEngine.process("templates/appointment_report", context);
        princeXMLService.convert(IOUtils.toInputStream(output), baos);
        return baos;
    }

    @SneakyThrows
    private ByteArrayOutputStream missedAppointment(Long facilityId, LocalDate start, LocalDate end) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String query = "SELECT DISTINCT * FROM (" +
                "   SELECT * FROM (" +
                "       SELECT datediff('year', date_birth, current_date) age, p.id patient_id, hospital_num, trim(from coalesce(other_names, '') || ' ' || coalesce(surname, '')) as name," +
                "           (case gender when 'MALE' then 'Male' else 'Female' end)  gender, date_birth, status_at_registration status_registration, " +
                "           address, phone, date_started, cast(p.extra->>'art' as boolean) art, (SELECT date_visit date_last_clinic FROM clinic WHERE patient_id = p.id AND date_visit <= :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1)," +
                "           (SELECT next_appointment FROM clinic WHERE patient_id = p.id AND date_visit <= :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1), " +
                "           (SELECT status FROM status_history WHERE patient_id = p.id AND date_status <= :end AND " +
                "           archived = false ORDER BY date_status DESC LIMIT 1),(select name from case_manager where id = case_manager_id) case_manager " +
                "       FROM patient p WHERE p.facility_id = :facility AND p.archived = false " +
                "   ) ph WHERE ph.next_appointment BETWEEN :start AND :end " +
                "   UNION ALL " +
                "   SELECT * FROM (" +
                "       SELECT datediff('year', date_birth, current_date) age, p.id patient_id, hospital_num, trim(from coalesce(other_names, '') || ' ' || coalesce(surname, '')) as name," +
                "           (case gender when 'MALE' then 'Male' else 'Female' end) gender, date_birth, status_at_registration status_registration, " +
                "           address, phone, date_started, cast(p.extra->>'art' as boolean) art, (SELECT date_visit date_last_clinic FROM pharmacy WHERE patient_id = p.id AND date_visit < :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1), " +
                "           (SELECT next_appointment FROM pharmacy WHERE patient_id = p.id AND date_visit <= :end AND " +
                "           archived = false ORDER BY date_visit DESC LIMIT 1), " +
                "           (SELECT status FROM status_history WHERE patient_id = p.id AND date_status <= :end AND " +
                "           archived = false ORDER BY date_status DESC LIMIT 1),(select name from case_manager where id = case_manager_id) case_manager " +
                "       FROM patient p WHERE p.facility_id = :facility AND p.archived = false" +
                "   ) c WHERE next_appointment BETWEEN :start AND :end " +
                ") as pl WHERE patient_id NOT IN (SELECT DISTINCT patient_id FROM clinic WHERE facility_id = :facility AND date_visit BETWEEN :start AND :end " +
                "   AND archived = false union SELECT DISTINCT patient_id FROM pharmacy WHERE facility_id = :facility AND date_visit BETWEEN :start AND :end " +
                "   AND archived = false) AND status NOT IN ('KNOWN_DEATH', 'STOPPED_TREATMENT', 'ART_TRANSFER_OUT') and art = true" +
                "   ORDER BY case_manager, next_appointment, name";

        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("facility", facilityId)
                .addValue("start", start)
                .addValue("end", end);
        List<Map<String, Object>> list = namedParameterJdbcTemplate.queryForList(query, namedParameters);
        list = getPatientCurrentStatus(list, null);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", "Missed Scheduled Appointments");
        parameters.put("from", convertToDateViaInstant(start));
        parameters.put("to", convertToDateViaInstant(end));

        Context context = new Context();
        context.setVariables(parameters);
        context.setVariables(facilityInfo(facilityId));
        context.setVariable("datasource", list);
        context.setVariable("css", "http://localhost:" + runningPortService.getPort() + "/across/resources/static/reporting/css/style.css");
        String output = templateEngine.process("templates/appointment_report", context);
        princeXMLService.convert(IOUtils.toInputStream(output), baos);
        return baos;
    }

    public static List<Map<String, Object>> getPatientCurrentStatus(List<Map<String, Object>> list, String idKeyName) {
        if (idKeyName == null) {
            idKeyName = "patient_id";
        }
        String finalIdKeyName = idKeyName;
        return list.stream()
                .map(d -> {
                    Patient patient = new Patient();
                    patient.setId(Long.valueOf(d.get(finalIdKeyName).toString()));
                    try {
                        d.put("status", ContextProvider.getBean(PatientCurrentStatusService.class).getStatus(patient).getStatus());
                    } catch (Exception e) {
                        d.put("status", "HIV+ non ART");
                    }
                    return d;
                }).collect(Collectors.toList());
    }

    public static Map<String, Object> facilityInfo(Long facilityId) {
        String query = "select f.name as facility, s.name as state, l.name as lga from "
                + "facility f inner join lga l on f.lga_id = l.id inner join state s "
                + "on f.state_id = s.id where f.id = ?";
        Map<String, Object> parameters = new HashMap<>();
        ContextProvider.getBean(JdbcTemplate.class).query(query, rs -> {
            while (rs.next()) {
                parameters.put("facility", rs.getString("facility"));
                parameters.put("state", rs.getString("state"));
                parameters.put("lga", rs.getString("lga"));
            }
            return null;
        }, facilityId);
        return parameters;
    }

    public static Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
