package com.idApps.userApi.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "action")
@Data
public class ActionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;

    private String label;
}
