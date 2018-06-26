package no.nav.dokarkiv.arkiverdokumentmottak.v1.tjoark203;

import no.nav.dokarkiv.arkiverdokumentmottak.v1.to.JournalforInngaaendeForsendelseRequestTo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.journabehandling.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest;
import org.dozer.Mapper;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * JournalforInngaaendeForsendelseRequestMapper implementation
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 17.02.2017
 */
@Component
public class DefaultJournalforInngaaendeForsendelseRequestMapper {

	private Mapper dozerMapper;

	@Inject
	private KildeNavnPopulator kildeNavnPopulator;

	public JournalforInngaaendeForsendelseRequestTo map(JournalforInngaaendeForsendelseRequest request) {
		Journalpost domainJournalpost = dozerMapper.map(request.getJournalpost(), Journalpost.class);
		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(domainJournalpost, RequestContextHolder.currentRequestContext()
				.getComponentId());

		return new JournalforInngaaendeForsendelseRequestTo(domainJournalpost);
	}

	@Inject
	@Named("dozerMapper")
	public void setDozerMapper(Mapper dozerMapper) {
		this.dozerMapper = dozerMapper;
	}
}