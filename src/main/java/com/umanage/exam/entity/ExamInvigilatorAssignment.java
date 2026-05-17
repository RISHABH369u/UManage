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
@Table(name = "exam_invigilator_assignments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_exam_invigilator", columnNames = {"exam_id", "invigilator_user_id"}),
        @UniqueConstraint(name = "uk_exam_room_invigilator", columnNames = {"exam_room_allocation_id", "invigilator_user_id"})
})
public class ExamInvigilatorAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_room_allocation_id", nullable = false)
    private ExamRoomAllocation examRoomAllocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invigilator_user_id", nullable = false)
    private User invigilatorUser;

    @Column(nullable = false)
    private int assignmentOrder;

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

    public User getInvigilatorUser() {
        return invigilatorUser;
    }

    public void setInvigilatorUser(User invigilatorUser) {
        this.invigilatorUser = invigilatorUser;
    }

    public int getAssignmentOrder() {
        return assignmentOrder;
    }

    public void setAssignmentOrder(int assignmentOrder) {
        this.assignmentOrder = assignmentOrder;
    }
}
