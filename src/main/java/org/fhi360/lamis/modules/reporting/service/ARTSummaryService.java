package org.fhi360.lamis.modules.reporting.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ARTSummaryService {
    private final JdbcTemplate jdbcTemplate;

    @SneakyThrows
    public ByteArrayOutputStream build(Long facilityId, LocalDate reportingPeriod, boolean today) {
        LocalDate start = reportingPeriod.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = reportingPeriod.with(TemporalAdjusters.lastDayOfMonth());
        if (today) {
            end = LocalDate.now();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();

        Map<String, Object> parameters = new HashMap<>(headers(facilityId, reportingPeriod));

        parameters.putAll(art1(facilityId, start, end));
        parameters.putAll(art2(facilityId, start, end));
        parameters.putAll(art3(facilityId, start, end));
        parameters.putAll(art4(facilityId, start, end));
        parameters.putAll(art5(facilityId, start, end));
        parameters.putAll(art6(facilityId, start, end));
        parameters.putAll(art7(facilityId, start, end));
        parameters.putAll(art8(facilityId, start, end));

        JasperPrint jasperPrint = JasperFillManager.fillReport(new ClassPathResource("jasperTemplates/art_summary.jasper").getInputStream(), parameters);
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

    private Map<String, String> art1(Long facilityId, LocalDate start, LocalDate end) {

        final int[] tbMale = {0};
        final int[] tbFemale = {0};
        final int[] pregnant = {0};
        final int[] breastfeeding = {0};
        final Map<String, String>[] params = new Map[]{initParams()};
        String query = "SELECT * FROM (" +
                "SELECT DISTINCT gender, DATEDIFF('YEAR', date_birth, ?) AS age, " +
                "pregnant, breastfeeding, tb_status, status_at_registration FROM patient P WHERE facility_id = ? AND " +
                "date_registration BETWEEN ? AND ? and archived = false) as sel " +
                "WHERE status_at_registration NOT IN ('ART_TRANSFER_IN') AND cast(extra->>art' as boolean) = true";
        jdbcTemplate.query(query, resultSet -> {
            while (resultSet.next()) {
                String gender = resultSet.getString("gender");
                int age = resultSet.getInt("age");
                boolean p = resultSet.getBoolean("pregnant");
                boolean bf = resultSet.getBoolean("breastfeeding");
                String tbStatus = StringUtils.trimToEmpty(resultSet.getString("tb_status"));

                params[0] = disaggregate(gender, age, params[0]);
                if (gender.trim().equalsIgnoreCase("Male")) {
                    //check for TB status during enrollmemnt
                    if (tbStatus.equalsIgnoreCase("Currently on TB treatment") || tbStatus.equalsIgnoreCase("TB positive not on TB drugs")) {
                        tbMale[0]++;
                    }
                } else {
                    //check if client is pregnant or breast breastfeeding during enrolment
                    if (p) {
                        pregnant[0]++;
                    } else {
                        if (bf) {
                            breastfeeding[0]++;
                        }
                    }
                    //check for TB status during enrollmemnt
                    if (tbStatus.equalsIgnoreCase("Currently on TB treatment") || tbStatus.equalsIgnoreCase("TB positive not on TB drugs")) {
                        tbFemale[0]++;
                    }
                }
            }
            return null;
        }, end, facilityId, start, end);

        //Populate the report parameter map with values computed for ART 1
        params[0].put("art1m1", params[0].get("maleU1"));
        params[0].put("art1f1", params[0].get("femaleU1"));
        params[0].put("art1t1", Integer.toString(Integer.parseInt(params[0].get("maleU1")) + Integer.parseInt(params[0].get("femaleU1"))));

        params[0].put("art1m2", params[0].get("maleU5"));
        params[0].put("art1f2", params[0].get("femaleU1"));
        params[0].put("art1t2", Integer.toString(Integer.parseInt(params[0].get("maleU5")) + Integer.parseInt(params[0].get("femaleU5"))));

        params[0].put("art1m3", params[0].get("maleU10"));
        params[0].put("art1f3", params[0].get("maleU10"));
        params[0].put("art1t3", Integer.toString(Integer.parseInt(params[0].get("maleU10")) + Integer.parseInt(params[0].get("femaleU10"))));

        params[0].put("art1m4", params[0].get("maleU15"));
        params[0].put("art1f4", params[0].get("maleU15"));
        params[0].put("art1t4", Integer.toString(Integer.parseInt(params[0].get("maleU15")) + Integer.parseInt(params[0].get("femaleU15"))));

        params[0].put("art1m5", params[0].get("maleU20"));
        params[0].put("art1f5", params[0].get("maleU20"));
        params[0].put("art1t5", Integer.toString(Integer.parseInt(params[0].get("maleU20")) + Integer.parseInt(params[0].get("femaleU20"))));

        params[0].put("art1m6", params[0].get("maleU25"));
        params[0].put("art1f6", params[0].get("femaleU25"));
        params[0].put("art1t6", Integer.toString(Integer.parseInt(params[0].get("maleU25")) + Integer.parseInt(params[0].get("femaleU25"))));

        params[0].put("art1m7", params[0].get("maleU49"));
        params[0].put("art1f7", params[0].get("femaleU49"));
        params[0].put("art1t7", Integer.toString(Integer.parseInt(params[0].get("maleU49")) + Integer.parseInt(params[0].get("femaleU49"))));

        params[0].put("art1m8", params[0].get("maleO49"));
        params[0].put("art1f8", params[0].get("femaleO49"));
        params[0].put("art1t8", Integer.toString(Integer.parseInt(params[0].get("maleO49")) + Integer.parseInt(params[0].get("femaleO49"))));

        params[0].put("art1f9", Integer.toString(pregnant[0]));
        params[0].put("art1t9", Integer.toString(pregnant[0]));

        params[0].put("art1f10", Integer.toString(breastfeeding[0]));
        params[0].put("art1t10", Integer.toString(breastfeeding[0]));

        params[0].put("art1m11", Integer.toString(tbMale[0]));
        params[0].put("art1f11", Integer.toString(tbFemale[0]));
        params[0].put("art1t11", Integer.toString(tbMale[0] + tbFemale[0]));

        return params[0];
    }

    public Map<String, String> art2(Long facilityId, LocalDate start, LocalDate end) {
        final Map<String, String>[] params = new Map[]{initParams()};
        final int[] preg = {0};
        final int[] feeding = {0};
        String query = "SELECT * FROM (" +
                "SELECT DISTINCT id, gender, DATEDIFF('YEAR', date_birth, ?) AS age, date_started,(select status FROM status_history " +
                "WHERE patient_id = p.id AND date_status <= ? AND archived = false ORDER BY date_status DESC LIMIT 1)," +
                "(select date_status FROM status_history WHERE patient_id = p.id AND date_status <= ? AND archived = false " +
                " ORDER BY date_status DESC LIMIT 1) FROM " +
                "patient p WHERE facility_id = ? AND date_registration <= ?  AND archived = false" +
                ") as selection " +
                "WHERE (status IN ('ART_START', 'ART_RESTART', 'ART_TRANSFER_IN') " +
                "OR (status IN ('ART_TRANSFER_OUT', 'LOST_TO_FOLLOWUP','STOPPED_TREATMENT', 'KNOWN_DEATH') AND date_status > ?))";
        jdbcTemplate.query(query, resultSet -> {
            while (resultSet.next()) {
                long patientId = resultSet.getLong("id");
                String gender = resultSet.getString("gender");
                int age = resultSet.getInt("age");
                Date dateStarted = resultSet.getDate("date_started");

                boolean[] pregnant = {false};
                boolean[] breastfeeding = {false};

                String query1 = "SELECT patient_id  FROM clinic WHERE patient_id = ? AND date_visit BETWEEN "
                        + "? AND ? AND clinic_stage IS NOT NULL OR clinic_stage != '' and archived = false";
                boolean[] exist = {jdbcTemplate.query(query1, rs -> true, patientId, start, end)};
                if (!exist[0]) {
                    query1 = "SELECT patient_id FROM laboratory, jsonb_array_elements(lines) with ordinality a(l) WHERE patient_id = ? AND " +
                            "date_result_received BETWEEN ? AND ? AND (jsonb_extract_path_text(l,'lab_test_id'))\\:\\:int = 16 and archived = false " +
                            "ORDER BY date_result_received DESC LIMIT 1";
                    exist[0] = jdbcTemplate.query(query1, rs -> true, patientId, start, end);
                }
                if (!exist[0]) {
                    if (dateStarted != null) {
                        String query3 = "select date_visit, date_visit + (jsonb_extract_path_text(l,'duration'))\\:\\:int + INTERVAL '28 DAYS' < ? as ltfu, r.description regimen, " +
                                "t.description as regimen_type from pharmacy p, jsonb_array_elements(lines) with ordinality a(l) " +
                                "join regimen r on r.id = (jsonb_extract_path_text(l,'regimen_id'))\\:\\:int " +
                                "join regimen_type t on t.id = r.regimen_type_id where r.regimen_type_id in (1,2,3,4,14) and p.patient_id = ? and " +
                                "date_visit <= ? and p.archived = false ORDER BY p.date_visit DESC, duration DESC LIMIT 1";
                        jdbcTemplate.query(query3, rs -> {
                            while (rs.next()) {
                                if (!rs.getBoolean("ltfu")) {
                                    exist[0] = true;
                                }
                            }
                            return null;
                        });
                    }
                }

                if (exist[0]) {
                    params[0] = disaggregate(gender, age, params[0]);
                    if (gender.trim().equalsIgnoreCase("Female")) {
                        //check if client is pregnant or breast feeding during enrolment
                        String query2 = "SELECT pregnant, breastfeeding FROM clinic WHERE patient_id = ? AND date_visit between "
                                + "? + INTERVAL '-9 MONTHS' AND ? AND archived = false ORDER BY date_visit DESC LIMIT 1";
                        jdbcTemplate.query(query2, rs -> {
                            while (rs.next()) {
                                pregnant[0] = rs.getBoolean("pregnant");
                                breastfeeding[0] = rs.getBoolean("breastfeeding");
                            }
                            return null;
                        }, patientId, start, end);
                        if (pregnant[0]) {
                            preg[0]++;
                        } else {
                            if (breastfeeding[0]) {
                                feeding[0]++;
                            }
                        }
                    }
                }
            }
            return null;
        }, end, end, end, facilityId, end, end);

        params[0].put("art2m1", params[0].get("maleU1"));
        params[0].put("art2f1", params[0].get("femaleU1"));
        params[0].put("art2t1", Integer.toString(Integer.parseInt(params[0].get("maleU1")) + Integer.parseInt(params[0].get("femaleU1"))));

        params[0].put("art2m2", params[0].get("maleU5"));
        params[0].put("art2f2", params[0].get("femaleU1"));
        params[0].put("art2t2", Integer.toString(Integer.parseInt(params[0].get("maleU5")) + Integer.parseInt(params[0].get("femaleU5"))));

        params[0].put("art2m3", params[0].get("maleU10"));
        params[0].put("art2f3", params[0].get("maleU10"));
        params[0].put("art2t3", Integer.toString(Integer.parseInt(params[0].get("maleU10")) + Integer.parseInt(params[0].get("femaleU10"))));

        params[0].put("art2m4", params[0].get("maleU15"));
        params[0].put("art2f4", params[0].get("maleU15"));
        params[0].put("art2t4", Integer.toString(Integer.parseInt(params[0].get("maleU15")) + Integer.parseInt(params[0].get("femaleU15"))));

        params[0].put("art2m5", params[0].get("maleU20"));
        params[0].put("art2f5", params[0].get("maleU20"));
        params[0].put("art2t5", Integer.toString(Integer.parseInt(params[0].get("maleU20")) + Integer.parseInt(params[0].get("femaleU20"))));

        params[0].put("art2m6", params[0].get("maleU25"));
        params[0].put("art2f6", params[0].get("femaleU25"));
        params[0].put("art2t6", Integer.toString(Integer.parseInt(params[0].get("maleU25")) + Integer.parseInt(params[0].get("femaleU25"))));

        params[0].put("art2m7", params[0].get("maleU49"));
        params[0].put("art2f7", params[0].get("femaleU49"));
        params[0].put("art2t7", Integer.toString(Integer.parseInt(params[0].get("maleU49")) + Integer.parseInt(params[0].get("femaleU49"))));

        params[0].put("art2m8", params[0].get("maleO49"));
        params[0].put("art2f8", params[0].get("femaleO49"));
        params[0].put("art2t8", Integer.toString(Integer.parseInt(params[0].get("maleO49")) + Integer.parseInt(params[0].get("femaleO49"))));

        params[0].put("art2f9", Integer.toString(preg[0]));
        params[0].put("art2t9", Integer.toString(preg[0]));
        params[0].put("art2f10", Integer.toString(feeding[0]));
        params[0].put("art2t10", Integer.toString(feeding[0]));

        return params[0];
    }

    public Map<String, String> art3(Long facilityId, LocalDate start, LocalDate end) {
        final Map<String, String>[] params = new Map[]{initParams()};
        int[] pregnant = {0};
        int[] breastfeeding = {0};
        String query = "SELECT DISTINCT p.id, gender, DATEDIFF('YEAR', date_birth, ?) AS age FROM patient p " +
                "JOIN pharmacy ph ON p.id = patient_id, jsonb_array_elements(ph.lines) with ordinality a(l) WHERE " +
                "p.facility_id = ? AND status_at_registration NOT IN ('ART_TRANSFER_IN) AND " +
                "cast(extra->>'art' as boolean) = true and p.archived = false " +
                "AND ph.archived = false AND  (jsonb_extract_path_text(l,'regimen_type_id'))\\:\\:int IN (1, 2, 3, 4, 14)";
        jdbcTemplate.query(query, resultSet -> {
            while (resultSet.next()) {
                long patientId = resultSet.getLong("id");
                String gender = resultSet.getString("gender");
                int age = resultSet.getInt("age");

                params[0] = disaggregate(gender, age, params[0]);

                if (gender.trim().equalsIgnoreCase("Female")) {
                    //check if client is pregnant or breast feeding during visit
                    String query1 = "SELECT pregnant, breastfeeding FROM clinic WHERE patient_id = ? AND archived = false  ORDER BY date_visit DESC LIMIT 1";
                    jdbcTemplate.query(query1, rs -> {
                        while (rs.next()) {
                            if (rs.getBoolean("pregnant")) {
                                pregnant[0]++;
                            }
                            if (rs.getBoolean("breastfeeding")) {
                                breastfeeding[0]++;
                            }
                        }
                        return null;
                    }, patientId);
                }
            }
            return null;
        }, end, facilityId, start, end);

        params[0].put("art3m1", params[0].get("maleU1"));
        params[0].put("art3f1", params[0].get("femaleU1"));
        params[0].put("art3t1", Integer.toString(Integer.parseInt(params[0].get("maleU1")) + Integer.parseInt(params[0].get("femaleU1"))));

        params[0].put("art3m2", params[0].get("maleU5"));
        params[0].put("art3f2", params[0].get("femaleU1"));
        params[0].put("art3t2", Integer.toString(Integer.parseInt(params[0].get("maleU5")) + Integer.parseInt(params[0].get("femaleU5"))));

        params[0].put("art3m3", params[0].get("maleU10"));
        params[0].put("art3f3", params[0].get("maleU10"));
        params[0].put("art3t3", Integer.toString(Integer.parseInt(params[0].get("maleU10")) + Integer.parseInt(params[0].get("femaleU10"))));

        params[0].put("art3m4", params[0].get("maleU15"));
        params[0].put("art3f4", params[0].get("maleU15"));
        params[0].put("art3t4", Integer.toString(Integer.parseInt(params[0].get("maleU15")) + Integer.parseInt(params[0].get("femaleU15"))));

        params[0].put("art3m5", params[0].get("maleU20"));
        params[0].put("art3f5", params[0].get("maleU20"));
        params[0].put("art3t5", Integer.toString(Integer.parseInt(params[0].get("maleU20")) + Integer.parseInt(params[0].get("femaleU20"))));

        params[0].put("art3m6", params[0].get("maleU25"));
        params[0].put("art3f6", params[0].get("femaleU25"));
        params[0].put("art3t6", Integer.toString(Integer.parseInt(params[0].get("maleU25")) + Integer.parseInt(params[0].get("femaleU25"))));

        params[0].put("art3m7", params[0].get("maleU49"));
        params[0].put("art3f7", params[0].get("femaleU49"));
        params[0].put("art3t7", Integer.toString(Integer.parseInt(params[0].get("maleU49")) + Integer.parseInt(params[0].get("femaleU49"))));

        params[0].put("art3m8", params[0].get("maleO49"));
        params[0].put("art3f8", params[0].get("femaleO49"));
        params[0].put("art3t8", Integer.toString(Integer.parseInt(params[0].get("maleO49")) + Integer.parseInt(params[0].get("femaleO49"))));

        params[0].put("art3f9", Integer.toString(pregnant[0]));
        params[0].put("art3t9", Integer.toString(pregnant[0]));
        params[0].put("art3f10", Integer.toString(breastfeeding[0]));
        params[0].put("art3t10", Integer.toString(breastfeeding[0]));

        return params[0];
    }

    public Map<String, String> art4(Long facilityId, LocalDate start, LocalDate end) {
        final Map<String, String>[] params = new Map[]{initParams()};
        int[] pregnant = {0};
        int[] breastfeeding = {0};
        final int[] agem9_1 = {0};
        final int[] agem10_1 = {0};
        final int[] agem11_1 = {0};
        final int[] agem9_2 = {0};
        final int[] agem10_2 = {0};
        final int[] agem11_2 = {0};
        final int[] agef9_1 = {0};
        final int[] agef10_1 = {0};
        final int[] agef11_1 = {0};
        final int[] agef9_2 = {0};
        final int[] agef10_2 = {0};
        final int[] agef11_2 = {0};
        //ART 4
        //Total number of people living with HIV who are currently receiving ART during the month (All regimen)

        String query = "SELECT * FROM (SELECT DISTINCT id, gender, DATEDIFF('YEAR', date_birth, ?) AS age, date_started, (select status FROM status_history " +
                "WHERE patient_id = p.id AND date_status <= ? AND archived = false ORDER BY date_status DESC LIMIT 1)," +
                "(select date_status FROM status_history WHERE patient_id = p.id AND date_status <= ? AND archived = false " +
                " ORDER BY date_status DESC LIMIT 1) FROM patient p WHERE facility_id = ?) as curr WHERE status IN ('ART_START', 'ART_RESTART', 'ART_TRANSFER_IN')" +
                " OR (status IN ('ART_TRANSFER_OUT', 'LOST_TO_FOLLOWUP', 'STOPPED_TREATMENT', 'KNOWN_DEATH') AND date_status > ? AND " +
                "date_started <= ?)";
        jdbcTemplate.query(query, resultSet -> {
            while (resultSet.next()) {
                long patientId = resultSet.getLong("id");
                String gender = resultSet.getString("gender");
                int age = resultSet.getInt("age");

                String queryx = "select date_visit, (jsonb_extract_path_text(l,'duration'))\\:\\:int duration, " +
                        "date_visit + (jsonb_extract_path_text(l,'duration'))\\:\\:int + INTERVAL '28 DAYS' < ? as ltfu, r.description regimen, " +
                        "t.id as regimen_type_id from pharmacy p, jsonb_array_elements(lines) with ordinality a(l) join " +
                        "regimen r on r.id = (jsonb_extract_path_text(l,'regimen_id'))\\:\\:int " +
                        "join regimen_type t on t.id = r.regimen_type_id where t.id in (1,2,3,4,14) and p.patient_id = ? and " +
                        "date_visit <= ? and p.archived = false ORDER BY p.date_visit DESC, duration DESC LIMIT 1";
                jdbcTemplate.query(queryx, rs -> {
                    while (rs.next()) {
                        boolean ltfu = rs.getBoolean("ltfu");

                        if (!ltfu) {
                            params[0] = disaggregate(gender, age, params[0]);

                            long regimenTypeId = rs.getLong("regimen_type_id");
                            if (gender.trim().equalsIgnoreCase("Male")) {
                                if (age < 15) {
                                    if (regimenTypeId == 1 || regimenTypeId == 3) {
                                        agem9_1[0]++;
                                    } else {
                                        if (regimenTypeId == 2 || regimenTypeId == 4) {
                                            agem10_1[0]++;
                                        } else {
                                            agem11_1[0]++;
                                        }
                                    }
                                } else {
                                    if (regimenTypeId == 1 || regimenTypeId == 3) {
                                        agem9_2[0]++;
                                    } else {
                                        if (regimenTypeId == 2 || regimenTypeId == 4) {
                                            agem10_2[0]++;
                                        } else {
                                            agem11_2[0]++;
                                        }
                                    }
                                }
                            } else {
                                if (age < 15) {
                                    if (regimenTypeId == 1 || regimenTypeId == 3) {
                                        agef9_1[0]++;
                                    } else {
                                        if (regimenTypeId == 2 || regimenTypeId == 4) {
                                            agef10_1[0]++;
                                        } else {
                                            agef11_1[0]++;
                                        }
                                    }
                                } else {
                                    if (regimenTypeId == 1 || regimenTypeId == 3) {
                                        agef9_2[0]++;
                                    } else {
                                        if (regimenTypeId == 2 || regimenTypeId == 4) {
                                            agef10_2[0]++;
                                        } else {
                                            agef11_2[0]++;
                                        }
                                    }
                                }
                            }

                            if (gender.trim().equalsIgnoreCase("Female")) {
                                //check if client is pregnant or breast feeding during visit
                                String query1 = "SELECT pregnant, breastfeeding FROM clinic WHERE patient_id = ? AND archived = false  ORDER BY date_visit DESC LIMIT 1";
                                jdbcTemplate.query(query1, rs1 -> {
                                    while (rs1.next()) {
                                        if (rs1.getBoolean("pregnant")) {
                                            pregnant[0]++;
                                        }
                                        if (rs1.getBoolean("breastfeeding")) {
                                            breastfeeding[0]++;
                                        }
                                    }
                                    return null;
                                }, patientId);
                            }
                        }
                    }
                    return null;
                }, end, patientId, end);
            }
            return null;
        }, end, end, end, facilityId, end, end);

        params[0].put("art4m1", params[0].get("maleU1"));
        params[0].put("art4f1", params[0].get("femaleU1"));
        params[0].put("art4t1", Integer.toString(Integer.parseInt(params[0].get("maleU1")) + Integer.parseInt(params[0].get("femaleU1"))));

        params[0].put("art4m2", params[0].get("maleU5"));
        params[0].put("art4f2", params[0].get("femaleU1"));
        params[0].put("art4t2", Integer.toString(Integer.parseInt(params[0].get("maleU5")) + Integer.parseInt(params[0].get("femaleU5"))));

        params[0].put("art4m3", params[0].get("maleU10"));
        params[0].put("art4f3", params[0].get("maleU10"));
        params[0].put("art4t3", Integer.toString(Integer.parseInt(params[0].get("maleU10")) + Integer.parseInt(params[0].get("femaleU10"))));

        params[0].put("art4m4", params[0].get("maleU15"));
        params[0].put("art4f4", params[0].get("maleU15"));
        params[0].put("art4t4", Integer.toString(Integer.parseInt(params[0].get("maleU15")) + Integer.parseInt(params[0].get("femaleU15"))));

        params[0].put("art4m5", params[0].get("maleU20"));
        params[0].put("art4f5", params[0].get("maleU20"));
        params[0].put("art4t5", Integer.toString(Integer.parseInt(params[0].get("maleU20")) + Integer.parseInt(params[0].get("femaleU20"))));

        params[0].put("art4m6", params[0].get("maleU25"));
        params[0].put("art4f6", params[0].get("femaleU25"));
        params[0].put("art4t6", Integer.toString(Integer.parseInt(params[0].get("maleU25")) + Integer.parseInt(params[0].get("femaleU25"))));

        params[0].put("art4m7", params[0].get("maleU49"));
        params[0].put("art4f7", params[0].get("femaleU49"));
        params[0].put("art4t7", Integer.toString(Integer.parseInt(params[0].get("maleU49")) + Integer.parseInt(params[0].get("femaleU49"))));

        params[0].put("art4m8", params[0].get("maleO49"));
        params[0].put("art4f8", params[0].get("femaleO49"));
        params[0].put("art4t8", Integer.toString(Integer.parseInt(params[0].get("maleO49")) + Integer.parseInt(params[0].get("femaleO49"))));

        params[0].put("art4m9_1", Integer.toString(agem9_1[0]));
        params[0].put("art4f9_1", Integer.toString(agef9_1[0]));
        params[0].put("art4m9_2", Integer.toString(agem9_2[0]));
        params[0].put("art4f9_2", Integer.toString(agef9_2[0]));
        params[0].put("art4t9", Integer.toString(agem9_1[0] + agef9_1[0] + agem9_2[0] + agef9_2[0]));

        params[0].put("art4m10_1", Integer.toString(agem10_1[0]));
        params[0].put("art4f10_1", Integer.toString(agef10_1[0]));
        params[0].put("art4m10_2", Integer.toString(agem10_2[0]));
        params[0].put("art4f10_2", Integer.toString(agef10_2[0]));
        params[0].put("art4t10", Integer.toString(agem10_1[0] + agef10_1[0] + agem10_2[0] + agef10_2[0]));

        params[0].put("art4m11_1", Integer.toString(agem11_1[0]));
        params[0].put("art4f11_1", Integer.toString(agef11_1[0]));
        params[0].put("art4m11_2", Integer.toString(agem11_2[0]));
        params[0].put("art4f11_2", Integer.toString(agef11_2[0]));

        params[0].put("art4f12", Integer.toString(pregnant[0]));
        params[0].put("art4t12", Integer.toString(pregnant[0]));
        params[0].put("art4f13", Integer.toString(breastfeeding[0]));
        params[0].put("art4t13", Integer.toString(breastfeeding[0]));

        return params[0];
    }

    public Map<String, String> art5(Long facilityId, LocalDate start, LocalDate end) {
        final Map<String, String>[] params = new Map[]{initParams()};
        int[] pregnant = {0};
        int[] breastfeeding = {0};

        //Number of people living with HIV and on ART with a viral load test result during the month

        String query = "SELECT DISTINCT id, gender, DATEDIFF('YEAR', date_birth, ?) AS age FROM patient WHERE " +
                "facility_id = ? AND date_registration <= ? AND date_started <= ? AND archived = false and " +
                "cast(extra->>'art' as boolean) = true ";
        jdbcTemplate.query(query, resultSet -> {
            while (resultSet.next()) {
                long patientId = resultSet.getLong("id");
                String gender = resultSet.getString("gender");
                int age = resultSet.getInt("age");

                //Check for viral load this reporting month
                String query2 = "SELECT patient_id FROM laboratory, jsonb_array_elements(lines) with ordinality a(l) WHERE " +
                        "patient_id = ? AND date_result_received BETWEEN ? AND ? AND (jsonb_extract_path_text(l,'lab_test_id'))\\:\\:int = 16 AND archived = false " +
                        "ORDER BY date_result_received DESC LIMIT 1";
                jdbcTemplate.query(query2, rs -> {
                    while (rs.next()) {
                        params[0] = disaggregate(gender, age, params[0]);
                    }
                    return null;
                }, patientId, start, end);
            }
            return null;
        }, end, facilityId, end, end);

        params[0].put("art5m1", params[0].get("maleU1"));
        params[0].put("art5f1", params[0].get("femaleU1"));
        params[0].put("art5t1", Integer.toString(Integer.parseInt(params[0].get("maleU1")) + Integer.parseInt(params[0].get("femaleU1"))));

        params[0].put("art5m2", params[0].get("maleU5"));
        params[0].put("art5f2", params[0].get("femaleU1"));
        params[0].put("art5t2", Integer.toString(Integer.parseInt(params[0].get("maleU5")) + Integer.parseInt(params[0].get("femaleU5"))));

        params[0].put("art5m3", params[0].get("maleU10"));
        params[0].put("art5f3", params[0].get("maleU10"));
        params[0].put("art5t3", Integer.toString(Integer.parseInt(params[0].get("maleU10")) + Integer.parseInt(params[0].get("femaleU10"))));

        params[0].put("art5m4", params[0].get("maleU15"));
        params[0].put("art5f4", params[0].get("maleU15"));
        params[0].put("art5t4", Integer.toString(Integer.parseInt(params[0].get("maleU15")) + Integer.parseInt(params[0].get("femaleU15"))));

        params[0].put("art5m5", params[0].get("maleU20"));
        params[0].put("art5f5", params[0].get("maleU20"));
        params[0].put("art5t5", Integer.toString(Integer.parseInt(params[0].get("maleU20")) + Integer.parseInt(params[0].get("femaleU20"))));

        params[0].put("art5m6", params[0].get("maleU25"));
        params[0].put("art5f6", params[0].get("femaleU25"));
        params[0].put("art5t6", Integer.toString(Integer.parseInt(params[0].get("maleU25")) + Integer.parseInt(params[0].get("femaleU25"))));

        params[0].put("art5m7", params[0].get("maleU49"));
        params[0].put("art5f7", params[0].get("femaleU49"));
        params[0].put("art5t7", Integer.toString(Integer.parseInt(params[0].get("maleU49")) + Integer.parseInt(params[0].get("femaleU49"))));

        params[0].put("art5m8", params[0].get("maleO49"));
        params[0].put("art5f8", params[0].get("femaleO49"));
        params[0].put("art5t8", Integer.toString(Integer.parseInt(params[0].get("maleO49")) + Integer.parseInt(params[0].get("femaleO49"))));

        return params[0];
    }

    public Map<String, String> art6(Long facilityId, LocalDate start, LocalDate end) {
        final Map<String, String>[] params = new Map[]{initParams()};
        int[] pregnant = {0};
        int[] breastfeeding = {0};

        //ART 6
        //Number of people living with HIV and on ART who are virologically suppressed (viral load < 1000 c/ml) during the month

        //executeUpdate("CREATE INDEX idx_lab ON lab(patient_id)");
        String query = "SELECT DISTINCT id, gender, DATEDIFF('YEAR', date_birth, ?) AS age FROM patient WHERE " +
                "facility_id = ? AND date_registration <= ? AND date_started <= ? AND archived = false and " +
                "cast(extra->>'art' as boolean) = true  ";
        jdbcTemplate.query(query, resultSet -> {
            while (resultSet.next()) {
                long patientId = resultSet.getLong("id");
                String gender = resultSet.getString("gender");
                int age = resultSet.getInt("age");

                //Check if the last viral load before the reporting month is less than 1000
                String query1 = "SELECT jsonb_extract_path_text(l,'result') result FROM laboratory, jsonb_array_elements(lines) with ordinality a(l) WHERE patient_id = ? AND " +
                        "date_result_received BETWEEN ? AND ? AND (jsonb_extract_path_text(l,'lab_test_id'))\\:\\:int = 16 AND archived = false " +
                        "ORDER BY date_result_received DESC LIMIT 1";
                jdbcTemplate.query(query1, rs -> {
                    while (rs.next()) {
                        String result = rs.getString("result");
                        try {
                            double value = Double.parseDouble(result);
                            if (value < 1000) {
                                params[0] = disaggregate(gender, age, params[0]);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    return null;
                }, patientId, start, end);
            }
            return null;
        }, end, facilityId, end, end);

        params[0].put("art6m1", params[0].get("maleU1"));
        params[0].put("art6f1", params[0].get("femaleU1"));
        params[0].put("art6t1", Integer.toString(Integer.parseInt(params[0].get("maleU1")) + Integer.parseInt(params[0].get("femaleU1"))));

        params[0].put("art6m2", params[0].get("maleU5"));
        params[0].put("art6f2", params[0].get("femaleU1"));
        params[0].put("art6t2", Integer.toString(Integer.parseInt(params[0].get("maleU5")) + Integer.parseInt(params[0].get("femaleU5"))));

        params[0].put("art6m3", params[0].get("maleU10"));
        params[0].put("art6f3", params[0].get("maleU10"));
        params[0].put("art6t3", Integer.toString(Integer.parseInt(params[0].get("maleU10")) + Integer.parseInt(params[0].get("femaleU10"))));

        params[0].put("art6m4", params[0].get("maleU15"));
        params[0].put("art6f4", params[0].get("maleU15"));
        params[0].put("art6t4", Integer.toString(Integer.parseInt(params[0].get("maleU15")) + Integer.parseInt(params[0].get("femaleU15"))));

        params[0].put("art6m5", params[0].get("maleU20"));
        params[0].put("art6f5", params[0].get("maleU20"));
        params[0].put("art6t5", Integer.toString(Integer.parseInt(params[0].get("maleU20")) + Integer.parseInt(params[0].get("femaleU20"))));

        params[0].put("art6m6", params[0].get("maleU25"));
        params[0].put("art6f6", params[0].get("femaleU25"));
        params[0].put("art6t6", Integer.toString(Integer.parseInt(params[0].get("maleU25")) + Integer.parseInt(params[0].get("femaleU25"))));

        params[0].put("art6m7", params[0].get("maleU49"));
        params[0].put("art6f7", params[0].get("femaleU49"));
        params[0].put("art6t7", Integer.toString(Integer.parseInt(params[0].get("maleU49")) + Integer.parseInt(params[0].get("femaleU49"))));

        params[0].put("art6m8", params[0].get("maleO49"));
        params[0].put("art6f8", params[0].get("femaleO49"));
        params[0].put("art6t8", Integer.toString(Integer.parseInt(params[0].get("maleO49")) + Integer.parseInt(params[0].get("femaleO49"))));

        params[0].put("art6f12", Integer.toString(pregnant[0]));
        params[0].put("art6t12", Integer.toString(pregnant[0]));
        params[0].put("art6f13", Integer.toString(breastfeeding[0]));
        params[0].put("art6t13", Integer.toString(breastfeeding[0]));

        return params[0];
    }

    public Map<String, String> art7(Long facilityId, LocalDate start, LocalDate end) {
        final Map<String, String>[] params = new Map[]{initParams()};
        final int[] agem1 = {0};
        final int[] agef1 = {0};

        //ART 7
        //Total number of people living with HIV known to have died during the month
        String query = "SELECT DISTINCT gender, DATEDIFF('YEAR', date_birth , ?) AS age FROM patient p JOIN status_history s " +
                "ON p.id = patient_id WHERE p.facility_id = ? AND status = 'KNOWN_DEATH' AND date_status BETWEEN ? AND ? and " +
                "p.archived = false AND s.archived = false";
        jdbcTemplate.query(query, resultSet -> {
            while (resultSet.next()) {
                String gender = resultSet.getString("gender");
                int age = resultSet.getInt("age");
                if (gender.trim().equalsIgnoreCase("Male")) {
                    agem1[0]++;
                } else {
                    agef1[0]++;
                }
            }
            return null;
        }, end, facilityId, start, end);
        params[0].put("art7m1", Integer.toString(agem1[0]));
        params[0].put("art7f1", Integer.toString(agef1[0]));
        params[0].put("art7t1", Integer.toString(agem1[0] + agef1[0]));

        return params[0];
    }

    public Map<String, String> art8(Long facilityId, LocalDate start, LocalDate end) {
        final Map<String, String>[] params = new Map[]{initParams()};
        final int[] agem1 = {0};
        final int[] agef1 = {0};

        //ART 8
        //Number of People living with HIV who are lost to follow up during the month
        String query = "SELECT DISTINCT gender, DATEDIFF('YEAR', date_birth , ?) AS age FROM patient p JOIN status_history s " +
                "ON p.id = patient_id WHERE p.facility_id = ? AND status = 'LOST_TO_FOLLOWUP' AND date_status BETWEEN ? AND ? and " +
                "p.archived = false AND s.archived = false";
        jdbcTemplate.query(query, resultSet -> {
            while (resultSet.next()) {
                String gender = resultSet.getString("gender");
                if (gender.trim().equalsIgnoreCase("Male")) {
                    agem1[0]++;
                } else {
                    agef1[0]++;
                }
            }
            return null;
        }, end, facilityId, start, end);
        params[0].put("art8m1", Integer.toString(agem1[0]));
        params[0].put("art8f1", Integer.toString(agef1[0]));
        params[0].put("art8t1", Integer.toString(agem1[0] + agef1[0]));
        return params[0];
    }

    public Map<String, String> headers(Long facilityId, LocalDate reportingPeriod) {
        Map<String, String> params = new HashMap<>();
        params.put("reportingMonth", reportingPeriod.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        params.put("reportingYear", Integer.toString(reportingPeriod.getYear()));

        String query = "SELECT DISTINCT f.name, s.name state, l.name lga FROM facility f JOIN state s ON s.id = state_id " +
                "JOIN lga l ON l.id = lga_id WHERE f.id = ?";
        jdbcTemplate.query(query, rs -> {
            while (rs.next()) {
                params.put("facilityName", rs.getString("name"));
                params.put("facilityType", "");
                params.put("lga", rs.getString("lga"));
                params.put("state", rs.getString("state"));
                params.put("level", "");
            }
            return null;
        }, facilityId);

        return params;
    }

    private Map<String, String> disaggregate(String gender, int age, Map<String, String> params) {
        int agem1 = Integer.parseInt(params.get("maleU1"));
        int agem2 = Integer.parseInt(params.get("maleU5"));
        int agem3 = Integer.parseInt(params.get("maleU10"));
        int agem4 = Integer.parseInt(params.get("maleU15"));
        int agem5 = Integer.parseInt(params.get("maleU20"));
        int agem6 = Integer.parseInt(params.get("maleU25"));
        int agem7 = Integer.parseInt(params.get("maleU49"));
        int agem8 = Integer.parseInt(params.get("maleO49"));
        int agef1 = Integer.parseInt(params.get("femaleU1"));
        int agef2 = Integer.parseInt(params.get("femaleU5"));
        int agef3 = Integer.parseInt(params.get("femaleU10"));
        int agef4 = Integer.parseInt(params.get("femaleU15"));
        int agef5 = Integer.parseInt(params.get("femaleU20"));
        int agef6 = Integer.parseInt(params.get("femaleU25"));
        int agef7 = Integer.parseInt(params.get("femaleU49"));
        int agef8 = Integer.parseInt(params.get("femaleO49"));
        if (gender.trim().equalsIgnoreCase("Male")) {
            if (age < 1) {
                agem1++;
            } else {
                if (age >= 1 && age <= 4) {
                    agem2++;
                } else {
                    if (age >= 5 && age <= 9) {
                        agem3++;
                    } else {
                        if (age >= 10 && age <= 14) {
                            agem4++;
                        } else {
                            if (age >= 15 && age <= 19) {
                                agem5++;
                            } else {
                                if (age >= 20 && age <= 24) {
                                    agem6++;
                                } else {
                                    if (age >= 25 && age <= 49) {
                                        agem7++;
                                    } else {
                                        if (age >= 50) {
                                            agem8++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (age < 1) {
                agef1++;
            } else {
                if (age >= 1 && age <= 4) {
                    agef2++;
                } else {
                    if (age >= 5 && age <= 9) {
                        agef3++;
                    } else {
                        if (age >= 10 && age <= 14) {
                            agef4++;
                        } else {
                            if (age >= 15 && age <= 19) {
                                agef5++;
                            } else {
                                if (age >= 20 && age <= 24) {
                                    agef6++;
                                } else {
                                    if (age >= 25 && age <= 49) {
                                        agef7++;
                                    } else {
                                        if (age >= 50) {
                                            agef8++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        params.put("maleU1", Integer.toString(agem1));
        params.put("maleU5", Integer.toString(agem2));
        params.put("maleU10", Integer.toString(agem3));
        params.put("maleU15", Integer.toString(agem4));
        params.put("maleU20", Integer.toString(agem5));
        params.put("maleU25", Integer.toString(agem6));
        params.put("maleU49", Integer.toString(agem7));
        params.put("maleO49", Integer.toString(agem8));
        params.put("femaleU1", Integer.toString(agef1));
        params.put("femaleU5", Integer.toString(agef2));
        params.put("femaleU10", Integer.toString(agef3));
        params.put("femaleU15", Integer.toString(agef4));
        params.put("femaleU20", Integer.toString(agef5));
        params.put("femaleU25", Integer.toString(agef6));
        params.put("femaleU49", Integer.toString(agef7));
        params.put("femaleO49", Integer.toString(agef8));
        return params;
    }

    private Map<String, String> initParams() {
        Map<String, String> params = new HashMap<>();
        params.put("maleU1", "0");
        params.put("maleU5", "0");
        params.put("maleU10", "0");
        params.put("maleU15", "0");
        params.put("maleU20", "0");
        params.put("maleU25", "0");
        params.put("maleU49", "0");
        params.put("maleO49", "0");
        params.put("femaleU1", "0");
        params.put("femaleU5", "0");
        params.put("femaleU10", "0");
        params.put("femaleU15", "0");
        params.put("femaleU20", "0");
        params.put("femaleU25", "0");
        params.put("femaleU49", "0");
        params.put("femaleO49", "0");
        return params;
    }

    /*public ReportResult art1(Long facilityId, LocalDate reportingPeriod) {
        LocalDate start = reportingPeriod.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = reportingPeriod.with(TemporalAdjusters.lastDayOfMonth());

        String query = "WITH disaggregation(gender, age) as ( " +
                "SELECT gender, DATEDIFF('YEAR', date_birth, ?) AS age FROM patient p WHERE facility_id = ? AND " +
                "date_registration BETWEEN ? AND ? AND (select status from status_history where patient_id = p.id and date_status " +
                "<= current_date order by date_status desc limit 1) = 'HIV_PLUS_NON_ART')" +
                "" +
                "" +
                "select case when age < 1 and gender = 'MALE' then 'maleU1' when age < 1 and gender = 'FEMALE' then 'femaleU1' " +
                "when age between 1 and 4 and gender = 'MALE' then 'maleU5' when age between 1 and 4 and gender = 'FEMALE' then 'femaleU5' " +
                "when age between 5 and 9 and gender = 'MALE' then 'maleU10' when age between 5 and 9 and gender = 'FEMALE' then 'femaleU10'" +
                "when age between 10 and 14 and gender = 'MALE' then 'maleU15' when age between 10 and 14 and gender = 'FEMALE' then 'femaleU15' " +
                "when age between 15 and 19 and gender = 'MALE' then 'maleU20' when age between 15 and 19 and gender = 'FEMALE' then 'femaleU20'" +
                "when age between 20 and 24 and gender = 'MALE' then 'maleU25' when age between 20 and 24 and gender = 'FEMALE' then 'femaleU25'" +
                "when age between 25 and 49 and gender = 'MALE' then 'maleU49' when age between 25 and 49 and gender = 'FEMALE' then 'femaleU49' " +
                "when age > 49 and gender = 'MALE' then 'maleO49' when age > 49 and gender = 'FEMALE' then 'femaleO49' end aggregate, count(*) value from " +
                "disaggregation group by 1 order by 1";
         return jdbcTemplate.query(query, new ReportResultExtractor(), end, facilityId, start, end);
    }

    public ReportResult art1Breastfeeding(Long facilityId, LocalDate reportingPeriod) {
        LocalDate start = reportingPeriod.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = reportingPeriod.with(TemporalAdjusters.lastDayOfMonth());

        String query = "WITH disaggregation(breastfeeding) as ( " +
                "SELECT breastfeeding FROM patient p WHERE facility_id = ? AND date_registration BETWEEN ? AND ? " +
                "AND (select status from status_history where patient_id = p.id and date_status " +
                "<= current_date order by date_status desc limit 1) = 'HIV_PLUS_NON_ART')" +
                "" +
                "" +
                "select case when breastfeeding = true then 'breastfeeding' else 'others' end aggregate, count(*) value from " +
                "disaggregation group by 1 order by 1";
        return jdbcTemplate.query(query, new ReportResultExtractor(), end, facilityId, start, end);
    }

    public ReportResult art1Pregnant(Long facilityId, LocalDate reportingPeriod) {
        LocalDate start = reportingPeriod.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = reportingPeriod.with(TemporalAdjusters.lastDayOfMonth());

        String query = "WITH disaggregation(pregnant) as ( " +
                "SELECT pregnant FROM patient p WHERE facility_id = ? AND date_registration BETWEEN ? AND ? " +
                "AND (select status from status_history where patient_id = p.id and date_status " +
                "<= current_date order by date_status desc limit 1) = 'HIV_PLUS_NON_ART')" +
                "" +
                "" +
                "select case when breastfeeding = true then 'pregnant' else 'others' end aggregate, count(*) value from " +
                "disaggregation group by 1 order by 1";
        return jdbcTemplate.query(query, new ReportResultExtractor(), end, facilityId, start, end);
    }

    public ReportResult art1TbStatus(Long facilityId, LocalDate reportingPeriod) {
        LocalDate start = reportingPeriod.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = reportingPeriod.with(TemporalAdjusters.lastDayOfMonth());

        String query = "WITH disaggregation(gender, status) as ( " +
                "SELECT gender, tb_status " +
                "FROM patient p WHERE facility_id = ? AND date_registration BETWEEN ? AND ? " +
                "AND (select status from status_history where patient_id = p.id and date_status " +
                "<= current_date order by date_status desc limit 1) = 'HIV_PLUS_NON_ART')" +
                "" +
                "" +
                "select case when gender = 'MALE' and lower(status) in ('currently on tb treatment', '') then 'maleTb' " +
                "when gender = 'FEMALE' and lower(status) in ('currently on tb treatment', '') then 'femaleTb' end aggregate, count(*) value from " +
                "disaggregation group by 1 order by 1";
        return jdbcTemplate.query(query, new ReportResultExtractor(), end, facilityId, start, end);
    }*/
}
