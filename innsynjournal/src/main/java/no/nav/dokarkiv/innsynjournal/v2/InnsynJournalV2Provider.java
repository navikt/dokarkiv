package no.nav.dokarkiv.innsynjournal.v2;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.AuthorizationException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.JournalpostNotSupportedException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.SecurityLimitationAttributeException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.UgyldigInputException;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.HentJournalpostListeToRequest;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.HentMinTilgjengeligJournalpostListeV2ResponseMapper;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.HentMinTilgjengeligeJournalpostListeV2RequestMapper;
import no.nav.dokarkiv.innsynjournal.v2.tjoark059.IdentifiserJournalpostToRequest;
import no.nav.dokarkiv.innsynjournal.v2.tjoark059.IdentifiserJournalpostV2RequestMapper;
import no.nav.dokarkiv.innsynjournal.v2.tjoark059.IdentifiserJournalpostV2ResponseMapper;
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
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentDokumentRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentDokumentResponse;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeResponse;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
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
public class InnsynJournalV2Provider implements InnsynJournalV2, InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(InnsynJournalV2Provider.class);

	static final String JOURNALPOSTID_REQIRED = "Journalpostid is required";
	static final String DOKUMENTID_REQUIRED = "Dokumentid is required";

	static final String INNSYN_JOURNAL_V2 = "InnsynJournalV2";
	static final String INNSYN_JOURNAL_V2_HENT_DOKUMENT = INNSYN_JOURNAL_V2 + ".hentDokument";
	static final String INNSYN_JOURNAL_V2_HENT_JP_LISTE = INNSYN_JOURNAL_V2 + ".hentTilgjengeligJournalpostListe";
	static final String INNSYN_JOURNAL_V2_IDENTIFISER_JP = INNSYN_JOURNAL_V2 + ".identifiserJournalpost";
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

	@Inject
	private MetricRegistry metrics;
	private Histogram sakerHistogram;
	private Histogram dokumenterHistogram;
	private Histogram dokumenterIJPHistogram;

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
			HentTilgjengeligJournalpostListeResponse response = journalpostListeV2ResponseMapper.mapList(innsynJournalpostTos);
			countSakerAndDokumenter(response, toRequest.getSaksListe().size());
			return response;
		} catch (AuthorizationException e) {
//			log.warn(String.format("Access denied in operation %s. LoggedOnUser=%s", INNSYN_JOURNAL_V2_HENT_JP_LISTE,
//					SubjectHandler.getSubjectHandler().getUid()), e); FIXME
			AuthorizationException undetailedException = new AuthorizationException(ACCESS_DENIED);
			throw new HentTilgjengeligJournalpostListeSikkerhetsbegrensning(undetailedException.getMessage(), new FunctionalFault());
		}
	}

	private void countSakerAndDokumenter(HentTilgjengeligJournalpostListeResponse response, int antallSaker) {
		sakerHistogram.update(antallSaker);
		List<Journalpost> journalpostListe = response.getJournalpostListe();
		int i = 0;
		for (Journalpost journalpost : journalpostListe) {
			i += journalpost.getDokumentinfoRelasjonListe().size();
		}
		dokumenterHistogram.update(i);
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
			throw new HentDokumentDokumentIkkeFunnet(e.getMessage(),new FunctionalFault());
		} catch (AuthorizationException e) {
//			log.warn(String.format("Access denied in operation %s. JournalpostId=%s ,dokumentInfoId=%s , logged on user=%s",
//					INNSYN_JOURNAL_V2_HENT_DOKUMENT, request.getJournalpostId(), request.getDokumentId(),
//					SubjectHandler.getSubjectHandler().getUid()), e);
			AuthorizationException undetailedException = new AuthorizationException(ACCESS_DENIED);
			throw new HentDokumentSikkerhetsbegrensning(undetailedException.getMessage(),new FunctionalFault());
		} catch (SecurityLimitationAttributeException e) {
			log.warn(e.toLogMessage(INNSYN_JOURNAL_V2_HENT_DOKUMENT));
			AuthorizationException undetailedException = new AuthorizationException(ACCESS_DENIED);
			throw new HentDokumentSikkerhetsbegrensning(undetailedException.getMessage(),new FunctionalFault());
		}

		HentDokumentResponse response = new HentDokumentResponse();
		response.setVariantFormat(createVariantFormatter());
		response.setDokument(file);
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
			(IdentifiserJournalpostRequest wsRequest) throws IdentifiserJournalpostUgyldingInput, IdentifiserJournalpostObjektIkkeFunnet, IdentifiserJournalpostUgyldigAntallJournalposter, IdentifiserJournalpostJournalpostIkkeInngaaende
	{
		try {
			if (wsRequest == null) {
				throw new IdentifiserJournalpostUgyldingInput("Request is empty", new TechnicalFault());
			}
			IdentifiserJournalpostToRequest toRequest = identifiserJournalpostV2RequestMapper.map(wsRequest);
			InnsynJournalpostTo innsynJournalpostTo = securityFacade.identifiserJournalpost(toRequest);
			IdentifiserJournalpostResponse response = identifiserJournalpostV2ResponseMapper.map(innsynJournalpostTo);
			countDokumenter(response);
			return response;

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

	private void countDokumenter(IdentifiserJournalpostResponse response) {
		int i = 1; //Hoveddokument
		i += response.getVedleggListe().size();
		dokumenterIJPHistogram.update(i);
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		sakerHistogram = metrics.histogram(INNSYN_JOURNAL_V2_HENT_JP_LISTE + ".saker");
		dokumenterHistogram = metrics.histogram(INNSYN_JOURNAL_V2_HENT_JP_LISTE + ".dokumenter");
		dokumenterIJPHistogram = metrics.histogram(INNSYN_JOURNAL_V2_IDENTIFISER_JP + ".dokumenter");
	}
}
