CREATE TABLE IF NOT EXISTS exams (
    id UUID PRIMARY KEY,
    course_offering_id UUID NOT NULL REFERENCES course_offerings(id) ON DELETE CASCADE,
    title VARCHAR(180) NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    duration_minutes INTEGER NOT NULL,
    version INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_exam_offering_version UNIQUE (course_offering_id, version)
);

CREATE TABLE IF NOT EXISTS exam_room_allocations (
    id UUID PRIMARY KEY,
    exam_id UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    room_code VARCHAR(50) NOT NULL,
    capacity INTEGER NOT NULL,
    row_count INTEGER NOT NULL,
    seats_per_row INTEGER NOT NULL,
    display_order INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_exam_room_code UNIQUE (exam_id, room_code)
);

CREATE TABLE IF NOT EXISTS exam_invigilator_assignments (
    id UUID PRIMARY KEY,
    exam_id UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    exam_room_allocation_id UUID NOT NULL REFERENCES exam_room_allocations(id) ON DELETE CASCADE,
    invigilator_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assignment_order INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_exam_invigilator UNIQUE (exam_id, invigilator_user_id),
    CONSTRAINT uk_exam_room_invigilator UNIQUE (exam_room_allocation_id, invigilator_user_id)
);

CREATE TABLE IF NOT EXISTS exam_seat_plans (
    id UUID PRIMARY KEY,
    exam_id UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    exam_room_allocation_id UUID NOT NULL REFERENCES exam_room_allocations(id) ON DELETE CASCADE,
    student_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    seat_number INTEGER NOT NULL,
    seat_label VARCHAR(50) NOT NULL,
    exam_version INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_exam_student_seat_plan UNIQUE (exam_id, student_user_id),
    CONSTRAINT uk_exam_seat_number UNIQUE (exam_id, seat_number)
);

CREATE TABLE IF NOT EXISTS exam_sheets (
    id UUID PRIMARY KEY,
    exam_id UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    exam_seat_plan_id UUID NOT NULL REFERENCES exam_seat_plans(id) ON DELETE CASCADE,
    sheet_id VARCHAR(80) NOT NULL,
    exam_version INTEGER NOT NULL,
    symbol_type VARCHAR(20) NOT NULL,
    qr_payload VARCHAR(1000) NOT NULL,
    barcode_payload VARCHAR(1000) NOT NULL,
    pdf_file_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_exam_sheet_id UNIQUE (sheet_id),
    CONSTRAINT uk_exam_seat_sheet UNIQUE (exam_seat_plan_id)
);

CREATE TABLE IF NOT EXISTS exam_sheet_scan_records (
    id UUID PRIMARY KEY,
    exam_sheet_id UUID NOT NULL REFERENCES exam_sheets(id) ON DELETE CASCADE,
    processing_status VARCHAR(20) NOT NULL,
    scanned_at TIMESTAMPTZ,
    raw_response_json TEXT,
    processing_notes VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_scan_record_sheet UNIQUE (exam_sheet_id)
);

CREATE INDEX IF NOT EXISTS idx_exams_offering ON exams(course_offering_id);
CREATE INDEX IF NOT EXISTS idx_exam_room_allocations_exam ON exam_room_allocations(exam_id);
CREATE INDEX IF NOT EXISTS idx_exam_invigilator_assignments_exam ON exam_invigilator_assignments(exam_id);
CREATE INDEX IF NOT EXISTS idx_exam_seat_plans_exam ON exam_seat_plans(exam_id);
CREATE INDEX IF NOT EXISTS idx_exam_sheets_exam ON exam_sheets(exam_id);
CREATE INDEX IF NOT EXISTS idx_exam_sheet_scan_status ON exam_sheet_scan_records(processing_status);
