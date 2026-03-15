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
public class UaRoleUserRelationId implements Serializable {

    @Column(name = "role_id", length = 255)
    private String roleId;

    @Column(name = "user_uid", length = 50)
    private String userUid;
}
