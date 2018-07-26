package no.nav.dokarkiv.behandleinngaaendejournal.v1;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.DokumentInfoIkkeTilknyttetJournalpostException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.FerdigstillingIkkeMuligException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.OppdaterJournalpostIkkeMuligException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.UgyldigInputException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.OppdaterJournalpostRequestMapper;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.OppdaterJournalpostService;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.OppdaterJournalpostRequestTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark067.FerdigstillJournalfoeringService;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark067.FerdigstillJournalfoeringTo;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.freg.abac.core.annotation.Abac;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringFerdigstillingIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostOppdateringIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.feil.FerdigstillingIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.feil.JournalpostIkkeInngaeende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.feil.ObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.feil.OppdateringIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.feil.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.feil.UgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.FerdigstillJournalfoeringRequest;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.OppdaterJournalpostRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.inject.Inject;

/**
 * @author Stig Strøm, Acando
 */
@Component
public class BehandleInngaaendeJournalProvider implements BehandleInngaaendeJournalV1 {
	private static final String BEHANDLE_INNGAAENDE_JOURNAL_V1 = "BehandleInngaaendeJournalV1";
	public static final String FERDIGSTILL_JOURNALFOERING = BEHANDLE_INNGAAENDE_JOURNAL_V1 + ".ferdigstillJournalfoering";
	public static final String OPPDATER_JOURNALPOST = BEHANDLE_INNGAAENDE_JOURNAL_V1 + ".oppdaterJournalpost";

	private static final String PING = BEHANDLE_INNGAAENDE_JOURNAL_V1 + ".ping";

	@Inject
	private FerdigstillJournalfoeringService ferdigstillJournalfoeringService;
	@Inject
	private OppdaterJournalpostService oppdaterJournalpostService;
	@Inject
	private OppdaterJournalpostRequestMapper oppdaterJournalpostRequestMapper;
	@Inject
	private AbacSecurityService abacSecurityService;

	private final BehandleInngaaendeJournalFaultInfoPopulator faultInfoPopulator = new BehandleInngaaendeJournalFaultInfoPopulator();

