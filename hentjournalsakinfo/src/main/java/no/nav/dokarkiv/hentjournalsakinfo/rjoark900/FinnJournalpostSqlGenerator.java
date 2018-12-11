package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
final class FinnJournalpostSqlGenerator {
	private FinnJournalpostSqlGenerator() {
		//ikke instansier
	}

	static String feilregistrertSelectionSql(boolean kunFeilregistrerte) {
		if (kunFeilregistrerte) {
			return "(s.feilregistrert = 1)";
		} else {
			return "(s.feilregistrert IS NULL OR (s.feilregistrert IN (:visFeilregistrert)))";
		}
	}
}
