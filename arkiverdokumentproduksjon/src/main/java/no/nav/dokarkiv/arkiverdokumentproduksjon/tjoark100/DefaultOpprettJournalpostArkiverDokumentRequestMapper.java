package no.nav.dokarkiv.map;

import no.nav.domain.dok.joark.Journalpost;
import no.nav.provider.dok.joark.nsb.map.OpprettJournalpostArkiverDokumentRequestMapper;
import no.nav.service.dok.joark.journalbehandling.KildeNavnPopulator;
import no.nav.service.dok.joark.nsb.to.OpprettJournalpostArkiverDokumentRequestTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentRequest;
import no.stelvio.common.context.RequestContextHolder;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implementation of OpprettJournalpostArkiverDokumentRequestMapper
 *
 * @author Stig Str?m
 */
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
