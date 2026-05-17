package com.umanage.enrollment.entity;

import com.umanage.academic.entity.CourseOffering;
import com.umanage.common.model.BaseEntity;
import com.umanage.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "enrollment_requests")
public class EnrollmentRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User studentUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_offering_id", nullable = false)
    private CourseOffering courseOffering;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_action", nullable = false, length = 20)
    private EnrollmentAction requestedAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by_user_id")
    private User decidedByUser;

    @Column(length = 500)
    private String decisionReason;

    public User getStudentUser() {
        return studentUser;
    }

    public void setStudentUser(User studentUser) {
        this.studentUser = studentUser;
    }

    public CourseOffering getCourseOffering() {
        return courseOffering;
    }

    public void setCourseOffering(CourseOffering courseOffering) {
        this.courseOffering = courseOffering;
    }

    public EnrollmentAction getRequestedAction() {
        return requestedAction;
    }

    public void setRequestedAction(EnrollmentAction requestedAction) {
        this.requestedAction = requestedAction;
    }

    public EnrollmentRequestStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentRequestStatus status) {
        this.status = status;
    }

    public User getDecidedByUser() {
        return decidedByUser;
    }

    public void setDecidedByUser(User decidedByUser) {
        this.decidedByUser = decidedByUser;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }
}
