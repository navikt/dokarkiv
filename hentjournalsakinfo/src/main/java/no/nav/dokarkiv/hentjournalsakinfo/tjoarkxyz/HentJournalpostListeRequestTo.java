package no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HentJournalpostListeRequestTo {

	@Builder.Default
	private final List<String> gsakSakIdList = new ArrayList<>();
	@Builder.Default
	private final List<String> psakSakIdList = new ArrayList<>();
}
