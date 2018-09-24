package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.DokumentInfoIdEntry;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * @author Torgeir Cook
 */
@Component
public class OpprettJournalpostArkiverDokumenterResponseMapper {

	public OpprettJournalpostArkiverDokumenterResponse map(OpprettJournalpostArkiverDokumenterResponseTo domainResponse) {
		return new OpprettJournalpostArkiverDokumenterResponse()
				.withJournalpostId(domainResponse.getJournalpostId())
				.withDokumentInfoIdMap(domainResponse.getDokumentInfoIds().stream()
						.map(to -> new DokumentInfoIdEntry()
								.withFilreferanse(to.getFilreferanse())
								.withDokumentInfoId(to.getDokumentInfoId()))
						.collect(Collectors.toList()));
	}
}
