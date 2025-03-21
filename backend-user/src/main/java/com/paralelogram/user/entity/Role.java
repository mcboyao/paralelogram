package com.paralelogram.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "paralelogram_roles")
public class Role extends Base {

    @Column(name = "role_id",  unique = true, nullable = false)
    private UUID roleId;

    @Column(name = "role_name", unique = true, nullable = false, length = 50)
    private String roleName;

}
