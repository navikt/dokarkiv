package no.nav.dokarkiv.behandlejournal.v2.tjoark060;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.validator.BrukerValidator;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import org.springframework.stereotype.Component;

@Component
public class DefaultArkiverUstrukturertKrav implements ArkiverUstrukturertKrav {

	private final JoarkRepositorySkjermet joarkRepository;
	private final DokumentFilerDelegate dokumentFilerDelegate;
	private final ArkiverUstrukturertKravJournalpostValidator behandleJournalJournalpostValidator;

	public DefaultArkiverUstrukturertKrav(JoarkRepositorySkjermet joarkRepository, DokumentFilerDelegate dokumentFilerDelegate, ArkiverUstrukturertKravJournalpostValidator behandleJournalJournalpostValidator) {
		this.joarkRepository = joarkRepository;
		this.dokumentFilerDelegate = dokumentFilerDelegate;
		this.behandleJournalJournalpostValidator = behandleJournalJournalpostValidator;
	}

	@Override
	public ArkiverUstrukturertKravResponse arkiverUstrukturertKrav(
			ArkiverUstrukturertKravRequest arkiverUstrukturertKravRequest) {
		arkiverUstrukturertKravRequest.validate();

		Journalpost journalpost = arkiverUstrukturertKravRequest.getJournalpost();

		return handleIncomingDocument(journalpost);
	}

	private ArkiverUstrukturertKravResponse handleIncomingDocument(Journalpost journalpost) {
		validateDokumentInfo(journalpost);
		setInternalJournalpostValues(journalpost);
		validateJournalpost(journalpost);

		return handleJoarkdokument(journalpost);
	}

	private ArkiverUstrukturertKravResponse handleJoarkdokument(Journalpost journalpost) {
		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);

		joarkRepository.save(journalpost);

		return createResponse(journalpost.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo().getDokumentInfoId());
	}

	private ArkiverUstrukturertKravResponse createResponse(Long journalpostId, Long dokumentInfoId) {
		return new ArkiverUstrukturertKravResponse(journalpostId, dokumentInfoId);
	}

	private void validateDokumentInfo(Journalpost journalpost) {
		if (journalpost.getJournalpostDokumentInfoRelasjoner().isEmpty()
				|| journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo() == null) {
			throw new ApplicationException("Journalpost must have a DokumentInfo");
		}
	}

	private void setInternalJournalpostValues(Journalpost journalpost) {
		journalpost.setJournalposttype(JournalpostTypeCode.I);
		journalpost.setJournalstatus(JournalStatusCode.OD);

		JournalpostDokumentInfoRelasjon relasjon = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next();
		relasjon.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
		relasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());

		DokumentInfo dokumentInfo = relasjon.getDokumentInfo();
		if (FagomradeCode.PEN.equals(journalpost.getFagomrade())) {
			dokumentInfo.setKategori(DokumentKategoriCode.IS);
		}
		dokumentInfo.setDokumentstatus(DokumentStatusCode.FERDIGSTILT);
		dokumentInfo.setOriginalJournalpost(journalpost);
	}

	private void validateJournalpost(Journalpost journalpost) {
		behandleJournalJournalpostValidator.validate(journalpost);
		validateBrukerId(journalpost);
	}

	private void validateBrukerId(Journalpost journalpost) {
		for (Bruker bruker : journalpost.getBrukere()) {
			BrukerValidator.validate(bruker);
		}
	}
}
