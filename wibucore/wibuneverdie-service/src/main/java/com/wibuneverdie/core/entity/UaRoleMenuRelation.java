package com.wibuneverdie.core.entity;

import com.wibuneverdie.core.entity.embeddable.UaRoleMenuRelationId;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ua_role_menu_relation", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "role")
@EqualsAndHashCode(of = "id")
public class UaRoleMenuRelation {

    @EmbeddedId
    private UaRoleMenuRelationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private UaRole role;

    @Column(name = "reg_dt")
    private OffsetDateTime regDt;

    @Column(name = "reg_user_uid", length = 255)
    private String regUserUid;

    @Column(name = "exc_dn_yn", length = 255)
    private String excDnYn;

    @Column(name = "mng_yn", length = 255)
    private String mngYn;

    @Column(name = "mod_yn", length = 255)
    private String modYn;

    @Column(name = "pnt_yn", length = 255)
    private String pntYn;

    @Column(name = "read_yn", length = 255)
    private String readYn;

    @Column(name = "wrt_yn", length = 255)
    private String wrtYn;

    @Column(name = "del_yn", length = 255)
    private String delYn;
}
