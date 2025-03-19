package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.lastOppVedlegg.LastOppVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.lastOppVedlegg.LastOppVedleggResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.TILKNYTT_NYTT_DOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.IS;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.FERDIGSTILT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.journalpost.v1.validators.DokumentValidator.validateDokument;
import static no.nav.dokarkiv.journalpost.v1.validators.LastOppVedleggValidator.validateJournalpostAndDokument;

@Service
public class LastOppVedleggService {

	private final JournalpostRepository journalpostRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final AksjonsLoggService aksjonsLoggService;

	public LastOppVedleggService(JournalpostRepository journalpostRepository,
								 DokumentFilRepository dokumentFilRepository,
								 AksjonsLoggService aksjonsLoggService) {
		this.journalpostRepository = journalpostRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.aksjonsLoggService = aksjonsLoggService;
	}

	public LastOppVedleggResponse lastOppVedlegg(long journalpostId, LastOppVedleggRequest request) {
		validateDokument(request.dokument());

		var journalpost = journalpostRepository.fetchByIdWithJournalpostDokumentInfoRelasjoner(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException("Kunne ikke finne journalpost med journalpostId=%s i joark"
						.formatted(journalpostId)));

		validateJournalpostAndDokument(journalpost, request.dokument());

		var dokumentInfo = opprettDokumentInfo(journalpost, request);
		var journalpostDokumentInfoRelasjon = opprettJournalpostDokumentInfoRelasjon(journalpost, dokumentInfo);

		journalpost.addJournalpostDokumentInfoRelasjon(journalpostDokumentInfoRelasjon);

		var dokumentFilListe = dokumentInfo.getFildetaljerListe().stream()
				.map(FilDetaljer::createDokumentFil)
				.toList();

		dokumentFilRepository.persistAll(dokumentFilListe);
		journalpostRepository.persist(journalpost);

		populerAksjonslogg(journalpost, dokumentInfo);

		return new LastOppVedleggResponse(dokumentInfo.getDokumentInfoId().toString());
	}

	private void populerAksjonslogg(Journalpost journalpost, DokumentInfo dokumentInfo) {
		var aksjonsLogg = AksjonsLoggTO.builder()
				.journalpostId(journalpost.getJournalpostId())
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.aksjon(TILKNYTT_NYTT_DOKUMENT)
				.build();

		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLogg, Collections.emptyList());
	}

	private JournalpostDokumentInfoRelasjon opprettJournalpostDokumentInfoRelasjon(Journalpost journalpost, DokumentInfo dokumentInfo) {
		var relasjon = JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetJournalpostSom(VEDLEGG)
				.journalpost(journalpost)
				.tilknyttetAvNavn(MDC.get(MDC_USER_NAME))
				.build();

		relasjon.setDokumentInfo(dokumentInfo);
		relasjon.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));

		return relasjon;
	}

	private DokumentInfo opprettDokumentInfo(Journalpost journalpost, LastOppVedleggRequest request) {
		var dokumentInfo = DokumentInfo.builder()
				.originalJournalpost(journalpost)
				.brevkode(request.dokument().getBrevkode())
				.tittel(request.dokument().getTittel())
				.kategori(IS)
				.dokumentstatus(FERDIGSTILT)
				.build();

		request.dokument().getDokumentvarianter().forEach(dokumentVariant ->
				dokumentInfo.addFilDetaljer(opprettFildetaljer(dokumentVariant)));

		dokumentInfo.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));

		return dokumentInfo;
	}

	private FilDetaljer opprettFildetaljer(DokumentVariant dokumentVariant) {
		var fildetaljer = FilDetaljer.builder()
				.filnavn(dokumentVariant.getFilnavn())
				.filtype(FilTypeCode.valueOf(dokumentVariant.getFiltype()))
				.variantFormat(VariantFormatCode.valueOf(dokumentVariant.getVariantformat()))
				.fileContent(dokumentVariant.getFysiskDokument())
				.filUuid(UUID.randomUUID().toString())
				.build();

		fildetaljer.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));

		return fildetaljer;
	}
}
