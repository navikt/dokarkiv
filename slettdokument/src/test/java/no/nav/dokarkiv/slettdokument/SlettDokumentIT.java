package no.nav.dokarkiv.slettdokument;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.transaction.TestTransaction;

import javax.inject.Inject;
import java.util.Date;

public class SlettDokumentIT extends AbstractSlettDokumentIT {

	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	// Egen
	private static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	private static final String OPPRETTET_AV_NAVN = "Opprettet navn";
	private static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	private static final String ENDRET_AV_NAVN = "Endret av navn";
	private static final String AVSENDER_MOTTAKER_ID = "***gammelt_fnr***";
	private static final String DOKUMENT_TITTEL = "SlettDokumentTittel";
	private static final String BREVGRUPPE = "Brevgruppe";
	private static final String BREVKODE = "Brevkode";
	private static final String FILNAVN = "filNavn";
	private static final String URL_SLETTDOKUMENT = "/rest/slettdokument/";

	@Test
	public void shouldDeleteDocumentInJoark() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(createJournalpostBuilder().build());
		Journalpost journalpost2 = joarkRepository.save(createJournalpostBuilder().build());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		restTemplate.exchange("/rest/slettdokument/" + journalpost1.getJournalpostId() + "/"
				+ journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(), HttpMethod.DELETE, createHeaders(), String.class);
		restTemplate.exchange("/rest/slettdokument/" + journalpost2.getJournalpostId()
				.toString(), HttpMethod.DELETE, createHeaders(), String.class);

		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
				journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())
				.get(0)
				.getDokumentInfo()
				.getSlettet(), true);
		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
				journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())
				.get(0)
				.getDokumentInfo()
				.getSlettet(), true);
	}

	@Test
	public void shouldFailToDeleteDocumentInJoark() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(createJournalpostBuilder().build());
		Journalpost journalpost2 = createJournalpostBuilder().dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo())
				.build()).build();
		joarkRepository.save(journalpost2);
		Journalpost journalpost3 = joarkRepository.save(createJournalpostBuilder().build());
		Journalpost journalpost4 = createJournalpostBuilder().dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(journalpost3.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo())
				.build()).build();
		joarkRepository.save(journalpost4);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		restTemplate.delete("/rest/slettdokument/" + journalpost1.getJournalpostId() + "/"
				+ journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(), String.class);
		restTemplate.delete("/rest/slettdokument/" + journalpost3.getJournalpostId(), String.class);


		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
				journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())
				.get(0)
				.getDokumentInfo()
				.getSlettet(), false);
		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
				journalpost3.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())
				.get(0)
				.getDokumentInfo()
				.getSlettet(), false);

	}

	@Test
	public void shouldFailToDeleteDocumentInJoarkBecauseDocumentAlreadyDeleted() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(createJournalpostBuilder().build());
		Journalpost journalpost2 = joarkRepository.save(createJournalpostBuilder().build());

		journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setSlettet(true);
		journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setSlettet(true);

		dokumentinfoRepository.save(journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());
		dokumentinfoRepository.save(journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());

		TestTransaction.flagForCommit();
		TestTransaction.end();


		restTemplate.delete("/rest/slettdokument/" + journalpost1.getJournalpostId() + "/"
				+ journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(), String.class);
		restTemplate.delete("/rest/slettdokument/" + journalpost2.getJournalpostId(), String.class);


		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
				journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())
				.get(0)
				.getDokumentInfo()
				.getSlettet(), true);
		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
				journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())
				.get(0)
				.getDokumentInfo()
				.getSlettet(), true);
	}

//	@Test
//	public void shouldFailToDeleteDocumentInJoarkBecauseOfExceptions() {
//		Journalpost journalpost1 = joarkRepository.save(createJournalpostBuilder().build());
//		Journalpost journalpost2 = joarkRepository.save(createJournalpostBuilder().build());
//		Journalpost journalpost3 = joarkRepository.save(createJournalpostBuilder().build());
//
//		TestTransaction.flagForCommit();
//		TestTransaction.end();
//
//		//Test av DocumentNotFoundException i slettDokumentMedJournalpostId
////		testRestTemplate.delete();
//
//
////		ResponseEntity<GetJournalpostResponse> responseEntity = testRestTemplate.exchange(
////				SLETTDOKUMENT + "168", HttpMethod.DELETE, createHeaders(), getJournalpostResponse.class);
//
//
////				testRestTemplate.delete("/rest/slettdokument/168", String.class);
//
//		//Test av DocumentNotFoundException i gyldigtAntallRelasjonerForSlettingAvEttDokument
//
//		//Test av ForMangeJournalpostDokumentInfoRelasjonerException gyldigtAntallRelasjonerForSlettingAvEttDokument
//
//		//Test av IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException i gyldigInputForJournalpostId
//
//		//Test av JournalpostIkkeFunnetException i gyldigInputForJournalpostId
//
//		//Test av DokumentAlleredeSlettetException i gyldigSletteStatusForDokument
//
//
//		// Missmatch mellom journalpostId og dokumentInfoId
//		testRestTemplate.delete("/rest/slettdokument/"+ journalpost1.getJournalpostId() + "/"
//				+ journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(), String.class);
//
//		testRestTemplate.delete("/rest/slettdokument/3618636/"
//				+ journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(), String.class);
//
//		testRestTemplate.delete("/rest/slettdokument/" + journalpost3.getJournalpostId() + "/6468613", String.class);
//
//		// tester deleteDocumentWithJournalpostId i SlettDokumentController
//		testRestTemplate.delete("/rest/slettdokument/1618625635", String.class);
//
//
//		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
//				journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId()).get(0).getDokumentInfo().getSlettet(), true);
//		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
//				journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId()).get(0).getDokumentInfo().getSlettet(), true);
//	}

	private JournalpostBuilder createJournalpostBuilder() {
		return JournalpostBuilder.getJournalpostBuilder()
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.dokumentDato(new Date())
				.journalStatus(JournalStatusCode.FS)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.fagomrade(FagomradeCode.RPO)
				.saksrelasjon(
						SaksrelasjonTestDataProvider.createSaksrelasjon().build())
				.brukere(
						BrukerTestDataProvider.createBruker().build())
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(createDokumentInfo().build())
								.build());
	}

	private DokumentInfoBuilder createDokumentInfo() {
		return getDokumentInfoBuilder()
				.slettet(false)
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.tittel(DOKUMENT_TITTEL)
				.endretAvNavn(ENDRET_AV_NAVN)
				.brevgruppe(BREVGRUPPE)
				.brevkode(BREVKODE)
				.filDetaljerList(createFildetaljer())
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN);
	}

	private FilDetaljer createFildetaljer() {
		return createFildetaljer(FilDetaljer.generateUuid());
	}

	private FilDetaljer createFildetaljer(String filUuid) {
		return FilDetaljerBuilder.getFilDetaljerBuilder()
				.filUuid(filUuid)
				.filnavn(FILNAVN)
				.filtype(FilTypeCode.PDF)
				.variantFormat(VariantFormatCode.ARKIV)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.build();
	}
}
