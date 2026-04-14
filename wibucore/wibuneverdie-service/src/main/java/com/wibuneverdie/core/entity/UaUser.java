package com.wibuneverdie.core.entity;

import com.wibuneverdie.core.generator.ProcGenId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ua_user", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "userInfo")
@EqualsAndHashCode(of = "userUid")
public class UaUser {

    @Id
    @ProcGenId
    @Column(name = "user_uid", length = 50)
    private String userUid;

    @Column(name = "user_id", length = 255, unique = true)
    private String userId;

    @Column(name = "pwd", length = 255)
    private String pwd;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "auth_provider", length = 50)
    private String authProvider;

    @Column(name = "type", length = 10)
    private String type;

    @Column(name = "full_name", length = 50)
    private String fullName;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 50)
    private String email;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private UaUserInfo userInfo;
}
