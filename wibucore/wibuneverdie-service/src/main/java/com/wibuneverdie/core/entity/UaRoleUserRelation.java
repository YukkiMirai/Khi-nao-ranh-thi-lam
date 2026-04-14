package com.wibuneverdie.core.entity;

import com.wibuneverdie.core.entity.embeddable.UaRoleUserRelationId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ua_role_user_relation", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "role")
@EqualsAndHashCode(of = "id")
public class UaRoleUserRelation {

    @EmbeddedId
    private UaRoleUserRelationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private UaRole role;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Column(name = "reg_user_uid", length = 50)
    private String regUserUid;
}
