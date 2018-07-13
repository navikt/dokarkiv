package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;

import static no.nav.dokarkiv.core.utils.DateUtil.getDateNow;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigInputException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ValideringAvVedleggFeiletException;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Slf4j
public class OpprettUtgaaendeJournalpostArkiverDokumentService {

	@Inject
	private JoarkRepository joarkRepository;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;
	@Inject
	private OpprettUtgaaendeJournalpostArkiverDokumentValidator validator;

	public OpprettUtgaaendeJournalpostArkiverDokumentResponseTo opprettUtgaaendeJournalpostArkiverDokument(OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo) throws UgyldigInputException, ValideringAvVedleggFeiletException {

		validator.validateRequiredFields(requestTo);
		//1. Verifiser for evt. tidligere arkivering av samme forsendelse
		Journalpost storedJournalpost = findPreviousJournalpostByKanalReferanseId(requestTo.getJournalpost()
				.getKanalReferanseId());

		if (storedJournalpost == null) {
			Journalpost journalpost = requestTo.getJournalpost();

			updateJournalpostBeforeValidation(journalpost);

			//2. Sjekk om tjenesten skal forsøke å ferdigstille journalposten
			//3. Sjekk om alle påkrevde attributter er satt
			decideAndSetJournalStatus(requestTo.isForsokFerdigstilling(), journalpost);

			//4.Validering av variantformater
			//5.Verifisering av hoveddokument
			validator.validateVariantFormaterAndHoveddokument(journalpost);

			//6.Kontroller knyttesFraJournalpost
			validateAndAddVedlegg(journalpost, requestTo.getVedleggList());

			updateJournalpostAfterValidation(journalpost, requestTo.getJournalforendeEnhet());

			//7.Opprett Journalpost
			dokumentFilerDelegate.saveNewDokumentFiler(journalpost);
			storedJournalpost = joarkRepository.save(journalpost);
			log.info("tjoark111 Har opprettet utgående journalpost med journalpostId={}, hoveddokumentDokumentInfoId={}, journalstatus={}, kanalreferanseId={}, fagområde={}", storedJournalpost
							.getJournalpostId(), storedJournalpost.findHoveddokumentDokumentInfoRelasjon()
							.getDokumentInfo()
							.getDokumentInfoId(),
					storedJournalpost.getJournalstatus(), storedJournalpost.getKanalReferanseId(), storedJournalpost
							.getFagomrade());
			return buildResponse(storedJournalpost, requestTo);
		}

		log.info("tjoark111 Journalpost med journalpostId={}, kanalReferanseId={} eksisterer allerede i databasen.", storedJournalpost
				.getJournalpostId(), storedJournalpost.getKanalReferanseId());
		return buildResponse(storedJournalpost, requestTo);
	}

	private OpprettUtgaaendeJournalpostArkiverDokumentResponseTo buildResponse(Journalpost journalpost, OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo) {
		return OpprettUtgaaendeJournalpostArkiverDokumentResponseTo.builder()
				.dokumentInfoIdHoveddokument(journalpost.findHoveddokumentDokumentInfoRelasjon()
						.getDokumentInfo()
						.getDokumentInfoId())
				.journalpostId(journalpost.getJournalpostId())
				.journalStatus(journalpost.getJournalstatus())
				.dokumentInfoIdVedlegg(mapDokumentInfoVedlegg(journalpost, requestTo))
				.build();
	}

	private List<Long> mapDokumentInfoVedlegg(Journalpost journalpost, OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo) {
		//Vedleggs in the request should be on the end of the list
		List<Long> requestVedleggDokumentInfoIds = getVedleggDokumentInfoIdsFromRequest(requestTo);
		List<Long> persistedDokumentInfoIds = getPersistedDokumentInfoIds(journalpost, requestVedleggDokumentInfoIds);

		persistedDokumentInfoIds.addAll(requestVedleggDokumentInfoIds);

		return persistedDokumentInfoIds;
	}

	private List<Long> getPersistedDokumentInfoIds(Journalpost journalpost, List<Long> requestVedleggDokumentInfoIds) {
		return journalpost.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(this::isNotHoveddokument)
				.filter(relasjon -> !requestVedleggDokumentInfoIds.contains(relasjon.getDokumentInfo().getDokumentInfoId()))
				.map(journalpostDokumentInfoRelasjon -> journalpostDokumentInfoRelasjon.getDokumentInfo().getDokumentInfoId())
				.collect(Collectors.toList());
	}

