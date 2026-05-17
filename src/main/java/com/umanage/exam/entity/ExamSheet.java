package com.umanage.exam.entity;

import com.umanage.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "exam_sheets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_exam_sheet_id", columnNames = {"sheet_id"}),
        @UniqueConstraint(name = "uk_exam_seat_sheet", columnNames = {"exam_seat_plan_id"})
})
public class ExamSheet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_seat_plan_id", nullable = false)
    private ExamSeatPlan examSeatPlan;

    @Column(name = "sheet_id", nullable = false, length = 80)
    private String sheetId;

    @Column(nullable = false)
    private int examVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SheetSymbolType symbolType;

    @Column(nullable = false, length = 1000)
    private String qrPayload;

    @Column(nullable = false, length = 1000)
    private String barcodePayload;

    @Column(nullable = false, length = 255)
    private String pdfFileName;

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public ExamSeatPlan getExamSeatPlan() {
        return examSeatPlan;
    }

    public void setExamSeatPlan(ExamSeatPlan examSeatPlan) {
        this.examSeatPlan = examSeatPlan;
    }

    public String getSheetId() {
        return sheetId;
    }

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    public int getExamVersion() {
        return examVersion;
    }

    public void setExamVersion(int examVersion) {
        this.examVersion = examVersion;
    }

    public SheetSymbolType getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(SheetSymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public String getQrPayload() {
        return qrPayload;
    }

    public void setQrPayload(String qrPayload) {
        this.qrPayload = qrPayload;
    }

    public String getBarcodePayload() {
        return barcodePayload;
    }

    public void setBarcodePayload(String barcodePayload) {
        this.barcodePayload = barcodePayload;
    }

    public String getPdfFileName() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }
}
