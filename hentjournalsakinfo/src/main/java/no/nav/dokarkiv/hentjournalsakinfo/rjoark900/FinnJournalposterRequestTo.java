package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;

import java.util.List;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
public class FinnJournalposterRequestTo {
	private static final int MAKS_ELEMENTER_LOGGING = 200;

	private List<String> gsakSakIds;
	private List<String> psakSakIds;
	private String fraDato;
	private String tilDato;
	private List<JournalStatusCode> inkluderJournalStatus;
	private List<JournalpostTypeCode> inkluderJournalpostType;
	private boolean visFeilregistrerte;
	@ToString.Exclude
	private List<String> alleIdenter;
	private Integer foerste;
	private String etterPeker;

	@SuppressWarnings("unused")
	@ToString.Include
	private String gsakSakIds() {
		if (gsakSakIds.size() > MAKS_ELEMENTER_LOGGING) {
			return Stream.concat(gsakSakIds.stream().limit(MAKS_ELEMENTER_LOGGING), Stream.of("trunkert til maks " + MAKS_ELEMENTER_LOGGING + " elementer...")).toList().toString();
		} else {
			return gsakSakIds.toString();
		}
	}
}
