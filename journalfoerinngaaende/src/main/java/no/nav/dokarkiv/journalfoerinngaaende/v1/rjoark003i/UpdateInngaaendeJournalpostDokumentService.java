package no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark003i;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentResponse;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositoryBegrenset;
import no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class UpdateInngaaendeJournalpostDokumentService {
	private final List<String> validDokumentKategorier = Arrays.asList(DokumentKategoriCode.SED.name(), DokumentKategoriCode.SOK
			.name(), DokumentKategoriCode.KA.name(), DokumentKategoriCode.IS.name());

	private final DokumentinfoRepository dokumentinfoRepository;
	private final JoarkRepositoryBegrenset joarkRepository;

	@Inject
	public UpdateInngaaendeJournalpostDokumentService(DokumentinfoRepository dokumentinfoRepository,
													  JoarkRepositoryBegrenset joarkRepository) {
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.joarkRepository = joarkRepository;
	}

	public PutDokumentResponse update(String journalpostIdString, String dokumentIdString, PutDokumentRequest request) {
		Long journalpostId = Utils.convertStringToLong(journalpostIdString, "journalpostId");
		Long dokumentId = Utils.convertStringToLong(dokumentIdString, "dokumentId");

		validateDokumentKategori(request.getDokumentKategori(), journalpostIdString, dokumentIdString);

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostIdString)));

		Utils.assertJournalpostIsInngaaende(journalpost);

		DokumentInfo dokumentInfo = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(dokumentId);
		Utils.assertDokumentInfoNotNull(dokumentInfo, journalpostIdString, dokumentIdString);

		updateValues(request, dokumentInfo);
		dokumentinfoRepository.save(dokumentInfo);

		return new PutDokumentResponse().withDokumentId(dokumentIdString);
	}

	private void validateDokumentKategori(String kategori, String journalpostId, String dokumentId) {
		if (isNotBlank(kategori) && !(validDokumentKategorier.contains(kategori))) {
			throw new InputValideringFeiletException(String.format("%s er ugyldig verdi for dokumentKategori. Gyldige verdier er %s. JournalpostId=%s, dokumentId=%s", kategori, validDokumentKategorier, journalpostId, dokumentId));
		}
	}

	private void updateValues(PutDokumentRequest request, DokumentInfo dokumentInfo) {
		boolean endret = false;
		if (isNotBlank(request.getDokumentTypeId())) {
			dokumentInfo.setDokumenttypeId(request.getDokumentTypeId());
			endret = true;
		}
		if (isNotBlank(request.getNavSkjemaId())) {
			dokumentInfo.setBrevkode(request.getNavSkjemaId());
			endret = true;
		}
		if (isNotBlank(request.getTittel())) {
			dokumentInfo.setTittel(request.getTittel());
			endret = true;
		}
		if (isNotBlank(request.getDokumentKategori())) {
			dokumentInfo.setKategori(DokumentKategoriCode.valueOf(request.getDokumentKategori()));
			endret = true;
		}
		if (endret) {
			dokumentInfo.setEndretAvNavn(MDC.get(MDC_USER_ID));
			dokumentInfo.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		}

	}

}
