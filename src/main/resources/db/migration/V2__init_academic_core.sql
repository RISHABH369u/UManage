CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS programs (
    id UUID PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    department_id UUID NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS semesters (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    sequence_number INT NOT NULL,
    program_id UUID NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_semester_program_sequence UNIQUE (program_id, sequence_number)
);

CREATE TABLE IF NOT EXISTS courses (
    id UUID PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    credits NUMERIC(4,2) NOT NULL,
    department_id UUID NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS course_offerings (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    semester_id UUID NOT NULL REFERENCES semesters(id) ON DELETE CASCADE,
    section_code VARCHAR(20) NOT NULL,
    capacity INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_offering_course_semester_section UNIQUE (course_id, semester_id, section_code)
);

CREATE TABLE IF NOT EXISTS prerequisites (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    prerequisite_course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_course_prerequisite UNIQUE (course_id, prerequisite_course_id),
    CONSTRAINT chk_not_self_prerequisite CHECK (course_id <> prerequisite_course_id)
);

CREATE TABLE IF NOT EXISTS faculty_assignments (
    id UUID PRIMARY KEY,
    course_offering_id UUID NOT NULL REFERENCES course_offerings(id) ON DELETE CASCADE,
    faculty_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_offering_faculty_assignment UNIQUE (course_offering_id)
);

CREATE INDEX IF NOT EXISTS idx_programs_department ON programs(department_id);
CREATE INDEX IF NOT EXISTS idx_semesters_program ON semesters(program_id);
CREATE INDEX IF NOT EXISTS idx_courses_department ON courses(department_id);
CREATE INDEX IF NOT EXISTS idx_course_offerings_course ON course_offerings(course_id);
CREATE INDEX IF NOT EXISTS idx_course_offerings_semester ON course_offerings(semester_id);
CREATE INDEX IF NOT EXISTS idx_prerequisites_course ON prerequisites(course_id);
CREATE INDEX IF NOT EXISTS idx_prerequisites_prereq ON prerequisites(prerequisite_course_id);
CREATE INDEX IF NOT EXISTS idx_faculty_assignment_user ON faculty_assignments(faculty_user_id);
