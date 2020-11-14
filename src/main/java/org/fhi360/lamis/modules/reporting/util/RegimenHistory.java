/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fhi360.lamis.modules.reporting.util;

import lombok.Data;

import java.util.Date;


@Data
public class RegimenHistory {

    private static final long serialVersionUID = 1L;

    private Long Id;

    private Date dateVisit;

    private String regimenType;

    private String regimen;

    private String reasonSwitchedSubs;
}
