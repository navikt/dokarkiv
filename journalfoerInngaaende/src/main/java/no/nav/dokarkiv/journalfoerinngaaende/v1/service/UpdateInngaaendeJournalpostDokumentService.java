package no.nav.dokarkiv.journalfoerinngaaende.v1.service;

import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils.assertDokumentInfoNotNull;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils.assetJournalpostIsInngaaende;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils.convertStringToLong;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentResponse;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
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

	private DokumentinfoRepository dokumentinfoRepository;
	private JoarkRepository joarkRepository;

	@Inject
	public UpdateInngaaendeJournalpostDokumentService(DokumentinfoRepository dokumentinfoRepository,
													  JoarkRepository joarkRepository) {
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.joarkRepository = joarkRepository;
	}

	public PutDokumentResponse update(String journalpostIdString, String dokumentIdString, PutDokumentRequest request) {
		Long journalpostId = convertStringToLong(journalpostIdString, "journalpostId");
		Long dokumentId = convertStringToLong(dokumentIdString, "dokumentId");

		validateDokumentKategori(request.getDokumentKategori(), journalpostIdString, dokumentIdString);

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostIdString)));

		assetJournalpostIsInngaaende(journalpost);

		DokumentInfo dokumentInfo = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(dokumentId);
		assertDokumentInfoNotNull(dokumentInfo, journalpostIdString, dokumentIdString);

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
		if (isNotBlank(request.getDokumentTypeId())) {
			dokumentInfo.setDokumenttypeId(request.getDokumentTypeId());
		}
		if (isNotBlank(request.getNavSkjemaId())) {
			dokumentInfo.setBrevkode(request.getNavSkjemaId());
		}
		if (isNotBlank(request.getTittel())) {
			dokumentInfo.setTittel(request.getTittel());
		}
		if (isNotBlank(request.getDokumentKategori())) {
			dokumentInfo.setKategori(DokumentKategoriCode.valueOf(request.getDokumentKategori()));
		}

	}

}
