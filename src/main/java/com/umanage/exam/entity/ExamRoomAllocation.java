package com.umanage.exam.entity;

import com.umanage.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "exam_room_allocations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_exam_room_code", columnNames = {"exam_id", "room_code"})
})
public class ExamRoomAllocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(nullable = false, length = 50)
    private String roomCode;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int rowCount;

    @Column(nullable = false)
    private int seatsPerRow;

    @Column(nullable = false)
    private int displayOrder;

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getSeatsPerRow() {
        return seatsPerRow;
    }

    public void setSeatsPerRow(int seatsPerRow) {
        this.seatsPerRow = seatsPerRow;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
