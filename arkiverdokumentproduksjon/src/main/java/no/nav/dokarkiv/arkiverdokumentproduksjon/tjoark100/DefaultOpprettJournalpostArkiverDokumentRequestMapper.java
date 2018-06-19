package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentRequest;
import org.dozer.Mapper;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implementation of OpprettJournalpostArkiverDokumentRequestMapper
 *
 * @author Stig Strøm
 */
@Component
public class DefaultOpprettJournalpostArkiverDokumentRequestMapper implements
		OpprettJournalpostArkiverDokumentRequestMapper {

	private Mapper dozerMapper;

	@Inject
	private KildeNavnPopulator kildeNavnPopulator;

	@Override
	public OpprettJournalpostArkiverDokumentRequestTo map(OpprettJournalpostArkiverDokumentRequest wsRequest) {
		Journalpost domainJournalpost = dozerMapper.map(wsRequest.getJournalpost(), Journalpost.class);
		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(domainJournalpost, RequestContextHolder
				.currentRequestContext().getComponentId());

		return new OpprettJournalpostArkiverDokumentRequestTo(domainJournalpost, wsRequest.isFerdigstillJournalpost());
	}

	@Inject
	@Named("dozerMapper")
	public void setDozerMapper(Mapper dozerMapper) {
		this.dozerMapper = dozerMapper;
	}

}
