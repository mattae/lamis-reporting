package org.fhi360.lamis.modules.reporting.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.fhi360.lamis.modules.reporting.service.converter.RunningPortService;
import org.lamisplus.modules.base.service.PrinceXMLService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.fhi360.lamis.modules.reporting.service.AppointmentReportsService.facilityInfo;
import static org.fhi360.lamis.modules.reporting.service.AppointmentReportsService.getPatientCurrentStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class DevolveReportService {
    private final JdbcTemplate jdbcTemplate;
    private final PrinceXMLService princeXMLService;
    private final ITemplateEngine templateEngine;
    private final RunningPortService runningPortService;

    @SneakyThrows
    public ByteArrayOutputStream cparpDevolveReport(Long facilityId) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        List<Map<String, Object>> cps = jdbcTemplate.queryForList("select id, name, pin from community_pharmacy c " +
                "where archived = false and active = true and c.lga_id in (select id from lga where state_id = (select " +
                "state_id from facility where id = ?)) order by name", facilityId);
        cps = cps.stream()
                .map(cp -> {
                    cp.put("devolves", new ArrayList<>());
                    Long id = (Long) cp.get("id");
                    List<Map<String, Object>> devolves =
                            jdbcTemplate.queryForList(
                                    "select * from ( " +
                                            "with active_devolves(hospital_num, patient_id, date_devolved, dmoc_type, " +
                                            "   name, address, phone, gender, age,status) as (" +
                                            "       select hospital_num, patient_id, date_devolved, dmoc_type, " +
                                            "       trim(from coalesce(other_names, '') || ' ' || coalesce(surname, '')), address, phone, " +
                                            "       (case gender when 'MALE' then 'Male' else 'Female' end) gender, " +
                                            "       datediff('year', date_birth, current_date) age," +
                                            "       (select status from status_history where patient_id = d.patient_id and archived = false" +
                                            "       order by date_status desc limit 1) from devolve d join patient p on p.id = patient_id " +
                                            "       where d.community_pharmacy_id = ? and d.archived = false and p.archived = false and date_devolved <= current_date " +
                                            "       and date_returned_to_facility is null" +
                                            ") " +
                                            "select hospital_num,patient_id, name, gender, age, address, phone, dmoc_type, status, rank() " +
                                            "   over(partition by patient_id order by date_devolved desc) _rank, " +
                                            "   max(date_devolved) over(partition by patient_id order by date_devolved desc) date_devolved from active_devolves" +
                                            ") devolves where _rank = 1 and status not in ('KNOWN_DEATH', 'ART_TRANSFER_OUT') order by name", id);

                    devolves = getPatientCurrentStatus(devolves, null);

                    cp.put("devolves", devolves);
                    cp.put("total", devolves.size());
                    return cp;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", "CPARP Devolvement");

        Context context = new Context();
        context.setVariables(parameters);
        context.setVariables(facilityInfo(facilityId));
        context.setVariable("css", "http://localhost:" + runningPortService.getPort() + "/across/resources/static/reporting/css/style.css");
        context.setVariable("datasource", cps);
        String output = templateEngine.process("templates/cparp_devolve_report", context);
        princeXMLService.convert(IOUtils.toInputStream(output), baos);
        return baos;
    }

    @SneakyThrows
    public ByteArrayOutputStream devolveReport(Long facilityId) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        List<Map<String, Object>> types = jdbcTemplate.queryForList("select distinct dmoc_type from devolve where " +
                "facility_id = ? and dmoc_type is not null and archived = false order by 1", facilityId);

        types = types.stream()
                .map(t -> {
                    String type = (String) t.get("dmoc_type");
                    List<Map<String, Object>> devolves =
                            jdbcTemplate.queryForList(
                                    "select * from ( " +
                                            "with active_devolves(hospital_num, patient_id, date_devolved, dmoc_type, name, " +
                                            "   address, phone, gender, age,status) as (" +
                                            "       select hospital_num, patient_id, date_devolved, dmoc_type, " +
                                            "       trim(from coalesce(other_names, '') || ' ' || coalesce(surname, '')), address, phone, " +
                                            "       (case gender when 'MALE' then 'Male' else 'Female' end) gender, " +
                                            "       datediff('year', date_birth, current_date) age," +
                                            "       (select status from status_history where patient_id = d.patient_id and archived = false" +
                                            "       order by date_status desc limit 1) from devolve d join patient p on p.id = patient_id " +
                                            "       where d.archived = false and p.archived = false and date_devolved <= current_date " +
                                            "       and date_returned_to_facility is null and dmoc_type = ? and d.facility_id = ?" +
                                            ") " +
                                            "select hospital_num, patient_id, name, gender, age, address, phone, dmoc_type, status, rank() " +
                                            "   over(partition by patient_id order by date_devolved desc) _rank, " +
                                            "   max(date_devolved) over(partition by patient_id order by date_devolved desc) date_devolved from active_devolves" +
                                            ") devolves where _rank = 1 and status not in ('KNOWN_DEATH', 'ART_TRANSFER_OUT') order by name", type, facilityId);
                    devolves = getPatientCurrentStatus(devolves, null);

                    type = StringUtils.equals(type, "F_CARG") ? "F-CARG" : StringUtils.equals(type, "FAST_TRACK") ? "Fast Track" :
                            StringUtils.equals(type, "S_CARG") ? "S-CARG" : StringUtils.equals(type, "ARC") ? "Adolescent Refill Club" : type;
                    t.put("devolves", devolves);
                    t.put("total", devolves.size());
                    t.put("type", type);
                    return t;
                }).collect(Collectors.toList());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", "Client Devolvement Report");

        Context context = new Context();
        context.setVariables(parameters);
        context.setVariables(facilityInfo(facilityId));
        context.setVariable("css", "http://localhost:" + runningPortService.getPort() + "/across/resources/static/reporting/css/style.css");
        context.setVariable("datasource", types);
        String output = templateEngine.process("templates/devolve_report", context);
        princeXMLService.convert(IOUtils.toInputStream(output), baos);
        return baos;
    }
}