	@Transactional(rollbackFor = {FerdigstillJournalfoeringFerdigstillingIkkeMulig.class, FerdigstillJournalfoeringJournalpostIkkeInngaaende.class,
			FerdigstillJournalfoeringObjektIkkeFunnet.class, FerdigstillJournalfoeringSikkerhetsbegrensning.class, FerdigstillJournalfoeringUgyldigInput.class})
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	@Override
	public void ferdigstillJournalfoering(FerdigstillJournalfoeringRequest request) throws FerdigstillJournalfoeringFerdigstillingIkkeMulig,
			FerdigstillJournalfoeringJournalpostIkkeInngaaende, FerdigstillJournalfoeringObjektIkkeFunnet, FerdigstillJournalfoeringSikkerhetsbegrensning,
			FerdigstillJournalfoeringUgyldigInput {

		try {
			Assert.notNull(request, "Input request is null.");
			Assert.hasLength(request.getJournalpostId(), "JournalpostId is null or empty.");
			FerdigstillJournalfoeringTo ferdigstillJournalfoeringTo = FerdigstillJournalfoeringTo.builder()
					.journalpostId(request.getJournalpostId())
					.enhetId(request.getEnhetId())
					.build();
			ferdigstillJournalfoeringTo.validate();
			assertAccessToFerdigstillJournalfoering(request);
			ferdigstillJournalfoeringService.ferdigstillJournalfoering(ferdigstillJournalfoeringTo);
		} catch (UgyldigInputException | IllegalArgumentException e) {
			throw new FerdigstillJournalfoeringUgyldigInput(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new UgyldigInput(), e, FERDIGSTILL_JOURNALFOERING), e);
		} catch (FerdigstillingIkkeMuligException e) {
			throw new FerdigstillJournalfoeringFerdigstillingIkkeMulig(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new FerdigstillingIkkeMulig(), e, FERDIGSTILL_JOURNALFOERING), e);
		} catch (JournalpostIkkeFunnetException e) {
			throw new FerdigstillJournalfoeringObjektIkkeFunnet(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new ObjektIkkeFunnet(), e, FERDIGSTILL_JOURNALFOERING), e);
		} catch (JournalpostIkkeInngaaendeException e) {
			throw new FerdigstillJournalfoeringJournalpostIkkeInngaaende(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new JournalpostIkkeInngaeende(), e, FERDIGSTILL_JOURNALFOERING), e);
		}
	}

	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	@Transactional(rollbackFor = {OppdaterJournalpostJournalpostIkkeInngaaende.class, OppdaterJournalpostObjektIkkeFunnet.class
			, OppdaterJournalpostOppdateringIkkeMulig.class, OppdaterJournalpostSikkerhetsbegrensning.class, OppdaterJournalpostUgyldigInput.class})
	@Override
	public void oppdaterJournalpost(OppdaterJournalpostRequest request) throws OppdaterJournalpostJournalpostIkkeInngaaende,
			OppdaterJournalpostObjektIkkeFunnet, OppdaterJournalpostOppdateringIkkeMulig,
			OppdaterJournalpostSikkerhetsbegrensning, OppdaterJournalpostUgyldigInput {

		try {
			Assert.notNull(request, "Input request is null.");
			Assert.notNull(request.getInngaaendeJournalpost(), "Journalpost is missing");
			Assert.hasLength(request.getInngaaendeJournalpost().getJournalpostId(), "JournalpostId is null or empty.");
			OppdaterJournalpostRequestTo requestTo = oppdaterJournalpostRequestMapper.map(request);
			assertAccessToOppdaterJournalpost(request);
			oppdaterJournalpostService.oppdaterJournalpost(requestTo);
		} catch (UgyldigInputException | IllegalArgumentException e) {
			throw new OppdaterJournalpostUgyldigInput(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new UgyldigInput(), e, OPPDATER_JOURNALPOST), e);
		} catch (OppdaterJournalpostIkkeMuligException | JournalpostIkkeMidlertidigException e) {
			throw new OppdaterJournalpostOppdateringIkkeMulig(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new OppdateringIkkeMulig(), e, OPPDATER_JOURNALPOST), e);
		} catch (JournalpostIkkeFunnetException | DokumentInfoIkkeTilknyttetJournalpostException e) {
			throw new OppdaterJournalpostObjektIkkeFunnet(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new ObjektIkkeFunnet(), e, OPPDATER_JOURNALPOST), e);
		} catch (JournalpostIkkeInngaaendeException e) {
			throw new OppdaterJournalpostJournalpostIkkeInngaaende(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new JournalpostIkkeInngaeende(), e, OPPDATER_JOURNALPOST), e);
		}
	}

	@Override
	public void ping() {
		//noop
	}

	private void assertAccessToFerdigstillJournalfoering(FerdigstillJournalfoeringRequest request) throws FerdigstillJournalfoeringSikkerhetsbegrensning {
		try {
			abacSecurityService.assertAccessToJournalpost(request.getJournalpostId());
		} catch (AuthorizationException e) {
			throw new FerdigstillJournalfoeringSikkerhetsbegrensning(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new Sikkerhetsbegrensning(), e, FERDIGSTILL_JOURNALFOERING), e);
		}
	}

	private void assertAccessToOppdaterJournalpost(OppdaterJournalpostRequest request) throws OppdaterJournalpostSikkerhetsbegrensning {
		try {
			abacSecurityService.assertAccessToJournalpost(request.getInngaaendeJournalpost().getJournalpostId());
		} catch (AuthorizationException e) {
			throw new OppdaterJournalpostSikkerhetsbegrensning(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new Sikkerhetsbegrensning(), e, OPPDATER_JOURNALPOST));
		}
	}

}