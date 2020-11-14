package org.fhi360.lamis.modules.reporting.service;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ReportResult {
    private long femaleU1;
    private long maleU1;
    private long femaleU5;
    private long maleU5;
    private long femaleU10;
    private long maleU10;
    private long femaleU15;
    private long maleU15;
    private long femaleO15;
    private long maleO15;
    private long femaleU15B;
    private long maleU15B;
    private long femaleU20;
    private long maleU20;
    private long femaleU25;
    private long maleU25;
    private long femaleU30;
    private long maleU30;
    private long femaleU35;
    private long maleU35;
    private long femaleU40;
    private long maleU40;
    private long femaleU45;
    private long maleU45;
    private long femaleU50;
    private long maleU50;
    private long femaleO49;
    private long maleO49;
    private long femaleTb;
    private long maleTb;
    private long breastfeeding;
    private long pregnant;
    private long female;
    private long male;
    private long total;

    public void setFemaleU1(long femaleU1) {
        this.femaleU1 = femaleU1;
        femaleU15B += femaleU1;
        female += femaleU1;
        total += femaleU1;
    }

    public void setMaleU1(long maleU1) {
        this.maleU1 = maleU1;
        maleU15B += maleU1;
        male += maleU1;
        total += maleU1;
    }

    public void setFemaleU5(long femaleU5) {
        this.femaleU5 = femaleU5;
        femaleU15B += femaleU5;
        female += femaleU5;
        total += femaleU5;
    }

    public void setMaleU5(long maleU5) {
        this.maleU5 = maleU5;
        maleU15B += maleU5;
        male += maleU5;
        total += maleU5;
    }

    public void setFemaleU10(long femaleU10) {
        this.femaleU10 = femaleU10;
        femaleU15B += femaleU10;
        female += femaleU10;
        total += femaleU10;
    }

    public void setMaleU10(long maleU10) {
        this.maleU10 = maleU10;
        male += maleU10;
        maleU15B += maleU10;
        total += maleU10;
    }

    public void setFemaleU15(long femaleU15) {
        this.femaleU15 = femaleU15;
        femaleU15B += femaleU15;
        female += femaleU15;
        total += femaleU15;
    }

    public void setMaleU15(long maleU15) {
        this.maleU15 = maleU15;
        maleU15B += maleU15;
        male += maleU15;
        total += maleU15;
    }

    public void setFemaleU20(long femaleU20) {
        this.femaleU20 = femaleU20;
        femaleO15 += femaleU20;
        female += femaleU20;
        total += femaleU20;
    }

    public void setMaleU20(long maleU20) {
        this.maleU20 = maleU20;
        maleO15 += maleU20;
        male += maleU20;
        total += maleU20;
    }

    public void setFemaleU25(long femaleU25) {
        this.femaleU25 = femaleU25;
        femaleO15 += femaleU25;
        female += femaleU25;
        total += femaleU25;
    }

    public void setMaleU25(long maleU25) {
        this.maleU25 = maleU25;
        maleO15 += maleU25;
        male += maleU25;
        total += maleU25;
    }

    public void setFemaleU30(long femaleU30) {
        this.femaleU30 = femaleU30;
        femaleO15 += femaleU30;
        female += femaleU30;
        total += femaleU30;
    }

    public void setMaleU30(long maleU30) {
        this.maleU30 = maleU30;
        male += maleU30;
        maleO15 += maleU30;
        total += maleU30;
    }

    public void setFemaleU35(long femaleU35) {
        this.femaleU35 = femaleU35;
        femaleO15 += femaleU35;
        female += femaleU35;
        total += femaleU35;
    }

    public void setMaleU35(long maleU35) {
        this.maleU35 = maleU35;
        maleO15 += maleU35;
        male += maleU35;
        total += maleU35;
    }

    public void setFemaleU40(long femaleU40) {
        this.femaleU40 = femaleU40;
        femaleO15 += femaleU40;
        female += femaleU40;
        total += femaleU40;
    }

    public void setMaleU40(long maleU40) {
        this.maleU40 = maleU40;
        maleO15 += maleU40;
        male += maleU40;
        total += maleU40;
    }

    public void setFemaleU45(long femaleU45) {
        this.femaleU45 = femaleU45;
        femaleO15 += femaleU45;
        female += femaleU45;
        total += femaleU45;
    }

    public void setMaleU45(long maleU45) {
        this.maleU45 = maleU45;
        maleO15 += maleU45;
        male += maleU45;
        total += maleU45;
    }

    public void setFemaleU50(long femaleU50) {
        this.femaleU50 = femaleU50;
        femaleO15 += femaleU50;
        female += femaleU50;
        total += femaleU50;
    }

    public void setMaleU50(long maleU50) {
        this.maleU50 = maleU50;
        maleO15 += maleU50;
        male += maleU50;
        total += maleU50;
    }

    public void setFemaleO49(long femaleO49) {
        this.femaleO49 = femaleO49;
        femaleO15 += femaleO49;
        female += femaleO49;
        total += femaleO49;
    }

    public void setMaleO49(long maleO49) {
        this.maleO49 = maleO49;
        maleO15 += maleO49;
        male += maleO49;
        total += maleO49;
    }

    public void setFemaleTb(long femaleTb) {
        this.femaleTb = femaleTb;
    }

    public void setMaleTb(long maleTb) {
        this.maleTb = maleTb;
    }

    public void setBreastfeeding(long breastfeeding) {
        this.breastfeeding = breastfeeding;
    }

    public void setPregnant(long pregnant) {
        this.pregnant = pregnant;
    }
}
