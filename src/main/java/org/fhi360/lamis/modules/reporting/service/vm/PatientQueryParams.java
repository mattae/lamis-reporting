package org.fhi360.lamis.modules.reporting.service.vm;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class PatientQueryParams {
    @NotNull
    private Long facilityId;
    private String gender;
    private String currentStatus;
    private LocalDate dateRegistrationBegin;
    private LocalDate dateRegistrationEnd;
    private LocalDate dateStartedBegin;
    private LocalDate dateStartedEnd;
    private LocalDate dateLastViralLoadBegin;
    private LocalDate dateLastViralLoadEnd;
    private Double viralLoadBegin;
    private Double viralLoadEnd;
    private String clinicStage;
    private String regimenType;
    private LocalDate dateCurrentStatusBegin;
    private Integer ageBegin;
    private Integer ageEnd;
    private Long stateId;
    private Long lgaId;
    private String format;
}
