package com.wibuneverdie.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "patients_database", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "patientId")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Integer patientId;

    @Column(name = "photo", columnDefinition = "text")
    private String photo;

    @Column(name = "last_name", columnDefinition = "text", nullable = false)
    private String lastName;

    @Column(name = "first_name", columnDefinition = "text", nullable = false)
    private String firstName;

    @Column(name = "e_mail_address", columnDefinition = "text")
    private String emailAddress;

    @Column(name = "walk_in_date")
    private LocalDate walkInDate;

    @Column(name = "mobile_phone", columnDefinition = "text", nullable = false, unique = true)
    private String mobilePhone;

    @Column(name = "address", columnDefinition = "text")
    private String address;

    @Column(name = "special_remarks", columnDefinition = "text")
    private String specialRemarks;

    @Column(name = "next_appointment")
    private LocalDate nextAppointment;

    @Column(name = "schedule_at", columnDefinition = "text")
    private String scheduleAt;

    @Column(name = "diagnosis", columnDefinition = "text")
    private String diagnosis;

    @Column(name = "attachments", columnDefinition = "text")
    private String attachments;

    @Column(name = "projected_bill", columnDefinition = "text")
    private String projectedBill;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "sex", columnDefinition = "text", nullable = false)
    private String sex;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;
}
