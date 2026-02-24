-- 출석: 학생별 당일 출석 조회 최적화
CREATE INDEX idx_attendance_student_checkin_status
    ON attendances(student_id, check_in_at, status);

-- 외출: 학생별 당일 진행 중 외출 조회 최적화
CREATE INDEX idx_outing_student_started_ended
    ON outings(student_id, started_at, ended_at);

-- 좌석 사용: 좌석별 상태 조회 최적화 (입퇴실)
CREATE INDEX idx_seat_usage_seat_status
    ON seat_usages(seat_id, status);

-- 좌석 사용: 학생별 상태 조회 최적화 (중복 입실 체크)
CREATE INDEX idx_seat_usage_student_status
    ON seat_usages(student_id, status);
