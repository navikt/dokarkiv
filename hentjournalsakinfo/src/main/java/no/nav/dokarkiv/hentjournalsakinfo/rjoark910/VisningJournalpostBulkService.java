package no.nav.dokarkiv.hentjournalsakinfo.rjoark910;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class VisningJournalpostBulkService {
	private final VisningJournalpostBulkRepository repository;

	@Inject
	public VisningJournalpostBulkService(VisningJournalpostBulkRepository repository) {
		this.repository = repository;
	}

	public VisningJournalpostBulkResponseTo visningJournalpostBulk(VisningJournalpostBulkRequestTo visningJournalpostBulkRequestTo) {
		List<Journalpost> journalposts = repository.visningJournalposter(visningJournalpostBulkRequestTo.getJournalpostIds().stream().map(Long::parseLong).collect(Collectors.toList()));
		List<JournalpostDto> journalpostDtos = journalposts.stream().map(this::map).collect(Collectors.toList());
		return new VisningJournalpostBulkResponseTo(journalpostDtos);
	}

	private JournalpostDto map(Journalpost jp) {
		return JournalpostDto.builder()
				.journalpostId(jp.getJournalpostId())
				.journalForendeEnhetId(jp.getJournalForendeEnhetId())
				.journalDato(jp.getJournalDato())
				.sendtPrintDato(jp.getSendtPrintDato())
				.innhold(jp.getInnhold())
				.fagomrade(jp.getFagomrade())
				.journalstatus(jp.getJournalstatus())
				.dokumentDato(jp.getDokumentDato())
				.avsenderMottakerId(jp.getAvsenderMottakerId())
				.journalfortAvNavn(jp.getJournalfortAvNavn())
				.mottattDato(jp.getMottattDato())
				.mottakskanal(jp.getMottakskanal())
				.utsendingskanal(jp.getUtsendingskanal())
				.ekspedertDato(jp.getEkspedertDato())
				.lestDato(jp.getLestDato())
				.mottattAdressatDato(jp.getMottattAdressatDato())
				.journalposttype(jp.getJournalposttype())
				.saksrelasjon(SaksrelasjonDto.builder()
						.sakId(jp.getSaksrelasjon().getSakId())
						.feilregistrert(jp.getSaksrelasjon().getFeilregistrert())
						.fagsystem(jp.getSaksrelasjon().getFagsystem())
						.build())
				.datoOpprettet(jp.getChangeStamp().getCreatedDate())
				.dokumenter(jp.getJournalpostDokumentInfoRelasjoner().stream().map(relasjon ->
						DokumentInfoDto.builder()
								.dokumentInfoId(relasjon.getDokumentInfo().getDokumentInfoId())
								.tittel(relasjon.getDokumentInfo().getTittel())
								.originalJournalpostId(relasjon.getDokumentInfo()
										.getOriginalJournalpost()
										.getJournalpostId())
								.build()).collect(Collectors.toList()))
				.build();
	}
}
