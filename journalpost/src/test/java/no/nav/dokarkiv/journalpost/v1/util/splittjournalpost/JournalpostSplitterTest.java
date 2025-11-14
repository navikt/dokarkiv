package no.nav.dokarkiv.journalpost.v1.util.splittjournalpost;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.splittJournalpost.SplittJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.splittJournalpost.SplittJournalpostRequest.SplittDokument;
import org.assertj.core.api.Assertions;
import org.jboss.logging.MDC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.SECONDS;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.ENDRE_DOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.KOPIER_DOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.PERSON;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.util.TestDataGenerator.BRUKER_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfo;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createHoveddokumentRelasjon;
import static no.nav.dokarkiv.core.util.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.util.splittjournalpost.JournalpostSplitter.SPLITT_JOURNALPOST_FILNAVN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class JournalpostSplitterTest {

	private static final String NY_JOURNALPOST_TITTEL = "Ny Splittet Journalpost";
	private static final String NY_EKSTERN_REFERANSE_ID = UUID.randomUUID().toString();
	private static final String CONSUMER_ID = "splitt-journalpost";
	private static final String USER_NAME = "splitt-user";
	private static final long DOKUMENT_INFO_ID = 123L;

	@BeforeEach
	void setUp() {
		MDC.put(MDC_CONSUMER_ID, CONSUMER_ID);
		MDC.put(MDC_USER_NAME, USER_NAME);
	}

	@Test
	void shouldSplittJournalpostMedDokumentUtenEndringer() {
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg();
		journalpost.clearJournalpostDokumentInfoRelasjoner();

		var dokumentInfo = createDokumentInfo();
		dokumentInfo.setDokumentInfoId(DOKUMENT_INFO_ID);
		journalpost.addJournalpostDokumentInfoRelasjon(createHoveddokumentRelasjon(journalpost, dokumentInfo));

		var request = createRequest(journalpost, createDokumenter(journalpost, true));
		var splittResultat = JournalpostSplitter.splitt(journalpost, request);

		//Sammenlign felter fra den originale journalposten
		assertThat(splittResultat.nyJournalpost())
				.usingRecursiveComparison()
				.ignoringFields("journalpostId",
						"changeStamp",
						"innhold",
						"journalDato",
						"journalstatus",
						"kanalReferanseId",
						"brukere",
						"kryssreferanser",
						"opprettetAvNavn",
						"opprettetKildeNavn",
						"journalpostDokumentInfoRelasjoner")
				.isEqualTo(journalpost);

		//Sammenlign felter som skal endres ved splitting
		assertThat(splittResultat.nyJournalpost())
				.satisfies(j -> {
					assertThat(j.getJournalpostId()).isNull();
					assertThat(j.getOpprettetAvNavn()).isEqualTo(USER_NAME);
					assertThat(j.getTilleggsopplysninger()).isEqualTo(journalpost.getTilleggsopplysninger());
					assertThat(j.getKanalReferanseId()).isEqualTo(request.eksternReferanseId());
					assertThat(j.getSaksrelasjon()).isNull();
					assertThat(j.getJournalDato()).isCloseTo(LocalDateTime.now(), within(1, SECONDS));
					assertThat(j.getJournalstatus()).isEqualTo(M);
					assertThat(j.getInnhold()).isEqualTo(NY_JOURNALPOST_TITTEL);
					assertThat(j.getJournalForendeEnhetId()).isEqualTo(journalpost.getJournalForendeEnhetId());
					assertThat(j.getFagomrade()).isEqualTo(journalpost.getFagomrade());

					assertThat(j.getBrukere())
							.singleElement()
							.extracting(no.nav.dokarkiv.core.domain.entities.Bruker::getBrukerId, no.nav.dokarkiv.core.domain.entities.Bruker::getBrukerType)
							.containsExactly(BRUKER_ID, PERSON);

					assertThat(j.getKryssreferanser()).isEqualTo(journalpost.getKryssreferanser());
					assertThat(j.getOpprettetKildeNavn()).isEqualTo(CONSUMER_ID);
				});

		assertThat(splittResultat.nyJournalpost().getJournalpostDokumentInfoRelasjoner())
				.singleElement()
				.satisfies(relasjon -> {
					assertThat(relasjon.getTilknyttetJournalpostSom()).isEqualTo(HOVEDDOKUMENT);
					assertThat(relasjon.getOpprettetKildeNavn()).isEqualTo(CONSUMER_ID);

					assertThat(relasjon.getDokumentInfo().getDokumentInfoId()).isEqualTo(request.dokumenter().getFirst().dokumentInfoId());
				});

		assertThat(splittResultat.aksjoner())
				.singleElement()
				.satisfies(aksjon -> {
					Assertions.assertThat(aksjon.getAksjon()).isEqualTo(KOPIER_DOKUMENT);
					Assertions.assertThat(aksjon.getJournalpostId()).isEqualTo(journalpost.getJournalpostId());
					Assertions.assertThat(aksjon.getDokumentInfoId()).isEqualTo(request.dokumenter().getFirst().dokumentInfoId());
				});
	}

	@Test
	void shouldSplittJournalpostMedDokumentMedEndringer() {
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg();
		journalpost.clearJournalpostDokumentInfoRelasjoner();

		var dokumentInfo = createDokumentInfo();
		dokumentInfo.setDokumentInfoId(DOKUMENT_INFO_ID);
		journalpost.addJournalpostDokumentInfoRelasjon(createHoveddokumentRelasjon(journalpost, dokumentInfo));

		var request = createRequest(journalpost, createDokumenter(journalpost, false));

		var splittResultat = JournalpostSplitter.splitt(journalpost, request);

		assertThat(splittResultat.nyJournalpost().getJournalpostDokumentInfoRelasjoner())
				.singleElement()
				.satisfies(relasjon -> {
					assertThat(relasjon.getTilknyttetJournalpostSom()).isEqualTo(HOVEDDOKUMENT);
					assertThat(relasjon.getOpprettetKildeNavn()).isEqualTo(CONSUMER_ID);
					assertThat(relasjon.getDokumentInfo())
							.satisfies(d -> {
								assertThat(d.getDokumentInfoId()).isNull();
								assertThat(d.getTittel()).isEqualTo(journalpost.findAllDokumentInfos().getFirst().getTittel());
							});

					assertThat(relasjon.getDokumentInfo().getFildetaljerListe())
							.singleElement()
							.satisfies(filDetaljer -> {
								var variant = request.dokumenter().getFirst().dokumentvarianter().getFirst();

								assertThat(filDetaljer.getFiltype()).isEqualTo(FilTypeCode.valueOf(variant.getFiltype()));
								assertThat(filDetaljer.getVariantFormat()).isEqualTo(VariantFormatCode.valueOf(variant.getVariantformat()));
								assertThat(filDetaljer.getFileContent()).isEqualTo(variant.getFysiskDokument());
								assertThat(filDetaljer.getFilnavn()).isEqualTo(SPLITT_JOURNALPOST_FILNAVN.formatted(
										journalpost.findAllDokumentInfos().getFirst().getDokumentInfoId(),
										journalpost.getJournalpostId(),
										FilTypeCode.valueOf(variant.getFiltype()).name().toLowerCase()));
							});
				});

		assertThat(splittResultat.aksjoner())
				.singleElement()
				.satisfies(aksjon -> {
					Assertions.assertThat(aksjon.getAksjon()).isEqualTo(ENDRE_DOKUMENT);
					Assertions.assertThat(aksjon.getJournalpostId()).isEqualTo(journalpost.getJournalpostId());
					Assertions.assertThat(aksjon.getDokumentInfoId()).isEqualTo(request.dokumenter().getFirst().dokumentInfoId());
				});
	}

	@Test
	void shouldSplittJournalpostMedDokumenterMedOgUtenEndringer() {
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg();
		journalpost.clearJournalpostDokumentInfoRelasjoner();

		var dokumentInfo = createDokumentInfo();
		dokumentInfo.setDokumentInfoId(DOKUMENT_INFO_ID);
		journalpost.addJournalpostDokumentInfoRelasjon(createHoveddokumentRelasjon(journalpost, dokumentInfo));

		var request = createRequest(journalpost, Stream.concat(
						createDokumenter(journalpost, false).stream(),
						createDokumenter(journalpost, true).stream())
				.toList());

		var splittResultat = JournalpostSplitter.splitt(journalpost, request);

		assertThat(splittResultat.nyJournalpost().getJournalpostDokumentInfoRelasjoner())
				.hasSize(2)
				.extracting(JournalpostDokumentInfoRelasjon::getDokumentInfo)
				.extracting(DokumentInfo::getDokumentInfoId)
				.containsExactlyInAnyOrder(DOKUMENT_INFO_ID, null);
	}

	private static SplittJournalpostRequest createRequest(Journalpost journalpost, List<SplittDokument> dokumenter) {
		return new SplittJournalpostRequest(
				journalpost.getFagomrade().name(),
				createBruker(),
				NY_JOURNALPOST_TITTEL,
				journalpost.getJournalForendeEnhetId(),
				NY_EKSTERN_REFERANSE_ID,
				dokumenter);
	}

	private static Bruker createBruker() {
		return Bruker.builder()
				.id(BRUKER_ID)
				.idType(FNR)
				.build();
	}

	private static List<SplittDokument> createDokumenter(Journalpost journalpost, boolean kopierUtenEndringer) {
		var varianter = List.of(DokumentVariant.builder()
				.filtype(PDF.name())
				.variantformat(ARKIV.name())
				.fysiskDokument("abc".getBytes())
				.build());

		return List.of(new SplittDokument(
				journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0).getDokumentInfoId(),
				kopierUtenEndringer,
				kopierUtenEndringer ? null : varianter));
	}

}