package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@NoArgsConstructor
public class FinnJournalposterRequestTo {
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
	/**
	 * @deprecated Kan fjernes når saf ikke sender denne pga jackson setting: fail-on-unknown-properties=true
	 * @since 4.10.0
	 */
	@Deprecated(since = "4.10.0", forRemoval = true)
	private Integer siste;
	/**
	 * @deprecated Kan fjernes når saf ikke sender denne pga jackson setting: fail-on-unknown-properties=true
	 * @since 4.10.0
	 */
	@Deprecated(since = "4.10.0", forRemoval = true)
	private String foerPeker;
}
