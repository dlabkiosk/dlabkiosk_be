package com.moduletest.deasungkioskbackend.domain.seatchangerequest.entity;

import com.moduletest.deasungkioskbackend.common.entity.BaseTimeEntity;
import com.moduletest.deasungkioskbackend.domain.seat.entity.Seat;
import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seat_change_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatChangeRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_seat_id")
    private Seat currentSeat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desired_seat_id_1")
    private Seat desiredSeat1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desired_seat_id_2")
    private Seat desiredSeat2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desired_seat_id_3")
    private Seat desiredSeat3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_seat_id")
    private Seat approvedSeat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatChangeRequestStatus status;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Builder
    public SeatChangeRequest(Student student, Store store, Seat currentSeat,
                             Seat desiredSeat1, Seat desiredSeat2, Seat desiredSeat3) {
        this.student = student;
        this.store = store;
        this.currentSeat = currentSeat;
        this.desiredSeat1 = desiredSeat1;
        this.desiredSeat2 = desiredSeat2;
        this.desiredSeat3 = desiredSeat3;
        this.status = SeatChangeRequestStatus.PENDING;
    }

    public void approve(Seat approvedSeat) {
        this.approvedSeat = approvedSeat;
        this.status = SeatChangeRequestStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
    }

    public void clearPrioritiesAtAndBelow(int approvedPriority, Seat newCurrentSeat) {
        this.currentSeat = newCurrentSeat;
        if (approvedPriority <= 1) {
            this.desiredSeat1 = null;
        }
        if (approvedPriority <= 2) {
            this.desiredSeat2 = null;
        }
        if (approvedPriority <= 3) {
            this.desiredSeat3 = null;
        }
    }

    public boolean hasRemainingPriorities(int approvedPriority) {
        return approvedPriority > 1 && this.desiredSeat1 != null;
    }

    public void reject() {
        this.status = SeatChangeRequestStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
    }

    public void updateDesiredSeats(Seat desiredSeat1, Seat desiredSeat2, Seat desiredSeat3) {
        this.desiredSeat1 = desiredSeat1;
        this.desiredSeat2 = desiredSeat2;
        this.desiredSeat3 = desiredSeat3;
    }

    public List<Seat> getDesiredSeatsInOrder() {
        List<Seat> seats = new ArrayList<>();
        seats.add(desiredSeat1);
        if (desiredSeat2 != null) {
            seats.add(desiredSeat2);
        }
        if (desiredSeat3 != null) {
            seats.add(desiredSeat3);
        }
        return seats;
    }
}
