create table public.ua_user
(
    user_uid      varchar(50) default proc_gen_id() not null
        constraint ua_user_pk
            primary key,
    user_id       varchar(255)
        constraint ua_user_pk2
            unique,
    pwd           varchar(255),
    status        varchar(50),
    auth_provider varchar(50),
    type          varchar(10),
    full_name     varchar(50),
    phone         varchar(50),
    email         varchar(50)
);

alter table public.ua_user
    owner to admin;

create table public.ua_user_info
(
    user_uid           varchar(50)
        constraint ua_user_info_fk_1
            references public.ua_user,
    full_name          varchar(50),
    phone              varchar(50),
    email              varchar(50),
    verify_key         varchar(50),
    expired_verify_key timestamp,
    fa_key             varchar(50),
    fa_enable          varchar(1),
    regt_dt            timestamp,
    dia_chi            varchar(255),
    ten_doanh_nghiep   varchar(255),
    so_cccd            varchar(255),
    so_dkkd            varchar(255)
);

alter table public.ua_user_info
    owner to admin;

create table public.ua_menu
(
    menu_id           varchar(255) not null
        constraint ua_menu_pk
            primary key,
    upper_menu_id     varchar(255) default NULL::character varying
        constraint ua_menu_to_ua_menu_fk_1
            references public.ua_menu
            on update cascade on delete cascade,
    menu_name         varchar(255) default NULL::character varying,
    link_uri          varchar(500) default NULL::character varying,
    display_order     bigint,
    menu_type         varchar(255) default NULL::character varying,
    use_yn            varchar(255) default NULL::character varying,
    reg_dt            timestamp with time zone,
    last_mod_dt       timestamp with time zone,
    reg_user_uid      varchar(255) default NULL::character varying,
    last_mod_user_uid varchar(255) default NULL::character varying,
    menu_name_en      varchar(50)  default NULL::character varying,
    menu_name_vi      varchar(50)  default NULL::character varying,
    lev               bigint,
    description       varchar(255) default NULL::character varying,
    remark            varchar(255)
);

comment on column public.ua_menu.upper_menu_id is 'if upper_menu_id is null then it is root menu';

alter table public.ua_menu
    owner to admin;

create table public.ua_role
(
    role_id           varchar(255)                                not null
        constraint ua_role_pk
            primary key,
    name              varchar(100)                                not null,
    admin_role_yn     varchar(1)   default 'N'::character varying not null,
    reg_dt            timestamp(6) default now()                  not null,
    last_mod_dt       timestamp(6),
    reg_user_uid      varchar(50),
    last_mod_user_uid varchar(50),
    description       varchar(255),
    use_yn            varchar(255) default 'Y'::character varying,
    level             integer      default 9999
);

comment on table public.ua_role is 'User roles with hierarchical levels';

comment on column public.ua_role.admin_role_yn is 'Y if this is an admin role, N otherwise';

comment on column public.ua_role.use_yn is 'Y if role is active, N if inactive';

comment on column public.ua_role.level is 'Role level (lower number = higher privilege)';

alter table public.ua_role
    owner to admin;

create index idx_ua_role_level
    on public.ua_role (level);

create index idx_ua_role_use_yn
    on public.ua_role (use_yn);

create table public.ua_role_menu_relation
(
    role_id      varchar(255) not null
        constraint ua_role_to_ua_role_menu_relation_fk_1
            references public.ua_role
            on update cascade on delete cascade,
    menu_id      varchar(255) not null,
    reg_dt       timestamp with time zone,
    reg_user_uid varchar(255) default NULL::character varying,
    exc_dn_yn    varchar(255) default NULL::character varying,
    mng_yn       varchar(255) default NULL::character varying,
    mod_yn       varchar(255) default NULL::character varying,
    pnt_yn       varchar(255) default NULL::character varying,
    read_yn      varchar(255) default NULL::character varying,
    wrt_yn       varchar(255) default NULL::character varying,
    del_yn       varchar(255) default NULL::character varying,
    constraint ua_role_menu_relation_pk
        primary key (role_id, menu_id)
);

alter table public.ua_role_menu_relation
    owner to admin;

create table public.ua_role_user_relation
(
    role_id      varchar(255)               not null
        constraint ua_role_user_relation_fk_1
            references public.ua_role
            on delete cascade,
    user_uid     varchar(50)                not null,
    reg_dt       timestamp(6) default now() not null,
    reg_user_uid varchar(50),
    constraint ua_role_user_relation_pk
        primary key (role_id, user_uid)
);

comment on table public.ua_role_user_relation is 'User-Role relationship table';

alter table public.ua_role_user_relation
    owner to admin;

create index idx_ua_role_user_relation_role_id
    on public.ua_role_user_relation (role_id);

create index idx_ua_role_user_relation_user_uid
    on public.ua_role_user_relation (user_uid);

create table public.patients_database
(
    patient_id       serial
        primary key,
    photo            text,
    last_name        text    not null,
    first_name       text    not null,
    e_mail_address   text,
    walk_in_date     date,
    mobile_phone     text    not null,
    address          text,
    special_remarks  text,
    next_appointment date,
    schedule_at      text,
    diagnosis        text,
    attachments      text,
    projected_bill   text,
    age              integer not null,
    sex              text    not null,
    notes            text
);

alter table public.patients_database
    owner to admin;

create index idx_patient_phone
    on public.patients_database (mobile_phone);

create index idx_patient_name
    on public.patients_database (last_name, first_name);

create unique index unique_patient_mobile_phone
    on public.patients_database (mobile_phone);

