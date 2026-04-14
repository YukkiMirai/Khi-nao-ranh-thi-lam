package com.wibuneverdie.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ua_user_info", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@EqualsAndHashCode(of = "userUid")
public class UaUserInfo {

    @Id
    @Column(name = "user_uid", length = 50)
    private String userUid;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_uid")
    private UaUser user;

    @Column(name = "full_name", length = 50)
    private String fullName;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "verify_key", length = 50)
    private String verifyKey;

    @Column(name = "expired_verify_key")
    private LocalDateTime expiredVerifyKey;

    @Column(name = "fa_key", length = 50)
    private String faKey;

    @Column(name = "fa_enable", length = 1)
    private String faEnable;

    @Column(name = "regt_dt")
    private LocalDateTime regtDt;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @Column(name = "ten_doanh_nghiep", length = 255)
    private String tenDoanhNghiep;

    @Column(name = "so_cccd", length = 255)
    private String soCccd;

    @Column(name = "so_dkkd", length = 255)
    private String soDkkd;
}
