package com.wibuneverdie.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "ua_menu", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"upperMenu", "children"})
@EqualsAndHashCode(of = "menuId")
public class UaMenu {

    @Id
    @Column(name = "menu_id", length = 255, nullable = false)
    private String menuId;

    /** Null khi là menu gốc (root) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upper_menu_id")
    private UaMenu upperMenu;

    @OneToMany(mappedBy = "upperMenu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UaMenu> children;

    @Column(name = "menu_name", length = 255)
    private String menuName;

    @Column(name = "link_uri", length = 500)
    private String linkUri;

    @Column(name = "display_order")
    private Long displayOrder;

    @Column(name = "menu_type", length = 255)
    private String menuType;

    @Column(name = "use_yn", length = 255)
    private String useYn;

    @Column(name = "reg_dt")
    private OffsetDateTime regDt;

    @Column(name = "last_mod_dt")
    private OffsetDateTime lastModDt;

    @Column(name = "reg_user_uid", length = 255)
    private String regUserUid;

    @Column(name = "last_mod_user_uid", length = 255)
    private String lastModUserUid;

    @Column(name = "menu_name_en", length = 50)
    private String menuNameEn;

    @Column(name = "menu_name_vi", length = 50)
    private String menuNameVi;

    @Column(name = "lev")
    private Long lev;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "remark", length = 255)
    private String remark;
}
