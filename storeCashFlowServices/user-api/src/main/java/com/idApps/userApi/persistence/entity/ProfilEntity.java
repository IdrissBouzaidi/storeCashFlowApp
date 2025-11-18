package com.idApps.userApi.persistence.entity;

import com.idApps.userApi.models.response.ProfilResponse;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "profil")
@Data
public class ProfilEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;

    private String label;

    public ProfilResponse mapToProfilResponse() {
        return ProfilResponse.builder()
                .id(this.id)
                .code(this.code)
                .label(this.label)
                .build();
    }
}
