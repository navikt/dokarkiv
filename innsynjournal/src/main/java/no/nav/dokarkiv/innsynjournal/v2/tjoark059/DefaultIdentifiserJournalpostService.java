package no.nav.dokarkiv.innsynjournal.v2.tjoark059;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.JournalpostNotSupportedException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.UgyldigInputException;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

/**
 * @author Ketill Fenne, Visma Consulting.
 */
public class DefaultIdentifiserJournalpostService implements IdentifiserJournalpostService {

	@Inject
	private JoarkRepository joarkRepository;

	@Override
	public Journalpost identifiserJournalpost(IdentifiserJournalpostToRequest identifiserJournalpostToRequest)
	throws JournalpostNotSupportedException, JournalpostIkkeFunnetException, UgyldigInputException, JournalpostIkkeInngaaendeException {
		validateInput(identifiserJournalpostToRequest);
		List<Journalpost> journalposts = joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(identifiserJournalpostToRequest.getKanalReferanseId(), identifiserJournalpostToRequest.getMottaksKanal());
		if (!(journalposts == null) && journalposts.size() == 1) {
			validateJournalpost(journalposts.get(0));
			return journalposts.get(0);
		} else {
			if ((identifiserJournalpostToRequest.getMottaksKanal() == null) || (Objects.equals(identifiserJournalpostToRequest.getMottaksKanal().name(), ""))) {
				throw new JournalpostIkkeFunnetException("Uthenting av journalposter med kanalReferanseId=" + identifiserJournalpostToRequest.getKanalReferanseId() + " resulterte ikke i nøyaktig én journalpost");
			} else {
				throw new JournalpostIkkeFunnetException("Uthenting av journalposter med kanalReferanseId=" + identifiserJournalpostToRequest.getKanalReferanseId() + " og mottakskanal=" + identifiserJournalpostToRequest.getMottaksKanal().name() + " resulterte ikke i nøyaktig én journalpost");
			}
		}
	}

	private void validateInput(IdentifiserJournalpostToRequest identifiserJournalpostToRequest) throws UgyldigInputException {
		if (StringUtils.isEmpty(identifiserJournalpostToRequest.getKanalReferanseId())) {
			throw new UgyldigInputException("KanalReferanseId cannot be empty");
		}
	}

	private void validateJournalpost(Journalpost journalpost) throws JournalpostNotSupportedException, JournalpostIkkeInngaaendeException {
		Boolean hoveddokument = false;
		if (!journalpost.isInngaende()) {
			throw new JournalpostIkkeInngaaendeException("Journalposten, journalpostId=" + journalpost.getJournalpostId() + ", som ble funnet er ikke inngående");
		}
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
