package no.nav.dokarkiv.journalfoerInngaaende.v1.service;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentResponse;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.KunneIkkeFinneDokumentInfoException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import org.springframework.stereotype.Component;

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

	public UpdateInngaaendeJournalpostDokumentService(DokumentinfoRepository dokumentinfoRepository) {
		this.dokumentinfoRepository = dokumentinfoRepository;
	}

	public PutDokumentResponse update(String journalpostId, String dokumentId, PutDokumentRequest request) throws DokarkivRestFunctionalException {
		validateDokumentKategori(request.getDokumentKategori(), journalpostId, dokumentId);

		DokumentInfo dokumentInfo = dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpostId, dokumentId)
				.orElseThrow(() -> new KunneIkkeFinneDokumentInfoException(String.format("Kunne ikke finne dokumentinfo med journalpostId = %s og dokumentId = %s", journalpostId, dokumentId)));

		updateValues(request, dokumentInfo);

		dokumentinfoRepository.save(dokumentInfo);

		return new PutDokumentResponse().withDokumentId(dokumentId);
	}

	private void validateDokumentKategori(String kategori, String journalpostId, String dokumentId) {
		if (isNotBlank(kategori) && !(validDokumentKategorier.contains(kategori))) {
			throw new InputValideringFeiletException(String.format("%s er ugyldig verdi for dokumentKategori. Gyldige verdier er %s. JournalpostId = %s, dokumentId = %s", kategori, validDokumentKategorier, journalpostId, dokumentId));
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
