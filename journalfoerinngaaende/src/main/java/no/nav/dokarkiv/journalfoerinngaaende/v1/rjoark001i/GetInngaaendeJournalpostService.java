package no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark001i;

import no.nav.dok.tjenester.journalfoerinngaaende.Dokument;
import no.nav.dok.tjenester.journalfoerinngaaende.GetJournalpostResponse;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetInngaaendeJournalpostService {

    private final JoarkRepositorySkjermet joarkRepository;
	private final GetInngaaendeJournalpostMapper getInngaaendeJournalpostMapper;

    public GetInngaaendeJournalpostService(JoarkRepositorySkjermet joarkRepository,
										   GetInngaaendeJournalpostMapper getInngaaendeJournalpostMapper) {
		this.joarkRepository = joarkRepository;
		this.getInngaaendeJournalpostMapper = getInngaaendeJournalpostMapper;
	}

	public GetJournalpostResponse getInngaaendeJournalpostByJournalpostId(String journalpostIdString) {
		Long journalpostId = Utils.convertStringToLong(journalpostIdString, "journalpostId");

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostIdString)));

		Utils.assertJournalpostIsInngaaende(journalpost);

		GetJournalpostResponse response = getInngaaendeJournalpostMapper.map(journalpost);

		return filterFildetaljer(response);
	}

	private GetJournalpostResponse filterFildetaljer(GetJournalpostResponse journalpost) {
		List<Dokument> dokumentListe = journalpost.getDokumentListe();
		for (Dokument dokument:dokumentListe){
			dokument.setVariant(dokument.getVariant().stream().filter(variant -> !variant.getVariantFormat().equalsIgnoreCase(VariantFormatCode.SLADDET.name())).collect(Collectors.toList()));
		}
		return journalpost;
	}

}