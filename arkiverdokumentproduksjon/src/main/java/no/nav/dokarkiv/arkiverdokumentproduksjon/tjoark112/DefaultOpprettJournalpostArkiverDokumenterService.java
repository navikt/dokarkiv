package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConstants.BESTILLINGS_ID_KEY;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConstants.FILREFERANSE_ID_KEY;
import static org.assertj.core.util.Strings.isNullOrEmpty;

@Component
@Slf4j
public class DefaultOpprettJournalpostArkiverDokumenterService implements OpprettJournalpostArkiverDokumenterService {

	private final JournalpostRepositorySkjermet journalpostRepositorySkjermet;
	private final OpprettJournalpostArkiverDokumenterValidator opprettJournalpostArkiverDokumenterValidator;
	private final DokumentFilerDelegate dokumentFilerDelegate;
	private final OpprettJournalpostArkiverDokumenterRequestMapper opprettJournalpostArkiverDokumenterRequestMapper;

	public DefaultOpprettJournalpostArkiverDokumenterService(JournalpostRepositorySkjermet journalpostRepositorySkjermet,
															 OpprettJournalpostArkiverDokumenterValidator opprettJournalpostArkiverDokumenterValidator,
															 DokumentFilerDelegate dokumentFilerDelegate,
															 OpprettJournalpostArkiverDokumenterRequestMapper opprettJournalpostArkiverDokumenterRequestMapper) {
		this.journalpostRepositorySkjermet = journalpostRepositorySkjermet;
		this.opprettJournalpostArkiverDokumenterValidator = opprettJournalpostArkiverDokumenterValidator;
		this.dokumentFilerDelegate = dokumentFilerDelegate;
		this.opprettJournalpostArkiverDokumenterRequestMapper = opprettJournalpostArkiverDokumenterRequestMapper;
	}

	@Override
	public OpprettJournalpostArkiverDokumenterResponseTo opprettJournalpostArkiverDokument(OpprettJournalpostArkiverDokumenterRequest wsRequest) {
		OpprettJournalpostArkiverDokumenterRequestTo requestTo = opprettJournalpostArkiverDokumenterRequestMapper.map(wsRequest);

		Journalpost storedJournalpost = findPreviousJournalforing(requestTo);
		if (storedJournalpost == null) {

			Journalpost journalpost = requestTo.getJournalpost();

			updateJournalpost(journalpost);

			opprettJournalpostArkiverDokumenterValidator.validate(journalpost);

			dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
			storedJournalpost = journalpostRepositorySkjermet.save(journalpost);
		}
		return createResponse(storedJournalpost);
	}

	private OpprettJournalpostArkiverDokumenterResponseTo createResponse(Journalpost journalpost) {
		return OpprettJournalpostArkiverDokumenterResponseTo.builder()
				.journalpostId(journalpost.getJournalpostId())
				.dokumentInfoIds(journalpost.getJournalpostDokumentInfoRelasjoner()
						.stream().map(relasjon -> {
							final String filreferanse = relasjon.getDokumentInfo().getTilleggsopplysninger().get(FILREFERANSE_ID_KEY);
							if(filreferanse == null) {
								return null;
							}
							final Long dokumentInfoId = relasjon.getDokumentInfo().getDokumentInfoId();
							return OpprettJournalpostArkiverDokumenterResponseTo.DokumentInfoIdEntryTo.builder()
									.filreferanse(filreferanse)
									.dokumentInfoId(dokumentInfoId)
									.build();
						})
						.filter(Objects::nonNull)
						.collect(Collectors.toList()))
				.build();
	}


	private void updateJournalpost(Journalpost journalpost) {
		journalpost.setJournalstatus(JournalStatusCode.D);
		journalpost.setJournalDato(null);
		journalpost.setJournalfortAvNavn(null);
		journalpost.setUtsendingskanal(null);

		if (journalpost.getJournalposttype() == null) {
			journalpost.setJournalposttype(JournalpostTypeCode.U);
		}

		Set<JournalpostDokumentInfoRelasjon> relasjoner = journalpost.getJournalpostDokumentInfoRelasjoner();
		relasjoner.forEach(relasjon -> {
			relasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());
			DokumentInfo dokumentInfo = relasjon.getDokumentInfo();
			dokumentInfo.setDokumentstatus(DokumentStatusCode.FERDIGSTILT);
			dokumentInfo.setDokumentFerdigDato(DateProvider.getToday());
			dokumentInfo.setOriginalJournalpost(journalpost);
		});
	}

	private Journalpost findPreviousJournalforing(OpprettJournalpostArkiverDokumenterRequestTo requestTo) {
		// Denne kan endres til å sjekke etter duplikat på kanalReferanseId i stedet for i dokumentinfo.tilleggsopplysninger
		// Kan utføres etter at endringen der denne kommentaren ligger har vist seg å være stabil
		final DokumentInfo dokumentInfo = requestTo.getJournalpost().findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		final String bestillingsId = dokumentInfo.getTilleggsopplysninger().get(BESTILLINGS_ID_KEY);
		if (isNullOrEmpty(bestillingsId)) {
			return null;
		}

		Long journalpostIdPreviousJournalforing = findPreviousJournalpostIdByDokumentInfoTilleggsopplysningerBestillingsId(bestillingsId);
		if (journalpostIdPreviousJournalforing == null) {
			return null;
		} else {
			return journalpostRepositorySkjermet.findById(journalpostIdPreviousJournalforing).orElse(null);
		}
	}

	private Long findPreviousJournalpostIdByDokumentInfoTilleggsopplysningerBestillingsId(final String bestillingsId) {
		Long dokumentinfoIdPreviousJournalforing = journalpostRepositorySkjermet.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(BESTILLINGS_ID_KEY, bestillingsId);
		if (dokumentinfoIdPreviousJournalforing == null) {
			return null;
		}

		return journalpostRepositorySkjermet.findJournalpostIdByDokumentinfoId(dokumentinfoIdPreviousJournalforing.toString());
	}
}
