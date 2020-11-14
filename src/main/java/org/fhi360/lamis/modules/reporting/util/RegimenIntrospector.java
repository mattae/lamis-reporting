package org.fhi360.lamis.modules.reporting.util;

import org.lamisplus.modules.base.config.ContextProvider;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

public class RegimenIntrospector {
    private final static JdbcTemplate jdbcTemplate = ContextProvider.getBean(JdbcTemplate.class);

    private RegimenIntrospector() {
    }

    public static Boolean substitutedOrSwitched(String regimen1, String regimen2) {
        String query = "SELECT DISTINCT composition, description FROM regimen WHERE description = ? OR description = ?";
        return jdbcTemplate.query(query, rs -> {
            String composition1 = "";
            String composition2 = "";
            while (rs.next()) {
                if (rs.getString("description").equals(regimen1)) {
                    composition1 = rs.getString("composition");
                }
                if (rs.getString("description").equals(regimen2)) {
                    composition2 = rs.getString("composition");
                }
            }
            return !composition1.equals(composition2);
        }, regimen1, regimen2);
    }

    public static Boolean substitutedOrSwitched(long regimenId1, long regimenId2) {
        String query = "SELECT DISTINCT composition, id FROM regimen WHERE id = ? OR id = ?";
        return jdbcTemplate.query(query, rs -> {
            String composition1 = "";
            String composition2 = "";
            while (rs.next()) {
                if (rs.getString("id").equals(regimenId1)) {
                    composition1 = rs.getString("composition");
                }
                if (rs.getString("id").equals(regimenId2)) {
                    composition2 = rs.getString("composition");
                }
            }
            return !composition1.equals(composition2);
        }, regimenId1, regimenId2);
    }

    //This method returns the details of the first time this regimen was dispensed to the patient
    public static RegimenHistory getRegimenHistory(long patientId, long regimentypeId, long regimenId) {
        String regimenType = jdbcTemplate.queryForObject("select description from regimen_type where id = ?",
                String.class, regimentypeId);
        String regimen = jdbcTemplate.queryForObject("select description from regimen where id = ?",
                String.class, regimenId);
        return getRegimenHistory(patientId, regimenType, regimen);
    }

    public static RegimenHistory getRegimenHistory(long patientId, String regimenType, String regimen) {
        String query = "SELECT * FROM regimen_history JOIN regimen r ON r.id = regimen_id JOIN regimen_type t ON " +
                "t.id = r.regimen_type_id WHERE patient_id = ? AND t.description = ? AND r.description = ? and archived = false " +
                "ORDER BY date_visit ASC LIMIT 1";
        List<RegimenHistory> histories = jdbcTemplate.query(query, rs -> {
            List<RegimenHistory> h = new ArrayList<>();
            while (rs.next()) {
                RegimenHistory history = new RegimenHistory();
                history.setId(rs.getLong("id"));
                history.setDateVisit(rs.getDate("date_visit"));
                history.setReasonSwitchedSubs(rs.getString("reason_switched_subs"));
                history.setRegimen(regimen);
                history.setRegimenType(regimenType);
                h.add(history);
            }
            return h;
        }, patientId, regimenType, regimen);
        if (histories != null && !histories.isEmpty()) {
            return histories.get(0);
        }
        return new RegimenHistory();
    }

    public static String resolveRegimen(String regimensys) {
        String query = "SELECT regimen FROM regimen_resolver WHERE regimensys = ?";
        return jdbcTemplate.query(query, rs -> {
            if (rs.next()) {
                return rs.getString("regimen");
            }
            return "";
        }, regimensys);
    }

}
