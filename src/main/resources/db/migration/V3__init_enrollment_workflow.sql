CREATE TABLE IF NOT EXISTS enrollments (
    id UUID PRIMARY KEY,
    student_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_offering_id UUID NOT NULL REFERENCES course_offerings(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_enrollment_student_offering UNIQUE (student_user_id, course_offering_id)
);

CREATE TABLE IF NOT EXISTS enrollment_requests (
    id UUID PRIMARY KEY,
    student_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_offering_id UUID NOT NULL REFERENCES course_offerings(id) ON DELETE CASCADE,
    requested_action VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    decided_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    decision_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_enrollments_student_status ON enrollments(student_user_id, status);
CREATE INDEX IF NOT EXISTS idx_enrollments_offering_status ON enrollments(course_offering_id, status);
CREATE INDEX IF NOT EXISTS idx_enrollment_requests_student ON enrollment_requests(student_user_id);
CREATE INDEX IF NOT EXISTS idx_enrollment_requests_status ON enrollment_requests(status);
CREATE INDEX IF NOT EXISTS idx_enrollment_requests_offering ON enrollment_requests(course_offering_id);
