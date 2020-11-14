package org.fhi360.lamis.modules.reporting.util;

import lombok.Data;

import java.util.Date;

@Data
public class RadetEntry {
    int sn;
    String patientId;
    String uniqueId;
    String hospitalNum;
    String sex;
    Double weight;
    Date dob;
    Date artStartDate;
    Date lastPickupDate;
    Integer monthsOfRefill;
    String iptInLast2Years;
    Date iptStartDate;
    String regimenLineAtStart;
    String regimenAtStart;
    String currentRegimenLine;
    String currentArtRegimen;
    Date regimenSwitchDate;
    String pregnancyStatus;
    Date viralLoadSampleCollectedDate;
    String currentViralLoad;
    Date currentViralLoadDate;
    String viralLoadIndication;
    String currentArtStatus;
    Date currentArtStatusDate;
    String causeOfDeath;
    String artEnrollmentSetting;
    String receivingDmocService;
    Date dmocServiceCommenceDate;
    String dmocType;
    String eacCommenced;
    Date eacCommencementDate;
    Integer eacSessions;
    String repeatViralLoadPostEacCollected;
    Date repeatViralLoadCollectionDate;
    Date repeatViralLoadReceivedDate;
    String iptType;
    String chronicCareScreening;
    String comorbidities;
    String referred;
    Date dateOfScreening;
    String caseManager;
    boolean ltfu;
}
