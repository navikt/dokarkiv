package no.nav.dokarkiv.journalpost.v1.rjoark202.util;

import no.nav.dokarkiv.core.consumer.pdl.IdentConsumer;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.mappers.OpprettJournalpostApiRequestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.MINUTES;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.VISES_MANUELT_GODKJENT;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.VISES_MASKINELT_GODKJENT;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.L;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.MIGRERING_L;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.MIGRERING_S;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.S;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.INNGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_MOTTAKER_LAND;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BATCHNAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BEHANDLINGSTEMA;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BREVKODE1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BREVKODE2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BREVKODE_4936;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DATO_MOTTATT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTKATEGORI_SED;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT_2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.INNHOLD;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.KANALREFERANSE_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.PENSJON_FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.SAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_PEN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_TIL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TILLEGGSOPPLYSNING_NOKKEL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TILLEGGSOPPLYSNING_VERDI;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottakerHelsepersonell;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottakerOrganisasjon;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottakerOrganisasjonWithoutNavn;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottakerPersonWithoutNavn;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottakerPersonWithoutNavnAndIdType;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createAvsenderMottakerUtlandOrganisasjon;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createBaseRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequestWithBrevkode;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequestWithKanal;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequestAvsenderMottaker;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OpprettJournalpostApiRequestMapperTest {

	@Mock
	private IdentConsumer identConsumerMock;

	@InjectMocks
	private OpprettJournalpostApiRequestMapper mapper;

	@Test
	void shouldMapInngaaendeJournalpost() {
		OpprettJournalpostRequest request = createRequest(INNGAAENDE);
		Journalpost jp = mapper.map(request, null);

		assertEquals(JournalpostTypeCode.I, jp.getJournalposttype());
		assertEquals(JournalStatusCode.M, jp.getJournalstatus());
		assertEquals(AVSENDER_ID_PERSON, jp.getAvsenderMottakerId());
		assertEquals(AVSENDER_NAVN, jp.getAvsenderMottaker());
		assertEquals(AVSENDER_MOTTAKER_LAND, jp.getLand());
		assertEquals(BRUKER_ID_PERSON, jp.getBrukere().iterator().next().getBrukerId());
		assertEquals(BrukerTypeCode.PERSON, jp.getBrukere().iterator().next().getBrukerType());
		assertEquals(FagomradeCode.FOR, jp.getFagomrade());
		assertEquals(BEHANDLINGSTEMA, jp.getBehandlingstema());
		assertEquals(INNHOLD, jp.getInnhold());
		assertEquals(MottaksKanalCode.NAV_NO, jp.getMottakskanal());
		assertNull(jp.getUtsendingskanal());
		assertEquals(KANALREFERANSE_ID, jp.getKanalReferanseId());
		assertEquals(TILLEGGSOPPLYSNING_NOKKEL, jp.getTilleggsopplysninger().keySet().iterator().next());
		assertEquals(TILLEGGSOPPLYSNING_VERDI, jp.getTilleggsopplysninger().values().iterator().next());
		assertEquals(SAK_ID, jp.getSaksrelasjon().getSakId());
		assertEquals(FagsystemCode.PEN, jp.getSaksrelasjon().getFagsystem());

		JournalpostDokumentInfoRelasjon relasjon = jp.findHoveddokumentDokumentInfoRelasjon();
		assertEquals(TilknyttetJournalpostSomCode.HOVEDDOKUMENT, relasjon.getTilknyttetJournalpostSom());
		DokumentInfo dokumentInfo = relasjon.getDokumentInfo();
		assertEquals(BREVKODE1, dokumentInfo.getBrevkode());
		assertEquals(DOKUMENT_TITTEL1, dokumentInfo.getTittel());
		assertEquals(DokumentKategoriCode.SED, dokumentInfo.getKategori());
		assertNull(dokumentInfo.getSensitivt());

		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		assertArrayEquals(FYSISK_DOKUMENT, filDetaljer.getFileContent());
		assertNotNull(filDetaljer.getFilUuid());
		assertEquals(FilTypeCode.PDF, filDetaljer.getFiltype());
		assertEquals(VariantFormatCode.ARKIV, filDetaljer.getVariantFormat());
		assertEquals(BATCHNAVN, filDetaljer.getBatchNavn());

		FilDetaljer filDetaljerOriginal = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ORIGINAL);
		assertArrayEquals(FYSISK_DOKUMENT_2, filDetaljerOriginal.getFileContent());
		assertNotNull(filDetaljerOriginal.getFilUuid());
		assertEquals(FilTypeCode.XML, filDetaljerOriginal.getFiltype());
		assertEquals(VariantFormatCode.ORIGINAL, filDetaljerOriginal.getVariantFormat());

		JournalpostDokumentInfoRelasjon relasjon2 = jp.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).iterator().next();
		assertEquals(TilknyttetJournalpostSomCode.VEDLEGG, relasjon2.getTilknyttetJournalpostSom());
		DokumentInfo dokumentInfo2 = relasjon2.getDokumentInfo();
		assertEquals(BREVKODE2, dokumentInfo2.getBrevkode());
		assertEquals(DOKUMENT_TITTEL2, dokumentInfo2.getTittel());
		assertEquals(DokumentKategoriCode.SED, dokumentInfo2.getKategori());
		assertNull(dokumentInfo2.getSensitivt());

		FilDetaljer filDetaljer2 = dokumentInfo2.getFildetaljerListe().iterator().next();
		assertArrayEquals(FYSISK_DOKUMENT, filDetaljer2.getFileContent());
		assertNotNull(filDetaljer2.getFilUuid());
		assertEquals(FilTypeCode.PDF, filDetaljer2.getFiltype());
		assertEquals(VariantFormatCode.ARKIV, filDetaljer2.getVariantFormat());
	}

	@Test
	void shouldMapUtgaaendeJournalpost() {
		OpprettJournalpostRequest request = createRequest(JournalpostType.UTGAAENDE);
		Journalpost jp = mapper.map(request, null);

		assertEquals(JournalpostTypeCode.U, jp.getJournalposttype());
		assertNull(jp.getMottakskanal());
		assertEquals(UtsendingsKanalCode.NAV_NO, jp.getUtsendingskanal());
		assertEquals(JournalStatusCode.D, jp.getJournalstatus());

		JournalpostDokumentInfoRelasjon relasjon = jp.findHoveddokumentDokumentInfoRelasjon();
		JournalpostDokumentInfoRelasjon relasjon2 = jp.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).iterator().next();
		assertEquals(DokumentStatusCode.FERDIGSTILT, relasjon.getDokumentInfo().getDokumentstatus());
		assertEquals(DokumentStatusCode.FERDIGSTILT, relasjon2.getDokumentInfo().getDokumentstatus());
	}

	@ParameterizedTest
	@MethodSource
	void shouldMapOverstyrInnsynsregler(String overstyrInnsynsregler, InnsynCode expected) {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(PENSJON_FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.PP01)
						.build())
				.overstyrInnsynsregler(overstyrInnsynsregler)
				.build();
		Journalpost journalpost = mapper.map(request, null);
		assertEquals(expected, journalpost.getInnsyn());
	}

	private static Stream<Arguments> shouldMapOverstyrInnsynsregler() {
		return Stream.of(
				Arguments.of(null, null),
				Arguments.of("VISES_MASKINELT_GODKJENT", VISES_MASKINELT_GODKJENT),
				Arguments.of("VISES_MANUELT_GODKJENT", VISES_MANUELT_GODKJENT)
		);
	}

	@Test
	void shoulMapDatoMottat() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.datoMottatt(DATO_MOTTATT)
				.build();
		Journalpost journalpost = mapper.map(request, null);
		assertEquals(DATO_MOTTATT, journalpost.getMottattDato());
	}

	@Test
	void shoulMapWithCurrentDateWhenDatoMottatIsNullAndJpTypeI() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.datoMottatt(null)
				.build();
		Journalpost journalpost = mapper.map(request, null);
		assertThat(journalpost.getMottattDato()).isCloseTo(LocalDateTime.now(), within(1, MINUTES));
	}

	@Test
	void shoulSetDatoMottatNullWhenJpTypeUtgaaende() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.UTGAAENDE)
				.datoMottatt(DATO_MOTTATT)
				.build();
		Journalpost journalpost = mapper.map(request, null);
		assertNull(journalpost.getMottattDato());
	}

	@Test
	void shoulSetDatoMottatNullWhenJpTypeNotat() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.NOTAT)
				.datoMottatt(DATO_MOTTATT)
				.build();
		Journalpost journalpost = mapper.map(request, null);
		assertNull(journalpost.getMottattDato());
	}

	@Test
	void shouldMapSaksrelasjonIfFagsaksystemIsValidValue() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.datoMottatt(DATO_MOTTATT)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.AO01)
						.build())
				.build();

		Journalpost journalpost = mapper.map(request, SAK_ID);
		assertEquals(FagsystemCode.FS22, journalpost.getSaksrelasjon().getFagsystem());
	}

	@Test
	void shouldNotMapSaksrelasjonIfFagsaksystemIsAnInvalidValue() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.datoMottatt(DATO_MOTTATT)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(null)
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> mapper.map(request, SAK_ID));
		assertThat(exception.getMessage()).contains(
				"""
						Kan ikke legge saksrelasjon til journalpost. For fagsaker og generelle saker må en av følgende regler være oppfylt:
						1) sakstype er FAGSAK og fagsaksystem er PP01
						2) sakstype er FAGSAK eller GENERELL_SAK, og fagsaksystem er ikke PP01
						Mottatt: sakstype=FAGSAK, fagsaksystem=null
						""");
	}

	@Test
	void shouldMapSaksrelasjonIfFagSakAndFagsaksystemIsPP01() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.datoMottatt(DATO_MOTTATT)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.PP01)
						.build())
				.build();

		Journalpost journalpost = mapper.map(request, SAK_ID);
		assertEquals(FagsystemCode.PEN, journalpost.getSaksrelasjon().getFagsystem());

	}

	@Test
	void shouldMapSaksrelasjonIfGenerellSakAndFagsaksystemNull() {

		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.datoMottatt(DATO_MOTTATT)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.GENERELL_SAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(null)
						.build())
				.build();

		Journalpost journalpost = mapper.map(request, SAK_ID);
		assertEquals(FagsystemCode.FS22, journalpost.getSaksrelasjon().getFagsystem());

	}

	@Test
	void shouldMapNotat() {
		OpprettJournalpostRequest request = createRequest(JournalpostType.NOTAT);
		Journalpost jp = mapper.map(request, null);

		assertEquals(JournalpostTypeCode.N, jp.getJournalposttype());
		assertNull(jp.getMottakskanal());
		assertEquals(UtsendingsKanalCode.NAV_NO, jp.getUtsendingskanal());
		assertEquals(JournalStatusCode.D, jp.getJournalstatus());
	}

	@Test
	void shouldMapJournalfoerendeEnhet() {
		OpprettJournalpostRequest request = createRequest(INNGAAENDE, "9999");
		Journalpost jp = mapper.map(request, null);

		assertEquals("9999", jp.getJournalForendeEnhetId());
	}

	@Test
	void shouldMapInngaaendeJournalpostOrganisasjon() {
		OpprettJournalpostRequest request = createRequestAvsenderMottaker(INNGAAENDE, createAvsenderMottakerOrganisasjon());
		Journalpost jp = mapper.map(request, null);
		assertEquals(AvsenderMottakerIdTypeCode.ORGNR, jp.getAvsenderMottakerIdType());
	}

	@Test
	void shouldMapInngaaendeJournalpostHelsePersonellNr() {
		OpprettJournalpostRequest request = createRequestAvsenderMottaker(INNGAAENDE, createAvsenderMottakerHelsepersonell());
		Journalpost jp = mapper.map(request, null);
		assertEquals(AvsenderMottakerIdTypeCode.HPRNR, jp.getAvsenderMottakerIdType());
	}

	@Test
	void shouldMapInngaaendeJournalpostUtlandOrganisasjon() {
		OpprettJournalpostRequest request = createRequestAvsenderMottaker(INNGAAENDE, createAvsenderMottakerUtlandOrganisasjon());
		Journalpost jp = mapper.map(request, null);
		assertEquals(AvsenderMottakerIdTypeCode.UTL_ORG, jp.getAvsenderMottakerIdType());
	}

	@Test
	void shouldMapInngaaendeJournalpostWithDokumentvarianter() {
		OpprettJournalpostRequest request = createBaseRequest(INNGAAENDE)
				.dokumenter(List.of(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.dokumentvarianter(Collections.singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument(FYSISK_DOKUMENT)
										.build()))
								.build()))
				.build();
		Journalpost jp = mapper.map(request, null);
		assertEquals(JournalStatusCode.M, jp.getJournalstatus());
	}

	@Test
	void shouldMapKanalMigreringSToSWhenMapJournalpost() {
		Journalpost test = mapper.map(createMinimalRequestWithKanal(MIGRERING_S.toString()), null);
		assertEquals(S, test.getUtsendingskanal());
	}

	@Test
	void shouldMapKanalMigreringLToLWhenMapJournalpost() {
		Journalpost test = mapper.map(createMinimalRequestWithKanal(MIGRERING_L.toString()), null);
		assertEquals(L, test.getUtsendingskanal());
	}

	@Test
	void shouldMapKanalCorrectlyLWhenMapJournalpost() {
		Journalpost test = mapper.map(createMinimalRequestWithKanal(L.toString()), null);
		assertEquals(L, test.getUtsendingskanal());
	}

	@Test
	void shouldMapNavnWhenIdTypeAndNavnNull() {
		when(identConsumerMock.hentPersonnavn(eq(AVSENDER_ID_PERSON), eq(TEMA_FOR))).thenReturn(AVSENDER_NAVN);

		OpprettJournalpostRequest request = createRequestAvsenderMottaker(INNGAAENDE, createAvsenderMottakerPersonWithoutNavnAndIdType());
		Journalpost jp = mapper.map(request, null);
		assertEquals(AVSENDER_NAVN, jp.getAvsenderMottaker());
	}

	@Test
	void shouldMapNavnWhenIdTypeFNR() {
		when(identConsumerMock.hentPersonnavn(eq(AVSENDER_ID_PERSON), eq(TEMA_FOR))).thenReturn(AVSENDER_NAVN);

		OpprettJournalpostRequest request = createRequestAvsenderMottaker(INNGAAENDE, createAvsenderMottakerPersonWithoutNavn());
		Journalpost jp = mapper.map(request, null);
		assertEquals(AVSENDER_NAVN, jp.getAvsenderMottaker());
	}

	@Test
	void shouldMapNavnToNullWhenIdTypeORGNR() {
		OpprettJournalpostRequest request = createRequestAvsenderMottaker(INNGAAENDE, createAvsenderMottakerOrganisasjonWithoutNavn());
		Journalpost jp = mapper.map(request, null);
		assertNull(jp.getAvsenderMottaker());
	}

	@Test
	void shoulMapdokumenttypeIdWhenBrevkode4936() {
		OpprettJournalpostRequest request = createMinimalRequestWithBrevkode(BREVKODE_4936);
		Journalpost journalpost = mapper.map(request, null);

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		assertEquals(BREVKODE_4936, dokumentInfo.getBrevkode());
		assertEquals("I000067", dokumentInfo.getDokumenttypeId());
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void shouldMapSensitivtPselv(boolean value) {
		OpprettJournalpostRequest request = createBaseRequest(INNGAAENDE)
				.dokumenter(List.of(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.sensitivtPselv(value)
								.build()))
				.build();
		Journalpost jp = mapper.map(request, null);
		assertEquals(jp.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getSensitivt(), value);
	}


	@Test
	void shoulMapHoveddokumentWhenAssignedRekkefoelge() {
		OpprettJournalpostRequest request = createBaseRequest(INNGAAENDE)
				.dokumenter(List.of(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL2)
								.brevkode(BREVKODE2)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.dokumentvarianter(List.of(
										DokumentVariant.builder()
												.filtype(FILTYPE_PDF)
												.fysiskDokument(FYSISK_DOKUMENT_2)
												.variantformat(VARIANTFORMAT_ARKIV)
												.build()))
								.build(),
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.rekkefoelge(1L)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.dokumentvarianter(List.of(
										DokumentVariant.builder()
												.filtype(FILTYPE_PDF)
												.fysiskDokument(FYSISK_DOKUMENT)
												.variantformat(VARIANTFORMAT_ARKIV)
												.build()))
								.build()
				)).build();
		Journalpost journalpost = mapper.map(request, null);

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		assertEquals(DOKUMENT_TITTEL2, dokumentInfo.getTittel());
		assertEquals(BREVKODE2, dokumentInfo.getBrevkode());
		assertEquals(1L, dokumentInfo.getRekkefoelge());
	}
}