package com.moduletest.deasungkioskbackend.domain.studentmessage.entity;

import com.moduletest.deasungkioskbackend.common.entity.BaseTimeEntity;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(length = 500)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private MessageTemplate template;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected StudentMessage(Student student, Store store, String content, MessageTemplate template) {
        this.student = student;
        this.store = store;
        this.content = content;
        this.template = template;
        this.active = true;
    }

    public static StudentMessage ofCustom(Student student, Store store, String content) {
        return new StudentMessage(student, store, content, null);
    }

    public static StudentMessage ofTemplate(Student student, Store store, MessageTemplate template) {
        return new StudentMessage(student, store, null, template);
    }

    public boolean isTemplateBased() {
        return template != null;
    }

    public String resolveContent() {
        if (template != null) {
            return template.getContent();
        }
        return content;
    }

    public void updateCustomContent(String content, boolean active) {
        this.content = content;
        this.active = active;
    }
}
