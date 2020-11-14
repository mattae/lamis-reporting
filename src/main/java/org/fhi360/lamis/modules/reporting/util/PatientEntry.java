package org.fhi360.lamis.modules.reporting.util;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientEntry {
    Long patientId;
    String uniqueId;
    String hospitalNum;
    String sex;
    float weight;
    LocalDate dob;
    LocalDate dateStarted;
    int age;
    String artEnrollmentSetting;
}
