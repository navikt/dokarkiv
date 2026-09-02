DROP INDEX XIE1FIL_DETALJER ONLINE;

ALTER TABLE t_fil_detaljer
MODIFY fil_navn varchar2(255 char);
