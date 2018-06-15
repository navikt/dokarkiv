package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark122;

import no.nav.dokarkiv.core.domain.Bruker;
import no.nav.dokarkiv.core.domain.DokumentInfo;
import no.nav.dokarkiv.core.domain.FilDetaljer;
import no.nav.dokarkiv.core.domain.Journalpost;
import no.nav.dokarkiv.core.domain.Saksrelasjon;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.JoarkRepository;

import javax.inject.Inject;
import java.util.Set;

/**
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public class HentJournalpostInfoService {

	@Inject
	private JoarkRepository joarkRepository;

	public HentJournalpostInfoResponseTo hentJournalOgDokumentStatus(HentJournalpostInfoRequestTo request)
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

		Saksrelasjon saksrelasjon = journalpost.getSaksrelasjon();
		Bruker bruker = findBruker(journalpost.getBrukere());

		return HentJournalpostInfoResponseTo.builder()
				.journalStatus(journalpost.getJournalstatus())
				.brukerId(bruker == null ? null : bruker.getBrukerId())
				.brukerType(bruker == null ? null : bruker.getBrukerType())
				.fagomrade(journalpost.getFagomrade())
				.journalfEnhet(journalpost.getJournalForendeEnhetId())
				.saksNummer(saksrelasjon == null ? null : saksrelasjon.getSakId())
				.fagsystem(saksrelasjon == null ? null : saksrelasjon.getFagsystem())
				.dokumentStatus(dokumentstatus)
				.metaforceInstanceId(metaforceInstanceId)
				.build();
	}

	private Bruker findBruker(Set<Bruker> brukere) {
		if(brukere == null || brukere.isEmpty()) {
			return null;
		} else {
			return brukere.iterator().next();
		}
	}

	private void validateRequest(HentJournalpostInfoRequestTo request) throws NoJournalpostFoundException {
		if (request == null) {
			throw new NoJournalpostFoundException("HentJournalpostInfoRequestTo is null", null);
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
