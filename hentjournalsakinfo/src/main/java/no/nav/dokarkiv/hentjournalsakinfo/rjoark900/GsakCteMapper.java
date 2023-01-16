package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.Value;
import no.nav.dokarkiv.hentjournalsakinfo.common.PadUtils;
import org.apache.commons.collections4.ListUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static no.nav.dokarkiv.hentjournalsakinfo.common.PadUtils.inPaddingBase2;
import static no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalpostSqlGenerator.feilregistrertSelectionSql;

class GsakCteMapper {

	@Value
	static class GsakCte {
		String cteSql;
		Map<String, List<String>> gsakIdParams;

		boolean isGsakerExists() {
			return !gsakIdParams.isEmpty();
		}
	}

	GsakCte mapCte(List<String> gsakIds, boolean kunFeilregistrerte) {
		if (gsakIds == null || gsakIds.isEmpty()) {
			return noGsaker();
		} else if (gsakIds.size() > PadUtils.ORACLE_IN_SELECTION_MAX_ELEMENTS) {
			return moreThan1000Gsaker(gsakIds, kunFeilregistrerte);
		} else {
			return lessThan1000Gsaker(gsakIds, kunFeilregistrerte);
		}
	}

	private GsakCte noGsaker() {
		return new GsakCte("", new HashMap<>());
	}

	private GsakCte moreThan1000Gsaker(List<String> gsakIds, boolean kunFeilregistrerte) {
		List<List<String>> partitions = ListUtils.partition(gsakIds, 1000);
		HashMap<String, List<String>> gsakIdParams = new HashMap<>();
		IntStream.range(0, partitions.size()).forEach(num -> gsakIdParams.put("gsakIds" + num, inPaddingBase2(partitions.get(num))));
		return new GsakCte(generateGsakCteSql(kunFeilregistrerte, partitions.size()), gsakIdParams);
	}

	private GsakCte lessThan1000Gsaker(List<String> gsakIds, boolean kunFeilregistrerte) {
		HashMap<String, List<String>> gsakIdParams = new HashMap<>();
		gsakIdParams.put("gsakIds0", inPaddingBase2(gsakIds));
		return new GsakCte(generateGsakCteSql(kunFeilregistrerte, 0), gsakIdParams);
	}

	private String generateGsakCteSql(boolean kunFeilregistrerte, int partitions) {
		return "     gsaksaker AS\n" +
				"       (SELECT s.journalpost_id\n" +
				"        FROM t_saksrelasjon s\n" +
				"        WHERE (s.k_fagsystem = 'FS22' AND " + generateGsakSelectionSql(partitions) + ")\n" +
				"          AND " + feilregistrertSelectionSql(kunFeilregistrerte) + "\n" +
				"       ),\n";
	}

	// GSAK saker risikerer å ha flere enn 1000 saker for en bruker. 1000 i en IN seleksjon er max i Oracle.
	// Derfor deler man det opp i flere deler med (s.sak_nr_fk IN(0, 1, ...) OR s.sak_nr_fk IN(1001, 1002, ...))
	// Listen over sakene blir satt til named parameters med navn: gsakIds0, gsakIds1, ..., gsakIdsN
	private String generateGsakSelectionSql(int partitions) {
		if (partitions == 0) {
			return "s.sak_nr_fk IN (:gsakIds0)";
		} else {
			return "(" + IntStream.range(0, partitions)
					.mapToObj(num -> "s.sak_nr_fk IN (:gsakIds" + num + ")")
					.collect(Collectors.joining(" OR ")) + ")";
		}
	}
}
