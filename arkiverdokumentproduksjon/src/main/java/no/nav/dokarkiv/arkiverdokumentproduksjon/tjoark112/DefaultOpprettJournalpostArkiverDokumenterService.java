package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConstants.BESTILLINGS_ID_KEY;
import static org.assertj.core.util.Strings.isNullOrEmpty;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.stream.Collectors;

/**
 * Implementation of OpprettJournalpostArkiverDokumentService
 *
 * @author Cook, Torgeir
 */
@Service
public class DefaultOpprettJournalpostArkiverDokumenterService implements OpprettJournalpostArkiverDokumenterService {

	@Inject
	private JoarkRepository joarkRepository;
	@Inject
	private OpprettJournalpostArkiverDokumenterValidator opprettJournalpostArkiverDokumentValidator;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;

	@Override
	public OpprettJournalpostArkiverDokumenterResponseTo opprettJournalpostArkiverDokument(
			OpprettJournalpostArkiverDokumenterRequestTo requestTo) {
		this.validateRequest(requestTo);
		Journalpost storedJournalpost = findPreviousJournalforing(requestTo);
		if (storedJournalpost == null) {
			Journalpost journalpost = requestTo.getJournalpost();

			updateJournalpost(journalpost);

			opprettJournalpostArkiverDokumentValidator.validate(journalpost);

			dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
			storedJournalpost = joarkRepository.save(journalpost);
		}
		return createResponse(storedJournalpost);
	}

	private void validateRequest(OpprettJournalpostArkiverDokumenterRequestTo request) {
		if (request == null) {
			throw new ApplicationException("Missing parameter: request");
		}
	}

	private OpprettJournalpostArkiverDokumenterResponseTo createResponse(Journalpost journalpost) {
		return OpprettJournalpostArkiverDokumenterResponseTo.builder()
				.journalpostId(journalpost.getJournalpostId())
				.dokumentInfoIdList(journalpost.getJournalpostDokumentInfoRelasjoner()
						.stream().map(relasjon -> relasjon.getDokumentInfo().getDokumentInfoId())
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

		JournalpostDokumentInfoRelasjon relasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		relasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());

		DokumentInfo dokumentInfo = relasjon.getDokumentInfo();
		dokumentInfo.setDokumentstatus(DokumentStatusCode.FERDIGSTILT);
		dokumentInfo.setDokumentFerdigDato(DateProvider.getToday());
		dokumentInfo.setOriginalJournalpost(journalpost);
	}

	private Journalpost findPreviousJournalforing(OpprettJournalpostArkiverDokumenterRequestTo requestTo) {
		final DokumentInfo dokumentInfo = requestTo.getJournalpost().findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		final String bestillingsId = dokumentInfo.getTilleggsopplysninger().get(BESTILLINGS_ID_KEY);
		if (isNullOrEmpty(bestillingsId)) {
			return null;
		}

		Long journalpostIdPreviousJournalforing = findPreviousJournalpostIdByDokumentInfoTilleggsopplysningerBestillingsId(bestillingsId);
		if (journalpostIdPreviousJournalforing == null) {
			return null;
		} else {
			return joarkRepository.findById(journalpostIdPreviousJournalforing).orElse(null);
		}
	}

	private Long findPreviousJournalpostIdByDokumentInfoTilleggsopplysningerBestillingsId(final String bestillingsId) {
		Long dokumentinfoIdPreviousJournalforing = joarkRepository.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(BESTILLINGS_ID_KEY, bestillingsId);
		if (dokumentinfoIdPreviousJournalforing == null) {
			return null;
		}

		return joarkRepository.findJournalpostIdByDokumentinfoId(dokumentinfoIdPreviousJournalforing.toString());
	}
}