	private boolean isNotHoveddokument(JournalpostDokumentInfoRelasjon relasjon) {
		return !relasjon.getTilknyttetJournalpostSom().equals(TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
	}

	private List<Long> getVedleggDokumentInfoIdsFromRequest(OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo) {
		return requestTo.getVedleggList()
				.stream()
				.map(OpprettUtgaaendeJournalpostArkiverDokumentRequestTo.Vedlegg::getDokumentInfoId)
				.collect(Collectors.toList());
	}

	private void validateAndAddVedlegg(Journalpost journalpost, List<OpprettUtgaaendeJournalpostArkiverDokumentRequestTo.Vedlegg> vedleggList) throws ValideringAvVedleggFeiletException {

		for (OpprettUtgaaendeJournalpostArkiverDokumentRequestTo.Vedlegg vedlegg : vedleggList) {

			Journalpost originalJournalpost = joarkRepository.findById(vedlegg.getKnyttesFraJournalpostId())
					.orElseThrow(() -> new ValideringAvVedleggFeiletException(String.format("tjoark111 Fant ingen knyttet journalpost med journalpostId=%s for vedlegg med dokumentInfoId=%s", vedlegg
							.getKnyttesFraJournalpostId(), vedlegg.getDokumentInfoId())));

			DokumentInfo dokumentInfo = originalJournalpost.findDokumentInfoById(vedlegg.getDokumentInfoId());
			if (dokumentInfo == null) {
				throw new ValideringAvVedleggFeiletException(String.format("tjoark111 Fant ingen DokumentInfo for vedlegg med dokumentInfoId=%s og knyttet journalpostId=%s", vedlegg
						.getDokumentInfoId(), vedlegg.getKnyttesFraJournalpostId()));
			}

			validator.validateVedlegg(originalJournalpost, dokumentInfo, vedlegg);

			journalpost.addJournalpostDokumentInfoRelasjon(no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon.builder()
					.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
					.tilknyttetAvNavn(journalpost.getOpprettetAvNavn())
					.dokumentInfo(dokumentInfo)
					.build());

			populateDokumentInfoRelasjonWithOpprettetKildeNavn(journalpost);
		}
	}

	public void populateDokumentInfoRelasjonWithOpprettetKildeNavn(Journalpost journalpost) {
		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(relasjon -> {
			relasjon.setOpprettetKildeNavn(RequestContextHolder
					.currentRequestContext().getComponentId());
		});
	}

	private void updateJournalpostAfterValidation(Journalpost journalpost, String journalforendeEnhet) {
		if (JournalStatusCode.FS == journalpost.getJournalstatus()) {
			journalpost.setJournalDato(getDateNow());
			journalpost.setJournalForendeEnhetId(journalforendeEnhet);
			journalpost.setJournalfortAvNavn(journalpost.getOpprettetAvNavn());
		} else {
			journalpost.setJournalForendeEnhetId(null);
		}
	}

	private void updateJournalpostBeforeValidation(Journalpost journalpost) {
		journalpost.setJournalstatus(JournalStatusCode.FS);
	}

	private void decideAndSetJournalStatus(boolean forsokFerdigstilling, Journalpost journalpost) {

		if (forsokFerdigstilling) {
			//3. Sjekk om alle påkrevde attributter er satt
			try {
				validator.validate(journalpost);
				journalpost.setJournalstatus(JournalStatusCode.FS);
			} catch (Exception e) {
				log.info("Påkrevde parametere ikke satt: " + e.getMessage() +
						". Setter JournalStatus = D. KanalreferanseId = " + journalpost.getKanalReferanseId());
				journalpost.setJournalstatus(JournalStatusCode.D);
			}

		} else {
			journalpost.setJournalstatus(JournalStatusCode.D);
		}
	}

	private Journalpost findPreviousJournalpostByKanalReferanseId(String kanalReferanseId) {

		return joarkRepository.findJournalpostByKanalReferanseId(kanalReferanseId).orElse(null);
	}

}
