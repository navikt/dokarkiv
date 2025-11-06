create table T_ADMINISTRATIV_ENHET
(
    ADMINISTRATIV_ENHET_ID NUMBER(19)    not null primary key,
    TEMA                   varchar2(128 char) not null,
    DATO_FOM               DATE not null,
    DATO_TOM               DATE not null,
    ENHET_NAVN             varchar2(512 char) not null,
    constraint FK_TEMA foreign key (TEMA) references T_K_FAGOMRADE (K_FAGOMRADE)
);
