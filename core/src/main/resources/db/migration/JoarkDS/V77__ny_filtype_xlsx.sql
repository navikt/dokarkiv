select 'XLSX',
       'XLSX',
       to_date('01.08.2024', 'DD.MM.RRRR'),
       null,
       '1',
       current_timestamp,
       'MMA-7563',
       current_timestamp,
       'MMA-7563'
from dual
where not exists(select 1 from t_k_fil_t where k_fil_t = 'XLSX');