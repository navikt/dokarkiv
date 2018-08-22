DECLARE
  already_exists EXCEPTION ;
  PRAGMA EXCEPTION_INIT ( already_exists, -955);
  PROCEDURE do_exec_handle_already_exists( p_str IN VARCHAR2)
  IS
  BEGIN
      EXECUTE IMMEDIATE p_str;
      dbms_output.put_line('Executed: ' || substr( p_str, 1, 40));
    EXCEPTION
      WHEN already_exists THEN
        dbms_output.put_line('Already Exists ' || substr( p_str, 1, 40));
  END;
  BEGIN
    do_exec_handle_already_exists('CREATE INDEX XIF4SAKRELASJON ON T_SAKSRELASJON(K_FAGSYSTEM, SAK_NR_FK)');
  END;
/