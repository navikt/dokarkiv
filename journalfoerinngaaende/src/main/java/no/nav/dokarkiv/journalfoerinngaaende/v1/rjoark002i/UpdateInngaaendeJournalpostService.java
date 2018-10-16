package no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark002i;

import static no.nav.dok.tjenester.journalfoerinngaaende.response.Status.MANGLER;
import static no.nav.dok.tjenester.journalfoerinngaaende.response.Status.MANGLER_IKKE;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.support.JournalpostValidator.validateJournalpostStatuser;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.support.JournalpostValidator.validateJournalpostStrukturOgPaakrevdeAttributter;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils.convertStringToLong;
import static org.hibernate.annotations.common.util.StringHelper.isEmpty;

import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.response.Dokument;
import no.nav.dok.tjenester.journalfoerinngaaende.response.Mangler;
import no.nav.dok.tjenester.journalfoerinngaaende.response.Status;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Paul Magne Lunde, Visma Consulting
 */
@Service
public class UpdateInngaaendeJournalpostService {

	private final JoarkRepository joarkRepository;
	private final PutInngaaendeJournalpostMapper putInngaaendeJournalpostMapper;

	@Inject
	public UpdateInngaaendeJournalpostService(JoarkRepository joarkRepository,
											  PutInngaaendeJournalpostMapper putInngaaendeJournalpostMapper) {
		this.joarkRepository = joarkRepository;
		this.putInngaaendeJournalpostMapper = putInngaaendeJournalpostMapper;
	}

	public PutJournalpostResponse updateInngaaendeJournalpost(String journalpostId, PutJournalpostRequest putJournalpostRequest) {
		Journalpost journalpost = joarkRepository.findById(convertStringToLong(journalpostId, "journalpostId"))
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		validateJournalpostStatuser(journalpost);

		if (putJournalpostRequest.isForsoekEndeligJF()) {
			validateJournalpostStrukturOgPaakrevdeAttributter(journalpost);
		}

		putInngaaendeJournalpostMapper.oppdaterJournalpost(journalpost, putJournalpostRequest);

		PutJournalpostResponse response = new PutJournalpostResponse();
		response.setJournalpostId(journalpostId);
		response.setHarEndeligJF(false);

		if (putJournalpostRequest.isForsoekEndeligJF()) {
			Mangler mangler = createMangler(journalpost);
			if (containsMangler(mangler)) {
				response.setMangler(createMangler(journalpost));
			} else {
				// ferdigstill journalpost
				journalpost.setJournalstatus(JournalStatusCode.J);
				journalpost.setJournalDato(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
				journalpost.setJournalForendeEnhetId(putJournalpostRequest.getJournalfEnhet());
				journalpost.setEndretAvNavn(MDC.get(MDC_USER_ID));
				journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
				response.setHarEndeligJF(true);
			}
		}
		joarkRepository.save(journalpost);

		return response;
	}

	private Mangler createMangler(Journalpost jp) {
		List<Dokument> dokumentList = new ArrayList<>();
		jp.getJournalpostDokumentInfoRelasjoner().forEach(d -> {
			DokumentInfo dokumentInfo = d.getDokumentInfo();
			if (dokumentInfo != null) {
				dokumentList.add(
						new Dokument()
								.withDokumentId(dokumentInfo.getId().toString())
								.withTittel(isEmpty(dokumentInfo.getTittel()) ? MANGLER : MANGLER_IKKE)
								.withDokumentKategori(isEmpty(dokumentInfo.getKategori().name()) ? MANGLER : MANGLER_IKKE)
				);
			}
		});

		return new Mangler()
				.withAvsenderNavn(decideIfMangler(jp.getAvsenderMottaker()))
				.withArkivSak((jp.getSaksrelasjon() == null) ? MANGLER : MANGLER_IKKE)
				.withTittel(decideIfMangler(jp.getInnhold()))
				.withTema((jp.getFagomrade() == null) ? MANGLER : MANGLER_IKKE)
				.withBruker((jp.getBrukere().isEmpty()) ? MANGLER : MANGLER_IKKE)
				.withDokumenter(dokumentList);
	}

	private boolean containsMangler(Mangler mangler) {
		boolean containsMangler = false;
		if (mangler.getDokumenter()
				.stream()
				.anyMatch(dokument -> MANGLER.equals(dokument.getDokumentKategori()) || MANGLER.equals(dokument.getTittel()))) {
			containsMangler = true;
		}
		if (MANGLER.equals(mangler.getAvsenderNavn())) {
			containsMangler = true;
		}
		if (MANGLER.equals(mangler.getArkivSak())) {
			containsMangler = true;
		}
		if (MANGLER.equals(mangler.getTittel())) {
			containsMangler = true;
		}
		if (MANGLER.equals(mangler.getTema())) {
			containsMangler = true;
		}
		if (MANGLER.equals(mangler.getBruker())) {
			containsMangler = true;
		}
		return containsMangler;
	}

	private Status decideIfMangler(String string) {
		if (string == null || string.isEmpty()) {
			return MANGLER;
		} else {
			return MANGLER_IKKE;
		}
	}
}
