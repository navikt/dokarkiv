package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlResponse;
import no.nav.dokarkiv.core.ondemand.HentOndemandDokument;
import org.springframework.stereotype.Component;

/**
 * Henter dokumenter fra joark (special case)
 * <p>
 * Kun for DLF og Ondemand dokumenter.
 *
 * @see HentOndemandDokument
 */
@Component
class SafHentDokumentJoarkRepository {
	private final HentOndemandDokument hentOndemandDokument;
	private final RetryingJoarkHentDokumentFromUrlService retryingJoarkHentDokumentFromUrlService;

	SafHentDokumentJoarkRepository(HentOndemandDokument hentOndemandDokument, RetryingJoarkHentDokumentFromUrlService retryingJoarkHentDokumentFromUrlService) {
		this.hentOndemandDokument = hentOndemandDokument;
		this.retryingJoarkHentDokumentFromUrlService = retryingJoarkHentDokumentFromUrlService;
	}

	byte[] hentDokument(final JoarkDokumentDto joarkDokumentDto) {
		HentDokumentUrlResponse dokumentUrl = hentOndemandDokument.createDokumentUrl(joarkDokumentDto.getJournalpostId(), joarkDokumentDto.getFilUuid());
		return retryingJoarkHentDokumentFromUrlService.hentDokumentFromJoark(dokumentUrl.getDokumentUrl());
	}
}
