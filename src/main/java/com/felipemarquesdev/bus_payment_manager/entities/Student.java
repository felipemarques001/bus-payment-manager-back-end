package com.felipemarquesdev.bus_payment_manager.entities;

import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentRequestDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TB_STUDENT")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String major;

    private String college;

    private Boolean active;

    public Student(StudentRequestDTO dto) {
        this.name = dto.name();
        this.phoneNumber = dto.phoneNumber();
        this.major = dto.major();
        this.college = dto.college();
        this.active = true;
    }
}
