package no.nav.dokarkiv.inngaaendejournal.v1;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.READ_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.dokarkiv.inngaaendejournal.v1.common.JournalpostManglerTo;
import no.nav.dokarkiv.inngaaendejournal.v1.exceptions.JournalpostKanIkkeBehandlesException;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.HentInngaaendeJournalpostResponseMapper;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.HentInngaaendeJournalpostService;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.InngaaendeJournalpostTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark057.UtledJournalfoeringsbehovResponseMapper;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark057.UtledJournalfoeringsbehovService;
import no.nav.freg.abac.core.annotation.Abac;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.InngaaendeJournalV1;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostKanIkkeBehandles;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.feil.JournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.feil.JournalpostIkkeInngaeende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.feil.JournalpostKanIkkeBehandles;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.feil.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.feil.UgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostResponse;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * @author Stig Strøm, Acando
 */
@Slf4j
@Component
public class InngaaendeJournalProvider implements InngaaendeJournalV1 {

	private static final String INNGAAENDE_JOURNAL_V1 = "InngaaendeJournalV1";
	public static final String HENT_JOURNALPOST = INNGAAENDE_JOURNAL_V1 + ".hentJournalpost";
	public static final String UTLED_JOURNALFOERINGSBEHOV = INNGAAENDE_JOURNAL_V1 + ".utledJournalfoeringsbehov";
	private final HentInngaaendeJournalpostResponseMapper hentInngaaendeJournalpostResponseMapper = new HentInngaaendeJournalpostResponseMapper();
	private final UtledJournalfoeringsbehovResponseMapper utledJournalfoeringsbehovResponseMapper = new UtledJournalfoeringsbehovResponseMapper();
	private final InngaaendeJournalFaultInfoPopulator faultInfoPopulator = new InngaaendeJournalFaultInfoPopulator();
	@Inject
	private HentInngaaendeJournalpostService inngaaendeJournalpostService;
	@Inject
	private UtledJournalfoeringsbehovService utledJournalfoeringsbehovService;
	@Inject
	private AbacSecurityService abacSecurityService;

	@Override
	public void ping() {
		//noop
	}

	@Transactional(readOnly = true)
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	@Override
	public HentJournalpostResponse hentJournalpost(HentJournalpostRequest request)
			throws HentJournalpostJournalpostIkkeFunnet, HentJournalpostJournalpostIkkeInngaaende,
			HentJournalpostSikkerhetsbegrensning, HentJournalpostUgyldigInput {
		final String journalpostId = request.getJournalpostId();

		try {
			assertAccessToHentJournalpost(journalpostId);
			InngaaendeJournalpostTo inngaaendeJournalpostTo = inngaaendeJournalpostService.hentJournalpost(journalpostId);
			log.info("tjoark056 hentet inngaaende journalpost med journalpostId={}", journalpostId);
			return hentInngaaendeJournalpostResponseMapper.map(inngaaendeJournalpostTo);
		} catch (UgyldigInputException | IllegalArgumentException e) {
			throw new HentJournalpostUgyldigInput(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new UgyldigInput(), e, HENT_JOURNALPOST), e);
		} catch (JournalpostIkkeFunnetException e) {
			throw new HentJournalpostJournalpostIkkeFunnet(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new JournalpostIkkeFunnet(), e, HENT_JOURNALPOST), e);
		} catch (JournalpostIkkeInngaaendeException e) {
			throw new HentJournalpostJournalpostIkkeInngaaende(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new JournalpostIkkeInngaeende(), e, HENT_JOURNALPOST), e);
		}
	}

	@Transactional(readOnly = true)
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	@Override
	public UtledJournalfoeringsbehovResponse utledJournalfoeringsbehov(UtledJournalfoeringsbehovRequest request)
			throws UtledJournalfoeringsbehovJournalpostIkkeFunnet, UtledJournalfoeringsbehovJournalpostIkkeInngaaende,
			UtledJournalfoeringsbehovJournalpostKanIkkeBehandles, UtledJournalfoeringsbehovSikkerhetsbegrensning,
			UtledJournalfoeringsbehovUgyldigInput {
		final String journalpostId = request.getJournalpostId();
		try {
			assertAccessToUtledJournalfoeringsbehov(journalpostId);
			JournalpostManglerTo journalpostManglerTo = utledJournalfoeringsbehovService.utledJournalfoeringsbehov(journalpostId);
			log.info("tjoark057 utledet journalføringsbehov for journalpostId={}", journalpostId);
			return utledJournalfoeringsbehovResponseMapper.map(journalpostManglerTo);
		} catch (UgyldigInputException | IllegalArgumentException e) {
			throw new UtledJournalfoeringsbehovUgyldigInput(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new UgyldigInput(), e, UTLED_JOURNALFOERINGSBEHOV), e);
		} catch (JournalpostIkkeFunnetException e) {
			throw new UtledJournalfoeringsbehovJournalpostIkkeFunnet(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new JournalpostIkkeFunnet(), e, UTLED_JOURNALFOERINGSBEHOV), e);
		} catch (JournalpostIkkeInngaaendeException e) {
			throw new UtledJournalfoeringsbehovJournalpostIkkeInngaaende(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new JournalpostIkkeInngaeende(), e, UTLED_JOURNALFOERINGSBEHOV), e);
		} catch (JournalpostKanIkkeBehandlesException e) {
			throw new UtledJournalfoeringsbehovJournalpostKanIkkeBehandles(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new JournalpostKanIkkeBehandles(), e, UTLED_JOURNALFOERINGSBEHOV), e);
		}
	}

	private void assertAccessToHentJournalpost(String journalpostId) throws HentJournalpostSikkerhetsbegrensning {
		try {
			inngaaendeJournalpostService.assertJournalpostIdIsNotNull(journalpostId);
			abacSecurityService.assertAccessToJournalpost(journalpostId);
		} catch (AuthorizationException e) {
			throw new HentJournalpostSikkerhetsbegrensning(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new Sikkerhetsbegrensning(), e, HENT_JOURNALPOST), e);
		}
	}

	private void assertAccessToUtledJournalfoeringsbehov(String journalpostId) throws UtledJournalfoeringsbehovSikkerhetsbegrensning {
		try {
			inngaaendeJournalpostService.assertJournalpostIdIsNotNull(journalpostId);
			abacSecurityService.assertAccessToJournalpost(journalpostId);
		} catch (AuthorizationException e) {
			throw new UtledJournalfoeringsbehovSikkerhetsbegrensning(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new Sikkerhetsbegrensning(), e, UTLED_JOURNALFOERINGSBEHOV), e);
		}
	}
}
