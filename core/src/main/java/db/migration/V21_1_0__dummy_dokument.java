package db.migration;

import static no.nav.dokarkiv.core.repository.DokumentFilSkjermetRepository.FIL_UUID_DUMMY_DOKUMENT_KASSERT;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class V21_1_0__dummy_dokument implements JdbcMigration {
	/**
	 * Executes this db.migration. The execution will automatically take place within a transaction, when the underlying
	 * database supports it.
	 *
	 * @param connection The connection to use to execute statements.
	 * @throws Exception when the db.migration failed.
	 */
	@Override
	public void migrate(Connection connection) throws Exception {
		String query;

		if (isFalse(isDummyDocumentExists(connection))) {
			query = "INSERT INTO T_DOKUMENT_FIL (DOKUMENT_FIL_ID, FIL, FIL_UUID, DATO_OPPRETTET, OPPRETTET_AV, DATO_ENDRET," +
					"                              ENDRET_AV, VERSJON, OPPRETTET_KILDE_NAVN, ENDRET_KILDE_NAVN)" +
					"  VALUES (T_DOKUMENT_FIL_SEQ.NEXTVAL, ?, ?," +
					"          sysdate, 'FLYWAY', null, null, 0," +
					"          'FLYWAY', null)";
		} else {
			query = "update T_DOKUMENT_FIL set FIL=? where FIL_UUID=?";
		}

		addOrUpdateDummyDocument(connection, query);
	}

	private void addOrUpdateDummyDocument(Connection connection, String query) throws Exception {
		try(InputStream in = new ClassPathResource("dummy_dokument_kassert.pdf").getInputStream();
			PreparedStatement pstmt = connection.prepareStatement(query)){
			pstmt.setBlob(1, in);
			pstmt.setString(2, FIL_UUID_DUMMY_DOKUMENT_KASSERT);
			pstmt.execute();
			connection.commit();
		}
	}

	private boolean isDummyDocumentExists(Connection connection) throws Exception {
		try (PreparedStatement existingFile = connection.prepareStatement("select 'exists' from T_DOKUMENT_FIL where FIL_UUID=?")) {
			existingFile.setString(1, FIL_UUID_DUMMY_DOKUMENT_KASSERT);
			try (ResultSet resultSet = existingFile.executeQuery()) {
				return resultSet.next();
			}
		}
	}
}
