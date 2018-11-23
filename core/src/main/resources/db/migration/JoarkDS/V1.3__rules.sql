CREATE PACKAGE kode_6_7 AS
  PROCEDURE purge_bruker(p_bruker_id IN T_BRUKER.bruker_id%TYPE);
  PROCEDURE delete_journalpost(p_journalpost_id IN T_JOURNALPOST.journalpost_id%TYPE);
END kode_6_7;
/

CREATE PACKAGE BODY kode_6_7 AS

  PROCEDURE purge_bruker(p_bruker_id IN T_BRUKER.bruker_id%TYPE)
  IS
    v_journalpost_id T_JOURNALPOST.journalpost_id%TYPE;
    v_count          PLS_INTEGER := 0;
    CURSOR jpc IS
      SELECT DISTINCT (journalpost_id)
      FROM (SELECT b.journalpost_id
            FROM T_BRUKER b
            WHERE b.bruker_id = p_bruker_id)
      UNION ALL
      (SELECT j.journalpost_id
       FROM T_JOURNALPOST j
       WHERE j.avsend_mottak_id = p_bruker_id);
    BEGIN
      DBMS_OUTPUT.PUT_LINE('Sletter bruker: ' || p_bruker_id);
      OPEN jpc;
      LOOP
        FETCH jpc INTO v_journalpost_id;
        EXIT WHEN jpc%NOTFOUND;
        UPDATE T_DOKUMENT_INFO
        SET orig_journalpost_id = NULL
        WHERE orig_journalpost_id = v_journalpost_id;
      END LOOP;
      CLOSE jpc;
      OPEN jpc;
      LOOP
        FETCH jpc INTO v_journalpost_id;
        EXIT WHEN jpc%NOTFOUND;
        delete_journalpost(v_journalpost_id);
        v_count := v_count + 1;
      END LOOP;
      DBMS_OUTPUT.PUT_LINE(CHR(9) || 'Antall journalposter: ' || v_count);
      CLOSE jpc;
    END;

  PROCEDURE delete_journalpost(p_journalpost_id IN T_JOURNALPOST.journalpost_id%TYPE)
  IS
    TYPE DOKUMENT_INFO_ID_TABLE IS TABLE OF T_DOKUMENT_INFO.dokument_info_id%TYPE;
    TYPE FIL_UUID_TABLE IS TABLE OF T_FIL_DETALJER.fil_uuid%TYPE;
    l_dokument_info_ids DOKUMENT_INFO_ID_TABLE;
    l_fil_uuids         FIL_UUID_TABLE;
    BEGIN
      SELECT dokument_info_id
      BULK COLLECT INTO l_dokument_info_ids
      FROM T_JP_DOK_INFO_REL
      WHERE journalpost_id = p_journalpost_id;

      DELETE FROM T_SAKSRELASJON
      WHERE journalpost_id = p_journalpost_id;
      DELETE FROM T_BRUKER
      WHERE journalpost_id = p_journalpost_id;
      DELETE FROM T_KRYSSREFERANSE
      WHERE journalpost_id = p_journalpost_id;
      DELETE FROM T_RETUR_INFO
      WHERE journalpost_id = p_journalpost_id;
      DELETE FROM T_DOK_URL_INFO
      WHERE journalpost_id = p_journalpost_id;

      FOR idx IN 1..l_dokument_info_ids.COUNT
      LOOP
        SELECT fil_uuid
        BULK COLLECT INTO l_fil_uuids
        FROM T_FIL_DETALJER
        WHERE dokument_info_id = l_dokument_info_ids(idx);
        DELETE FROM T_SKANNET_INNHOLD
        WHERE dokument_info_id = l_dokument_info_ids(idx);
        DELETE FROM T_FIL_DETALJER
        WHERE dokument_info_id = l_dokument_info_ids(idx);
        DELETE FROM T_DOK_INFO_TILLEGG
        WHERE dokument_info_id = l_dokument_info_ids(idx);
        DELETE FROM T_JP_DOK_INFO_REL
        WHERE dokument_info_id = l_dokument_info_ids(idx);
        DELETE FROM T_DOKUMENT_INFO
        WHERE dokument_info_id = l_dokument_info_ids(idx);

        FOR fidx IN 1..l_fil_uuids.COUNT
        LOOP
          DELETE FROM T_DOKUMENT_FIL
          WHERE fil_uuid = l_fil_uuids(fidx);
        END LOOP;
      END LOOP;

      DELETE FROM T_JP_DOK_INFO_REL
      WHERE journalpost_id = p_journalpost_id;
      DELETE FROM T_JOURNALPOST
      WHERE journalpost_id = p_journalpost_id;

      -- Skriv ut rapport
      DBMS_OUTPUT.PUT_LINE(CHR(9) || 'Slettet journalpost_id: ' || p_journalpost_id);
      FOR idx IN 1..l_dokument_info_ids.COUNT
      LOOP
        DBMS_OUTPUT.PUT_LINE(CHR(9) || CHR(9) || 'Slettet dokument_info_id: ' || l_dokument_info_ids(idx));
      END LOOP;
    END;

END kode_6_7;
/

CREATE FUNCTION SQUIRREL_GET_ERROR_OFFSET(query IN VARCHAR2)
  RETURN NUMBER AUTHID CURRENT_USER IS l_theCursor INTEGER DEFAULT dbms_sql.open_cursor;
  l_status                                         INTEGER;
  BEGIN BEGIN dbms_sql.parse(l_theCursor, query, dbms_sql.native);
    EXCEPTION WHEN OTHERS THEN l_status := dbms_sql.last_error_position;
  END;
    dbms_sql.close_cursor(l_theCursor);
    RETURN l_status;
  END;
/
