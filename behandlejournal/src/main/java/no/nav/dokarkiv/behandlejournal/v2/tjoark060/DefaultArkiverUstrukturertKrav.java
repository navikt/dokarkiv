package no.nav.dokarkiv.behandlejournal.v2.tjoark060;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagring;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokument;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokumentType;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringStatus;
import no.nav.dokarkiv.core.domain.validator.BrukerValidator;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.BidragMellomlagringRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Implementation of {@link ArkiverUstrukturertKrav}
 *
 * @author Joakim Bjørnstad, Visma Consulting
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Component
public class DefaultArkiverUstrukturertKrav implements ArkiverUstrukturertKrav {

	@Inject
    private JoarkRepositorySkjermet joarkRepository;
	@Inject
	private BidragMellomlagringRepository bidragMellomlagringRepository;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;
	@Inject
	private ArkiverUstrukturertKravJournalpostValidator behandleJournalJournalpostValidator;

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

		if (isBidragsdokument(journalpost)) {
			return handleBidragsdokument(journalpost);
		} else {
			return handleJoarkdokument(journalpost);
		}
	}

	private boolean isBidragsdokument(Journalpost journalpost) {
		return FagomradeCode.BID == journalpost.getFagomrade();
	}

	private ArkiverUstrukturertKravResponse handleJoarkdokument(Journalpost journalpost) {
		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);

		joarkRepository.save(journalpost);

		return createResponse(journalpost.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo().getDokumentInfoId());
	}

	private ArkiverUstrukturertKravResponse handleBidragsdokument(Journalpost journalpost) {
		BidragMellomlagring bidragMellomlagring = new BidragMellomlagring();
		bidragMellomlagring.setAvsenderFnr(journalpost.getBrukere().iterator().next().getBrukerId());
		bidragMellomlagring.setMottattDato(journalpost.getMottattDato());
		bidragMellomlagring.setStatus(BidragMellomlagringStatus.DOKUMENTOPPLASTING);

		BidragMellomlagringDokument bidragMellomlagringDokument = new BidragMellomlagringDokument();
		bidragMellomlagringDokument.setDokument(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo()
				.getFildetaljerListe().iterator().next().getFileContent());
		bidragMellomlagringDokument.setDokumentType(BidragMellomlagringDokumentType.HOVEDDOKUMENT);

		bidragMellomlagring.addBidragMellomlagringDokument(bidragMellomlagringDokument);

		BidragMellomlagring persistedBidragMellomlagring = bidragMellomlagringRepository
				.save(bidragMellomlagring);

		return createResponse(persistedBidragMellomlagring.getIdWithPrefix(), persistedBidragMellomlagring
				.getBidragMellomlagringDokuments().iterator().next().getBidragMellomlagringDokumentId());
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
