ALTER TABLE student_messages
    MODIFY COLUMN content VARCHAR(500) NULL,
    ADD COLUMN template_id BIGINT NULL,
    ADD CONSTRAINT fk_student_messages_template
        FOREIGN KEY (template_id) REFERENCES message_templates(id) ON DELETE CASCADE,
    ADD CONSTRAINT uk_student_messages_student_template
        UNIQUE (student_id, template_id),
    ADD CONSTRAINT chk_student_messages_content_xor_template
        CHECK ((content IS NOT NULL AND template_id IS NULL)
            OR (content IS NULL AND template_id IS NOT NULL));

CREATE INDEX idx_student_messages_template_id ON student_messages(template_id);
