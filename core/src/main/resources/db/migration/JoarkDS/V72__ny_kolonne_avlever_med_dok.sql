ALTER TABLE T_K_FAGOMRADE add (
         AVLEVER_MED_DOK       CHAR
        );

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'AAP';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'AAR';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'AGR';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'BAR';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'BID';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'BIL';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'DAG';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'ENF';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'ERS';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'EYB';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'EYO';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'FAR';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'FEI';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'FOR';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'FOS';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'FRI';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'FUL';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'GEN';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'GRA';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'GRU';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'HEL';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'HJE';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'IAR';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'IND';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'KON';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'KTR';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'MED';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'MOB';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'OMS';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'OPA';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'OPP';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'PEN';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'PER';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'REH';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'REK';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'RVE';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'SAK';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'SAP';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'SER';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'STO';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'SUP';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'SYK';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'SYM';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'SAA';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'TIL';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'TRK';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'TRY';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'TSO';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'TSR';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'UFM';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'UFO';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '0',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'VEN';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'YRA';

update T_K_FAGOMRADE set AVLEVER_MED_DOK = '1',
                         dato_endret = current_timestamp,
                         endret_av = 'MMA-7206'
where K_FAGOMRADE = 'YRK';