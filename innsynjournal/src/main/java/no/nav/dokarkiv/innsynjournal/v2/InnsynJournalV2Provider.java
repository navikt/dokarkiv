package no.nav.dokarkiv.innsynjournal.v2;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.JournalpostNotSupportedException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.SecurityLimitationAttributeException;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.HentJournalpostListeToRequest;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.HentMinTilgjengeligJournalpostListeV2ResponseMapper;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.HentMinTilgjengeligeJournalpostListeV2RequestMapper;
import no.nav.dokarkiv.innsynjournal.v2.tjoark059.IdentifiserJournalpostToRequest;
import no.nav.dokarkiv.innsynjournal.v2.tjoark059.IdentifiserJournalpostV2RequestMapper;
import no.nav.dokarkiv.innsynjournal.v2.tjoark059.IdentifiserJournalpostV2ResponseMapper;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.exception.AuthorizationException;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.HentDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.HentDokumentSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.HentTilgjengeligJournalpostListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.IdentifiserJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.IdentifiserJournalpostObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.IdentifiserJournalpostUgyldigAntallJournalposter;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.IdentifiserJournalpostUgyldingInput;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.InnsynJournalV2;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.feil.FunctionalFault;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.feil.TechnicalFault;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentDokumentRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentDokumentResponse;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeResponse;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.inject.Inject;
import java.util.List;

