package com.wibuneverdie.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ua_role", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "roleId")
public class UaRole {

    @Id
    @Column(name = "role_id", length = 255, nullable = false)
    private String roleId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /** Y nếu là admin role, N nếu không */
    @Column(name = "admin_role_yn", length = 1, nullable = false)
    @Builder.Default
    private String adminRoleYn = "N";

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Column(name = "last_mod_dt")
    private LocalDateTime lastModDt;

    @Column(name = "reg_user_uid", length = 50)
    private String regUserUid;

    @Column(name = "last_mod_user_uid", length = 50)
    private String lastModUserUid;

    @Column(name = "description", length = 255)
    private String description;

    /** Y nếu role đang hoạt động, N nếu không */
    @Column(name = "use_yn", length = 255)
    @Builder.Default
    private String useYn = "Y";

    /** Cấp độ role — số càng nhỏ quyền hạn càng cao */
    @Column(name = "level")
    @Builder.Default
    private Integer level = 9999;
}
