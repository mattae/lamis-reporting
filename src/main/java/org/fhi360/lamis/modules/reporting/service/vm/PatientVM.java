package org.fhi360.lamis.modules.reporting.service.vm;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.lamisplus.modules.lamis.legacy.domain.entities.enumerations.ClientStatus;

import java.time.LocalDate;

@Data
public class PatientVM {
    private String hospitalNum;
    private String surname;
    private String otherNames;
    private String clinicStage;
    private LocalDate dateBirth;
    private int age;
    private String gender;
    private LocalDate dateRegistration;
    private String statusRegistration;
    private String currentStatus;
    private LocalDate dateCurrentStatus;
    private String address;
    private String phone;
    private LocalDate dateStarted;
    private String lastViralLoad;
    private LocalDate dateLastViralLoad;
    private LocalDate dateLastRefill;
    private LocalDate dateLastClinic;
    private Long lgaId;
    private String regimenType;
    private Boolean ltfu;

    public void setCurrentStatus(String status) {
        try {
            currentStatus = ClientStatus.valueOf(status).getStatus();
        } catch (Exception e) {
            currentStatus = "";
        }
    }

    public void setLtfu(Boolean ltfu) {
        if (ltfu != null && !ltfu) {
            if ((!StringUtils.equals(currentStatus, ClientStatus.KNOWN_DEATH.getStatus())
                    && !StringUtils.equals(currentStatus, ClientStatus.ART_TRANSFER_OUT.getStatus()))
                    || StringUtils.isBlank(currentStatus)) {
                if (StringUtils.equals(statusRegistration, ClientStatus.ART_TRANSFER_IN.getStatus())) {
                    currentStatus = ClientStatus.ART_TRANSFER_IN.getStatus();
                } else if (StringUtils.equals(currentStatus, ClientStatus.LOST_TO_FOLLOWUP.getStatus())) {
                    currentStatus = ClientStatus.ART_RESTART.getStatus();
                }
                this.ltfu = false;
            } else {
                this.ltfu = null;
            }
        } else {
            this.ltfu = true;
        }
    }

    public void setGender(String gender) {
        this.gender = StringUtils.equals("MALE", gender) ? "Male" : "Female";
    }
}
