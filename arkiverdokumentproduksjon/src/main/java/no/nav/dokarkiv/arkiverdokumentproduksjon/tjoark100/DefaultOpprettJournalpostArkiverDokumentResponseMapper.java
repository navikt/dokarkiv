package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import no.nav.dokarkiv.map.OpprettJournalpostArkiverDokumentResponseMapper;
import no.nav.provider.dok.joark.nsb.map.OpprettJournalpostArkiverDokumentResponseMapper;
import no.nav.service.dok.joark.nsb.to.OpprettJournalpostArkiverDokumentResponseTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentResponse;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implementation of OpprettJournalpostAkiverDokumentResponseMapper
 *
 * @author Torgeir Cook
 */
public class DefaultOpprettJournalpostArkiverDokumentResponseMapper implements
		OpprettJournalpostArkiverDokumentResponseMapper {

	private Mapper dozerMapper;

	@Override
	public OpprettJournalpostArkiverDokumentResponse map(OpprettJournalpostArkiverDokumentResponseTo domainResponse) {
		OpprettJournalpostArkiverDokumentResponse wsResponse =
				dozerMapper.map(domainResponse, OpprettJournalpostArkiverDokumentResponse.class);
		return wsResponse;
	}

	@Inject
	@Named("dozerMapper")
	public void setDozerMapper(Mapper dozerMapper) {
		this.dozerMapper = dozerMapper;
	}
}
