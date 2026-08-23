CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(30) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    kakao_id VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    major VARCHAR(100),
    student_no VARCHAR(20),
    push_token VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE admins (
    id BIGSERIAL PRIMARY KEY,
    login_id VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    department_id BIGINT NOT NULL REFERENCES departments(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE buildings (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(30) UNIQUE,
    lat DECIMAL(10,7) NOT NULL,
    lng DECIMAL(10,7) NOT NULL,
    address VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    tracking_id VARCHAR(20) UNIQUE,
    summary VARCHAR(200),
    description TEXT,
    status VARCHAR(20) NOT NULL,
    risk_level VARCHAR(10),
    risk_level_source VARCHAR(10) NOT NULL DEFAULT 'AI',
    department_id BIGINT REFERENCES departments(id),
    reporter_id BIGINT NOT NULL REFERENCES users(id),
    reporter_type VARCHAR(15) NOT NULL,
    building_id BIGINT REFERENCES buildings(id),
    building_name_snapshot VARCHAR(100),
    is_indoor BOOLEAN,
    floor VARCHAR(10),
    room VARCHAR(30),
    lat DECIMAL(10,7),
    lng DECIMAL(10,7),
    detected_hazards TEXT,
    report_file_url VARCHAR(500),
    parent_report_id BIGINT REFERENCES reports(id),
    submitted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_reports_department ON reports(department_id);
CREATE INDEX idx_reports_reporter ON reports(reporter_id);
CREATE INDEX idx_reports_building ON reports(building_id);
CREATE INDEX idx_reports_status_department ON reports(status, department_id);

CREATE TABLE report_photos (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    original_filename VARCHAR(255),
    mime_type VARCHAR(50),
    size_bytes BIGINT,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE report_activity_logs (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    activity_type VARCHAR(30) NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20),
    from_department_id BIGINT,
    to_department_id BIGINT,
    actor_type VARCHAR(10) NOT NULL,
    actor_id BIGINT,
    action_note TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_activity_report ON report_activity_logs(report_id);
CREATE INDEX idx_activity_report_created ON report_activity_logs(report_id, created_at);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    type VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    message VARCHAR(300) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_report ON notifications(report_id);
