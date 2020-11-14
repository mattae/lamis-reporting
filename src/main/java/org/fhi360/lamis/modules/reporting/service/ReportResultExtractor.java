package org.fhi360.lamis.modules.reporting.service;

import org.jooq.exception.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportResultExtractor implements ResultSetExtractor<ReportResult> {

    @Override
    public ReportResult extractData(ResultSet rs) throws SQLException, DataAccessException {
        ReportResult result = new ReportResult();
        while (rs.next()) {
            String aggregate = rs.getString("aggregate");
            long value = rs.getLong("value");
            switch (aggregate) {
                case "femaleU1":
                    result.setFemaleU1(value);
                    break;
                case "maleU1":
                    result.setMaleU1(value);
                    break;
                case "femaleU5":
                    result.setFemaleU5(value);
                    break;
                case "maleU5":
                    result.setMaleU5(value);
                    break;
                case "femaleU10":
                    result.setFemaleU10(value);
                    break;
                case "maleU10":
                    result.setMaleU10(value);
                    break;
                case "femaleU15":
                    result.setFemaleU15(value);
                    break;
                case "maleU15":
                    result.setMaleU15(value);
                    break;
                case "femaleU20":
                    result.setFemaleU20(value);
                    break;
                case "maleU20":
                    result.setMaleU20(value);
                    break;
                case "femaleU25":
                    result.setFemaleU25(value);
                    break;
                case "maleU25":
                    result.setMaleU25(value);
                    break;
                case "femaleU30":
                    result.setFemaleU30(value);
                    break;
                case "maleU30":
                    result.setMaleU30(value);
                    break;
                case "femaleU35":
                    result.setFemaleU35(value);
                    break;
                case "maleU35":
                    result.setMaleU35(value);
                    break;
                case "femaleU40":
                    result.setFemaleU40(value);
                    break;
                case "maleU40":
                    result.setMaleU40(value);
                    break;
                case "femaleU45":
                    result.setFemaleU45(value);
                    break;
                case "maleU45":
                    result.setMaleU45(value);
                    break;
                case "femaleU50":
                    result.setFemaleU50(value);
                    break;
                case "maleU50":
                    result.setMaleU50(value);
                    break;
                case "femaleO49":
                    result.setFemaleO49(value);
                    break;
                case "maleO49":
                    result.setMaleO49(value);
                    break;
            }
        }
        return result;
    }
}
