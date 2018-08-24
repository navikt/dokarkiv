package no.nav.dokarkiv.journalfoerInngaaende.v1.service;

import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentResponse;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class UpdateInngaaendeJournalpostDokumentService {
	private List<String> validDokumentKategorier = Arrays.asList("SED", "SOK", "KA", "IS");


	private DokumentinfoRepository dokumentinfoRepository;

	public UpdateInngaaendeJournalpostDokumentService(DokumentinfoRepository dokumentinfoRepository) {
		this.dokumentinfoRepository = dokumentinfoRepository;
	}

	public PutDokumentResponse update(String journalpostId, String dokumentId, PutDokumentRequest request) throws DokarkivRestFunctionalException {
		validateDokumentKategori(request.getDokumentKategori());

		DokumentInfo dokumentInfo = dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpostId, dokumentId)
				.orElseThrow(() -> new DokarkivRestFunctionalException(String.format("Kunde ikke finne dokumentinfo med journalpostId = %s og dokumentId = %s", journalpostId, dokumentId), HttpStatus.BAD_REQUEST));

		updateValues(request, dokumentInfo);

		dokumentinfoRepository.save(dokumentInfo);

		return new PutDokumentResponse().withDokumentId(dokumentId);
	}

	private void validateDokumentKategori(String kategori) {
		if (!isBlank(kategori) && (!validDokumentKategorier.contains(kategori))) {
			throw new DokarkivRestFunctionalException(String.format("%s er ugyldig verdi for dokumentKategori. Gyldige verdier er %s", kategori, validDokumentKategorier), HttpStatus.BAD_REQUEST);
		}
	}

	private void updateValues(PutDokumentRequest request, DokumentInfo dokumentInfo) {

		if (!isBlank(request.getDokumentTypeId())) {
			dokumentInfo.setDokumenttypeId(request.getDokumentTypeId());
		}
		if (!isBlank(request.getNavSkjemaId())) {
			dokumentInfo.setBrevkode(request.getNavSkjemaId());
		}
		if (!isBlank(request.getTittel())) {
			dokumentInfo.setTittel(request.getTittel());
		}
		if (!isBlank(request.getDokumentKategori())) {
			dokumentInfo.setKategori(DokumentKategoriCode.valueOf(request.getDokumentKategori()));
		}


	}

}
