update T_K_FAGOMRADE set DATO_ENDRET = systimestamp, ENDRET_AV = 'MMA-8716', K_BEVARINGSTID = '10_AAR_ETTER_BRUKERS_DOED'
                     where K_FAGOMRADE in ('DAG', 'GRA', 'HEL', 'HJE', 'IND', 'OMS', 'OPP', 'REH', 'SAP', 'SUP', 'SYK', 'TSO', 'TSR', 'VEN', 'YRA');
update T_K_FAGOMRADE set DATO_ENDRET = systimestamp, ENDRET_AV = 'MMA-8716', K_BEVARINGSTID = '25_AAR_ETTER_BRUKERS_DOED'
                     where K_FAGOMRADE in ('MED');
update T_K_FAGOMRADE set DATO_ENDRET = systimestamp, ENDRET_AV = 'MMA-8716', K_BEVARINGSTID = '10_AAR_ETTER_AVSLUTTET_SAK'
                     where K_FAGOMRADE in ('SAK');