/**
 * POJO InnsynJournalV1Provider that maps from and to WS model (FIM) and delegates to
 * Service implementations.
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@Slf4j
@Component
public class InnsynJournalV2Provider implements InnsynJournalV2 {
	static final String JOURNALPOSTID_REQIRED = "Journalpostid is required";
	static final String DOKUMENTID_REQUIRED = "Dokumentid is required";

	private static final String INNSYN_JOURNAL_V2 = "InnsynJournalV2";
	private static final String INNSYN_JOURNAL_V2_HENT_DOKUMENT = INNSYN_JOURNAL_V2 + ".hentDokument";
	private static final String INNSYN_JOURNAL_V2_HENT_JP_LISTE = INNSYN_JOURNAL_V2 + ".hentTilgjengeligJournalpostListe";
	private static final String ACCESS_DENIED = "Access denied";

	@Inject
	private InnsynJournalV2SecurityFacade securityFacade;
	@Inject
	private HentMinTilgjengeligeJournalpostListeV2RequestMapper journalpostListeV2RequestMapper;
	@Inject
	private HentMinTilgjengeligJournalpostListeV2ResponseMapper journalpostListeV2ResponseMapper;
	@Inject
	private IdentifiserJournalpostV2RequestMapper identifiserJournalpostV2RequestMapper;
	@Inject
	private IdentifiserJournalpostV2ResponseMapper identifiserJournalpostV2ResponseMapper;

	@Override
	public void ping() {
		//noop
	}

	@Override
	@Transactional(readOnly = true)
	public HentTilgjengeligJournalpostListeResponse hentTilgjengeligJournalpostListe
			(HentTilgjengeligJournalpostListeRequest wsRequest) throws HentTilgjengeligJournalpostListeSikkerhetsbegrensning {
		try {
			HentJournalpostListeToRequest toRequest = journalpostListeV2RequestMapper.map(wsRequest);
			List<InnsynJournalpostTo> innsynJournalpostTos = securityFacade.hentMineTilgjengeligeJournalpostListe(toRequest);
			log.info("tjoark053 hentet tilgjengelige journalposter");
			return journalpostListeV2ResponseMapper.mapList(innsynJournalpostTos);
		} catch (AuthorizationException e) {
			log.warn(String.format("Access denied in operation %s. LoggedOnUser=%s", INNSYN_JOURNAL_V2_HENT_JP_LISTE,
					SubjectHandler.getSubjectHandler().getUid()), e);
			AuthorizationException undetailedException = new AuthorizationException(ACCESS_DENIED);
			throw new HentTilgjengeligJournalpostListeSikkerhetsbegrensning(undetailedException.getMessage(), new FunctionalFault());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public HentDokumentResponse hentDokument(HentDokumentRequest request) throws HentDokumentDokumentIkkeFunnet,
			HentDokumentSikkerhetsbegrensning {
		Assert.hasText(request.getJournalpostId(), JOURNALPOSTID_REQIRED);
		Assert.hasText(request.getDokumentId(), DOKUMENTID_REQUIRED);
		Long journalpostId = Long.valueOf(request.getJournalpostId());
		Long dokumentId = Long.valueOf(request.getDokumentId());

		byte[] file;
		try {
			file = securityFacade.hentDokument(journalpostId, dokumentId);
		} catch (NoJournalpostFoundException | DocumentNotFoundException e) {
			throw new HentDokumentDokumentIkkeFunnet(e.getMessage(), new FunctionalFault());
		} catch (AuthorizationException e) {
			log.warn(String.format("Access denied in operation %s. JournalpostId=%s ,dokumentInfoId=%s , logged on user=%s",
					INNSYN_JOURNAL_V2_HENT_DOKUMENT, request.getJournalpostId(), request.getDokumentId(),
					SubjectHandler.getSubjectHandler().getUid()), e);
			AuthorizationException undetailedException = new AuthorizationException(ACCESS_DENIED);
			throw new HentDokumentSikkerhetsbegrensning(undetailedException.getMessage(), new FunctionalFault());
		} catch (SecurityLimitationAttributeException e) {
			log.warn(e.toLogMessage(INNSYN_JOURNAL_V2_HENT_DOKUMENT));
			AuthorizationException undetailedException = new AuthorizationException(ACCESS_DENIED);
			throw new HentDokumentSikkerhetsbegrensning(undetailedException.getMessage(), new FunctionalFault());
		}

		HentDokumentResponse response = new HentDokumentResponse();
		response.setVariantFormat(createVariantFormatter());
		response.setDokument(file);
		log.info("tjoark054 hentet dokument fra journalpostId={}, dokumentInfoId={}", request.getJournalpostId(), request.getDokumentId());
		return response;
	}

	private Variantformater createVariantFormatter() {
		Variantformater variantformater = new Variantformater();
		variantformater.setValue(VariantFormatCode.ARKIV.name());
		return variantformater;
	}

	@Override
	@Transactional(readOnly = true)
	public IdentifiserJournalpostResponse identifiserJournalpost
			(IdentifiserJournalpostRequest wsRequest) throws IdentifiserJournalpostUgyldingInput, IdentifiserJournalpostObjektIkkeFunnet, IdentifiserJournalpostUgyldigAntallJournalposter, IdentifiserJournalpostJournalpostIkkeInngaaende {
		try {
			if (wsRequest == null) {
				throw new IdentifiserJournalpostUgyldingInput("Request is empty", new TechnicalFault());
			}
			IdentifiserJournalpostToRequest toRequest = identifiserJournalpostV2RequestMapper.map(wsRequest);
			InnsynJournalpostTo innsynJournalpostTo = securityFacade.identifiserJournalpost(toRequest);
			log.info("tjoark059 identifiserte journalposter for kanalReferanseId={}, mottakskanal={}", wsRequest.getKanalReferanseId(), wsRequest.getMottakskanal());
			return identifiserJournalpostV2ResponseMapper.map(innsynJournalpostTo);
		} catch (UgyldigInputException e) {
			throw new IdentifiserJournalpostUgyldingInput(e.getMessage(), new TechnicalFault());
		} catch (JournalpostNotSupportedException e) {
			//Mangler hoveddokument
			throw new IdentifiserJournalpostObjektIkkeFunnet(e.getMessage(), new FunctionalFault());
		} catch (JournalpostIkkeInngaaendeException e) {
			throw new IdentifiserJournalpostJournalpostIkkeInngaaende(e.getMessage(), new FunctionalFault());
		} catch (JournalpostIkkeFunnetException e) {
			throw new IdentifiserJournalpostUgyldigAntallJournalposter(e.getMessage(), new FunctionalFault());
		}
	}
}
