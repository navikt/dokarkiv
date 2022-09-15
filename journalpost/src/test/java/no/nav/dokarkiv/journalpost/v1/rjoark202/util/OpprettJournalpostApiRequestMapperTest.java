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
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static no.nav.dokarkiv.core.domain.codes.InnsynCode.VISES_MANUELT_GODKJENT;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.VISES_MASKINELT_GODKJENT;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.L;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.MIGRERING_L;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.MIGRERING_S;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.S;
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
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.SAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
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
	public void shouldMapInngaaendeJournalpost() {
		OpprettJournalpostRequest request = createRequest(JournalpostType.INNGAAENDE);
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
		assertEquals(FagsystemCode.FS22, jp.getSaksrelasjon().getFagsystem());

		JournalpostDokumentInfoRelasjon relasjon = jp.findHoveddokumentDokumentInfoRelasjon();
		assertEquals(TilknyttetJournalpostSomCode.HOVEDDOKUMENT, relasjon.getTilknyttetJournalpostSom());
		DokumentInfo dokumentInfo = relasjon.getDokumentInfo();
		assertEquals(BREVKODE1, dokumentInfo.getBrevkode());
		assertEquals(DOKUMENT_TITTEL1, dokumentInfo.getTittel());
		assertEquals(DokumentKategoriCode.SED, dokumentInfo.getKategori());

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

		FilDetaljer filDetaljer2 = dokumentInfo2.getFildetaljerListe().iterator().next();
		assertArrayEquals(FYSISK_DOKUMENT, filDetaljer2.getFileContent());
		assertNotNull(filDetaljer2.getFilUuid());
		assertEquals(FilTypeCode.PDF, filDetaljer2.getFiltype());
		assertEquals(VariantFormatCode.ARKIV, filDetaljer2.getVariantFormat());
	}

	@Test
	public void shouldMapUtgaaendeJournalpost() {
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
	public void shouldMapOverstyrInnsynsregler(String overstyrInnsynsregler, InnsynCode expected) {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.FS36)
						.overstyrInnsynsregler(overstyrInnsynsregler)
						.build())
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
	public void shoulMapDatoMottat() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.datoMottatt(DATO_MOTTATT)
				.build();
		Journalpost journalpost = mapper.map(request, null);
		assertEquals(journalpost.getMottattDato(), DATO_MOTTATT);
	}

	@Test
	public void shoulMapWithCurrentDateWhenDatoMottatIsNullAndJpTypeI() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.datoMottatt(null)
				.build();
		Journalpost journalpost = mapper.map(request, null);
		// Sjekk om det er den samme datoen som dagens dato.
		assertEquals(LocalDate.ofInstant(journalpost.getMottattDato().toInstant(), ZoneId.systemDefault()), LocalDate.now());
	}

	@Test
	public void shoulSetDatoMottatNullWhenJpTypeUtgaaende() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.UTGAAENDE)
				.datoMottatt(DATO_MOTTATT)
				.build();
		Journalpost journalpost = mapper.map(request, null);
		assertNull(journalpost.getMottattDato());
	}

	@Test
	public void shoulSetDatoMottatNullWhenJpTypeNotat() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.NOTAT)
				.datoMottatt(DATO_MOTTATT)
				.build();
		Journalpost journalpost = mapper.map(request, null);
		assertNull(journalpost.getMottattDato());
	}

	@Test
	public void shouldMapSaksrelasjonIfFagsaksystemIsValidValue() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.datoMottatt(DATO_MOTTATT)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.AO01)
						.build())
				.build();

		Journalpost journalpost = mapper.map(request, FAGSAK_ID);
		assertEquals(journalpost.getSaksrelasjon().getFagsystem(), FagsystemCode.FS22);

	}

	@Test
	public void shouldNotMapSaksrelasjonIfFagsaksystemIsAnInvalidValue() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.datoMottatt(DATO_MOTTATT)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(null)
						.build())
				.build();

		assertThrows(UgyldigInputException.class, () ->
						mapper.map(request, FAGSAK_ID),
				"Kan ikke mappe fagsystem basert på input");
	}

	@Test
	public void shouldMapSaksrelasjonIfFagSakAndFagsaksystemIsPP01() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.datoMottatt(DATO_MOTTATT)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(Fagsaksystem.PP01)
						.build())
				.build();

		Journalpost journalpost = mapper.map(request, FAGSAK_ID);
		assertEquals(journalpost.getSaksrelasjon().getFagsystem(), FagsystemCode.PEN);

	}

	@Test
	public void shouldMapSaksrelasjonIfGenerellSakAndFagsaksystemNull() {

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.datoMottatt(DATO_MOTTATT)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.GENERELL_SAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(null)
						.build())
				.build();

		Journalpost journalpost = mapper.map(request, FAGSAK_ID);
		assertEquals(journalpost.getSaksrelasjon().getFagsystem(), FagsystemCode.FS22);

	}

	@Test
	public void shouldMapNotat() {
		OpprettJournalpostRequest request = createRequest(JournalpostType.NOTAT);
		Journalpost jp = mapper.map(request, null);

		assertEquals(JournalpostTypeCode.N, jp.getJournalposttype());
		assertNull(jp.getMottakskanal());
		assertEquals(UtsendingsKanalCode.NAV_NO, jp.getUtsendingskanal());
		assertEquals(JournalStatusCode.D, jp.getJournalstatus());
	}

	@Test
	public void shouldMapJournalfoerendeEnhet() {
		OpprettJournalpostRequest request = createRequest(JournalpostType.INNGAAENDE, "9999");
		Journalpost jp = mapper.map(request, null);

		assertEquals("9999", jp.getJournalForendeEnhetId());
	}

	@Test
	public void shouldMapInngaaendeJournalpostOrganisasjon() {
		OpprettJournalpostRequest request = createRequestAvsenderMottaker(JournalpostType.INNGAAENDE, createAvsenderMottakerOrganisasjon());
		Journalpost jp = mapper.map(request, null);
		assertEquals(AvsenderMottakerIdTypeCode.ORGNR, jp.getAvsenderMottakerIdType());

	}

	@Test
	public void shouldMapInngaaendeJournalpostHelsePersonellNr() {
		OpprettJournalpostRequest request = createRequestAvsenderMottaker(JournalpostType.INNGAAENDE, createAvsenderMottakerHelsepersonell());
		Journalpost jp = mapper.map(request, null);
		assertEquals(AvsenderMottakerIdTypeCode.HPRNR, jp.getAvsenderMottakerIdType());

	}

	@Test
	public void shouldMapInngaaendeJournalpostUtlandOrganisasjon() {
		OpprettJournalpostRequest request = createRequestAvsenderMottaker(JournalpostType.INNGAAENDE, createAvsenderMottakerUtlandOrganisasjon());
		Journalpost jp = mapper.map(request, null);
		assertEquals(AvsenderMottakerIdTypeCode.UTL_ORG, jp.getAvsenderMottakerIdType());

	}

	@Test
	public void shouldMapInngaaendeJournalpostWithoutDokumentvarianter() {
		OpprettJournalpostRequest request = createBaseRequest(JournalpostType.INNGAAENDE)
				.dokumenter(List.of(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.build()))
				.build();
		Journalpost jp = mapper.map(request, null);
		assertEquals(jp.getJournalstatus(), JournalStatusCode.OD);
	}

	@Test
	public void shouldMapInngaaendeJournalpostWithDokumentvarianter() {
		OpprettJournalpostRequest request = createBaseRequest(JournalpostType.INNGAAENDE)
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
		assertEquals(jp.getJournalstatus(), JournalStatusCode.M);
	}

	@Test
	public void shouldMapKanalMigreringSToSWhenMapJournalpost() {
		Journalpost test = mapper.map(createMinimalRequestWithKanal(MIGRERING_S.toString()), null);
		assertEquals(S, test.getUtsendingskanal());
	}

	@Test
	public void shouldMapKanalMigreringLToLWhenMapJournalpost() {
		Journalpost test = mapper.map(createMinimalRequestWithKanal(MIGRERING_L.toString()), null);
		assertEquals(L, test.getUtsendingskanal());
	}

	@Test
	public void shouldMapKanalCorrectlyLWhenMapJournalpost() {
		Journalpost test = mapper.map(createMinimalRequestWithKanal(L.toString()), null);
		assertEquals(L, test.getUtsendingskanal());
	}

	@Test
	public void shouldMapNavnWhenIdTypeAndNavnNull() {
		when(identConsumerMock.hentPersonIdent(eq(AVSENDER_ID_PERSON), eq(TEMA_FOR))).thenReturn(AVSENDER_NAVN);

		OpprettJournalpostRequest request = createRequestAvsenderMottaker(JournalpostType.INNGAAENDE, createAvsenderMottakerPersonWithoutNavnAndIdType());
		Journalpost jp = mapper.map(request, null);
		assertEquals(jp.getAvsenderMottaker(), AVSENDER_NAVN);
	}

	@Test
	public void shouldMapNavnWhenIdTypeFNR() {
		when(identConsumerMock.hentPersonIdent(eq(AVSENDER_ID_PERSON), eq(TEMA_FOR))).thenReturn(AVSENDER_NAVN);

		OpprettJournalpostRequest request = createRequestAvsenderMottaker(JournalpostType.INNGAAENDE, createAvsenderMottakerPersonWithoutNavn());
		Journalpost jp = mapper.map(request, null);
		assertEquals(jp.getAvsenderMottaker(), AVSENDER_NAVN);
	}

	@Test
	public void shouldMapNavnToNullWhenIdTypeORGNR() {
		OpprettJournalpostRequest request = createRequestAvsenderMottaker(JournalpostType.INNGAAENDE, createAvsenderMottakerOrganisasjonWithoutNavn());
		Journalpost jp = mapper.map(request, null);
		assertNull(jp.getAvsenderMottaker());
	}

	@Test
	public void shoulMapdokumenttypeIdWhenBrevkode4936() {
		OpprettJournalpostRequest request = createMinimalRequestWithBrevkode(BREVKODE_4936);
		Journalpost journalpost = mapper.map(request, null);

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		assertEquals(BREVKODE_4936, dokumentInfo.getBrevkode());
		assertEquals("I000067", dokumentInfo.getDokumenttypeId());
	}
}