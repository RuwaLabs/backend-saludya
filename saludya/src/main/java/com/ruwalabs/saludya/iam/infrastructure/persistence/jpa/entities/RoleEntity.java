package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor
@Entity @Table(name="roles")
public class RoleEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true,length=50) private String name;
}
