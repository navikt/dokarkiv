delete
from T_ADMINISTRATIV_ENHET
where administrativ_enhet_id = 3
  and enhet_navn = 'Nav Økonomi Stønad';

insert into T_ADMINISTRATIV_ENHET(administrativ_enhet_id, tema, dato_fom, dato_tom, enhet_navn)
values (ADMINISTRATIV_ENHET_SEQ.nextval, 'AGR', date '2010-06-22', date '2010-07-31', 'Nav Økonomi Stønad');
insert into T_ADMINISTRATIV_ENHET(administrativ_enhet_id, tema, dato_fom, dato_tom, enhet_navn)
values (ADMINISTRATIV_ENHET_SEQ.nextval, 'AGR', date '2010-08-01', date '2013-07-31', 'Nav Regnskap Mo i Rana');
insert into T_ADMINISTRATIV_ENHET(administrativ_enhet_id, tema, dato_fom, dato_tom, enhet_navn)
values (ADMINISTRATIV_ENHET_SEQ.nextval, 'AGR', date '2013-08-01', date '2099-01-01', 'Nav Økonomi Stønad');
insert into T_ADMINISTRATIV_ENHET(administrativ_enhet_id, tema, dato_fom, dato_tom, enhet_navn)
values (ADMINISTRATIV_ENHET_SEQ.nextval, 'AKT', date '2010-01-01', date '2099-01-01', 'Nav-kontor');
insert into T_ADMINISTRATIV_ENHET(administrativ_enhet_id, tema, dato_fom, dato_tom, enhet_navn)
values (ADMINISTRATIV_ENHET_SEQ.nextval, 'STO', date '2009-10-14', date '2010-07-31', 'Nav Servicesenter');
insert into T_ADMINISTRATIV_ENHET(administrativ_enhet_id, tema, dato_fom, dato_tom, enhet_navn)
values (ADMINISTRATIV_ENHET_SEQ.nextval, 'STO', date '2010-08-01', date '2013-07-31', 'Nav Regnskap Mo i Rana');
insert into T_ADMINISTRATIV_ENHET(administrativ_enhet_id, tema, dato_fom, dato_tom, enhet_navn)
values (ADMINISTRATIV_ENHET_SEQ.nextval, 'STO', date '2013-08-01', date '2099-01-01', 'Nav Økonomi Stønad');
insert into T_ADMINISTRATIV_ENHET(administrativ_enhet_id, tema, dato_fom, dato_tom, enhet_navn)
values (ADMINISTRATIV_ENHET_SEQ.nextval, 'SUP', date '2009-10-14', date '2016-12-31', 'Nav Forvaltning');
insert into T_ADMINISTRATIV_ENHET(administrativ_enhet_id, tema, dato_fom, dato_tom, enhet_navn)
values (ADMINISTRATIV_ENHET_SEQ.nextval, 'SUP', date '2017-01-01', date '2019-09-01', 'Nav Familie- og pensjonsytelser');
insert into T_ADMINISTRATIV_ENHET(administrativ_enhet_id, tema, dato_fom, dato_tom, enhet_navn)
values (ADMINISTRATIV_ENHET_SEQ.nextval, 'SUP', date '2019-09-02', date '2019-12-31', 'Nav Familie- og pensjonsytelser Sandnes');
insert into T_ADMINISTRATIV_ENHET(administrativ_enhet_id, tema, dato_fom, dato_tom, enhet_navn)
values (ADMINISTRATIV_ENHET_SEQ.nextval, 'SUP', date '2020-01-01', date '2099-01-01', 'Nav Familie- og pensjonsytelser Ålesund');
insert into T_ADMINISTRATIV_ENHET(administrativ_enhet_id, tema, dato_fom, dato_tom, enhet_navn)
values (ADMINISTRATIV_ENHET_SEQ.nextval, 'TRK', date '2010-06-22', date '2010-07-31', 'Nav Servicesenter');
insert into T_ADMINISTRATIV_ENHET(administrativ_enhet_id, tema, dato_fom, dato_tom, enhet_navn)
values (ADMINISTRATIV_ENHET_SEQ.nextval, 'TRK', date '2010-08-01', date '2013-07-31', 'Nav Regnskap Mo i Rana');
insert into T_ADMINISTRATIV_ENHET(administrativ_enhet_id, tema, dato_fom, dato_tom, enhet_navn)
values (ADMINISTRATIV_ENHET_SEQ.nextval, 'TRK', date '2013-08-01', date '2099-01-01', 'Nav Økonomi Stønad');

update T_ADMINISTRATIV_ENHET
set enhet_navn = 'Nav Kontroll'
where administrativ_enhet_id = 85
  and enhet_navn = 'Nav Kontroll til og med 2015';
