package com.wibuneverdie.core.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UaRoleMenuRelationId implements Serializable {

    @Column(name = "role_id", length = 255)
    private String roleId;

    @Column(name = "menu_id", length = 255)
    private String menuId;
}
