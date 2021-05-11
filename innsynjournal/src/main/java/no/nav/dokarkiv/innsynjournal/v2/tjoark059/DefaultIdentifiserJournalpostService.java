package no.nav.dokarkiv.innsynjournal.v2.tjoark059;

import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.JournalpostNotSupportedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static java.util.Objects.isNull;

/**
 * @author Ketill Fenne, Visma Consulting.
 */
@Component
public class DefaultIdentifiserJournalpostService implements IdentifiserJournalpostService {

	@Inject
	private JoarkRepositorySkjermet joarkRepository;

	@Override
	public Journalpost identifiserJournalpost(IdentifiserJournalpostToRequest identifiserJournalpostToRequest)
	throws JournalpostNotSupportedException, JournalpostIkkeFunnetException, UgyldigInputException, JournalpostIkkeInngaaendeException {
		validateInput(identifiserJournalpostToRequest);

		Journalpost journalpost = findJournalpost(identifiserJournalpostToRequest.getKanalReferanseId(), identifiserJournalpostToRequest.getMottaksKanal());
		validateJournalpost(journalpost);
		return journalpost;
	}

	private Journalpost findJournalpost(final String kanalReferanseId, MottaksKanalCode mottaksKanal) {
		if (isNull(mottaksKanal)) {
			return joarkRepository.findJournalpostByKanalReferanseId(kanalReferanseId).orElseThrow(() ->
					new JournalpostIkkeFunnetException("Uthenting av journalposter med kanalReferanseId=" + kanalReferanseId + " resulterte ikke i nøyaktig én journalpost"));
		} else {
			List<Journalpost> journalposts = joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(kanalReferanseId, mottaksKanal);
			if (!(journalposts == null) && journalposts.size() == 1) {
				validateJournalpost(journalposts.get(0));
				return journalposts.get(0);
			} else {
				throw new JournalpostIkkeFunnetException("Uthenting av journalposter med kanalReferanseId=" + kanalReferanseId + " og mottakskanal=" + mottaksKanal + " resulterte ikke i nøyaktig én journalpost");
			}
		}
	}

	private void validateInput(IdentifiserJournalpostToRequest identifiserJournalpostToRequest) throws UgyldigInputException {
		if (StringUtils.isEmpty(identifiserJournalpostToRequest.getKanalReferanseId())) {
			throw new UgyldigInputException("KanalReferanseId cannot be empty");
		}
	}

	private void validateJournalpost(Journalpost journalpost) throws JournalpostNotSupportedException, JournalpostIkkeInngaaendeException {
		if (!journalpost.isInngaende()) {
			throw new JournalpostIkkeInngaaendeException("Journalposten, journalpostId=" + journalpost.getJournalpostId() + ", som ble funnet er ikke inngående");
		}
		boolean hoveddokument = false;
		for (JournalpostDokumentInfoRelasjon dokumentInfoRelasjon : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			if (dokumentInfoRelasjon.isHoveddokument()) {
				hoveddokument = true;
				break;
			}
		}
		if (!hoveddokument) {
			throw new JournalpostNotSupportedException("Journalposten, journalpostId=" + journalpost.getJournalpostId() + ", mangler hoveddokument");
		}
	}
}
