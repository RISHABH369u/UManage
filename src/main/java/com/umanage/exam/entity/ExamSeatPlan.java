package com.umanage.exam.entity;

import com.umanage.common.model.BaseEntity;
import com.umanage.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "exam_seat_plans", uniqueConstraints = {
        @UniqueConstraint(name = "uk_exam_student_seat_plan", columnNames = {"exam_id", "student_user_id"}),
        @UniqueConstraint(name = "uk_exam_seat_number", columnNames = {"exam_id", "seat_number"})
})
public class ExamSeatPlan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_room_allocation_id", nullable = false)
    private ExamRoomAllocation examRoomAllocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User studentUser;

    @Column(nullable = false)
    private int seatNumber;

    @Column(nullable = false, length = 50)
    private String seatLabel;

    @Column(nullable = false)
    private int examVersion;

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public ExamRoomAllocation getExamRoomAllocation() {
        return examRoomAllocation;
    }

    public void setExamRoomAllocation(ExamRoomAllocation examRoomAllocation) {
        this.examRoomAllocation = examRoomAllocation;
    }

    public User getStudentUser() {
        return studentUser;
    }

    public void setStudentUser(User studentUser) {
        this.studentUser = studentUser;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }

    public int getExamVersion() {
        return examVersion;
    }

    public void setExamVersion(int examVersion) {
        this.examVersion = examVersion;
    }
}
