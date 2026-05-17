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

import java.time.OffsetDateTime;

@Entity
@Table(name = "exam_sheet_scan_records", uniqueConstraints = {
        @UniqueConstraint(name = "uk_scan_record_sheet", columnNames = {"exam_sheet_id"})
})
public class ExamSheetScanRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_sheet_id", nullable = false)
    private ExamSheet examSheet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScanProcessingStatus processingStatus = ScanProcessingStatus.PENDING;

    @Column
    private OffsetDateTime scannedAt;

    @Column(columnDefinition = "TEXT")
    private String rawResponseJson;

    @Column(length = 1000)
    private String processingNotes;

    public ExamSheet getExamSheet() {
        return examSheet;
    }

    public void setExamSheet(ExamSheet examSheet) {
        this.examSheet = examSheet;
    }

    public ScanProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ScanProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public OffsetDateTime getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(OffsetDateTime scannedAt) {
        this.scannedAt = scannedAt;
    }

    public String getRawResponseJson() {
        return rawResponseJson;
    }

    public void setRawResponseJson(String rawResponseJson) {
        this.rawResponseJson = rawResponseJson;
    }

    public String getProcessingNotes() {
        return processingNotes;
    }

    public void setProcessingNotes(String processingNotes) {
        this.processingNotes = processingNotes;
    }
}
