package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import org.springframework.stereotype.Service;

@Service
public class DefaultHentJournalOgDokumentStatus implements HentJournalOgDokumentStatus {

    private final JoarkRepositorySkjermet joarkRepository;

	public DefaultHentJournalOgDokumentStatus(JoarkRepositorySkjermet joarkRepository) {
		this.joarkRepository = joarkRepository;
	}

	@Override
	public HentJournalOgDokumentStatusResponseTo hentJournalOgDokumentStatus(HentJournalOgDokumentStatusRequestTo request)
			throws NoJournalpostFoundException, NoDokumentInfoFoundException {

		validateRequest(request);

		Journalpost journalpost = findJournalpost(request.getJournalpostId());
		Long metaforceInstanceId = null;
		DokumentStatusCode dokumentstatus = null;

		if (request.getDokumentInfoId() != null && request.getDokumentInfoId() != 0) {
			DokumentInfo dokumentInfo = findDokumentInfoOnJournalpost(journalpost, request.getDokumentInfoId());

			metaforceInstanceId = findMetaforceInstanceIdOnProduksjonFildetaljer(dokumentInfo);
			dokumentstatus = dokumentInfo.getDokumentstatus();
		}

		return new HentJournalOgDokumentStatusResponseTo(journalpost.getJournalstatus(), dokumentstatus,
				metaforceInstanceId);
	}

	private void validateRequest(HentJournalOgDokumentStatusRequestTo request) {
		if (request == null) {
			throw new InvalidArgumentException("HentJournalOgDokumentStatusRequestTo is null");
		}
		request.validate();
	}

	private Journalpost findJournalpost(Long journalpostId) throws NoJournalpostFoundException {
		Journalpost journalpost = joarkRepository.findById(journalpostId).orElse(null);
		if (journalpost == null) {
			throw new NoJournalpostFoundException("Journalpost with id: " + journalpostId + " does not exist", journalpostId);
		}
		return journalpost;
	}

	private DokumentInfo findDokumentInfoOnJournalpost(Journalpost journalpost, Long dokumentInfoId)
			throws NoDokumentInfoFoundException {
		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(dokumentInfoId);
		if (dokumentInfo == null) {
			throw new NoDokumentInfoFoundException("Journalpost, journalpostId=" + journalpost.getJournalpostId() + ",  has no DokumentInfo with id: " + dokumentInfoId,
					dokumentInfoId);
		}
		return dokumentInfo;
	}

	private Long findMetaforceInstanceIdOnProduksjonFildetaljer(DokumentInfo dokumentInfo) {
		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.PRODUKSJON);
		if (filDetaljer != null) {
			return filDetaljer.getMetaforceInstanceId();
		}
		return null;
	}

}
