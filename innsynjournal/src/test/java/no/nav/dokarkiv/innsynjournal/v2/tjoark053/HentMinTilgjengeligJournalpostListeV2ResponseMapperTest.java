package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder;
import no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.util.DateConverterUtil;
import no.nav.dokarkiv.innsynjournal.v2.InnsynJournalpostTo;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.AvsenderMottaker;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.DokumentInnhold;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.DokumentinfoRelasjon;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.InnsynDokument;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Sak;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.SkannetInnhold;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Unit tests for {@link HentMinTilgjengeligJournalpostListeV2ResponseMapper}
 *
 * @author Ketill Fenne, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class HentMinTilgjengeligJournalpostListeV2ResponseMapperTest {

	private static final String AVSENDER_MOTTAKER = "avsendermottaker";
	private static final FagomradeCode FAGOMRADE = FagomradeCode.PEN;
	private static final String SAKS_ID = "id";
	private static final FagsystemCode FAGSYSTEM = FagsystemCode.FS22;
	private static final Long DOK_INFO_REL_ID = 3L;
	private static final String DOKUMENT_TITTEL = "tittel";
	private static final VariantFormatCode VARIANT_FORMAT = VariantFormatCode.ARKIV;
	private static final FilTypeCode FIL_TYPE = FilTypeCode.PDF;
	private static final Long SKANNET_INNHOLD_ID = 2L;
	private static final String VEDLEGG_INNHOLD = "vedleggInnhold";
	private static final Long DOKUMENT_INFO_ID = 3L;
	private static final Long JOURNALPOST_ID1 = 0L;
	private static final Long JOURNALPOST_ID2 = 1L;
	private static final JournalpostTypeCode JOURNALPOST_TYPE = JournalpostTypeCode.U;
	private static final Date JOURNAL_DATO = Date.from(LocalDate.of(2014, Month.JANUARY, 1)
			.atStartOfDay(ZoneId.systemDefault())
			.toInstant());
	private static final Date MOTTATT_DATO = Date.from(LocalDate.of(2015, Month.JANUARY, 1)
			.atStartOfDay(ZoneId.systemDefault())
			.toInstant());
	private static final DokumentStatusCode DOKUMENT_STATUS = DokumentStatusCode.FERDIGSTILT;
	private static final Date DOKUMENT_FERDIG_DATO = new Date(1L);
	private static final Date DOKUMENT_FERDIG_DATO_OLD = new Date(0L);
	private static final Date CHANGE_STAMP_DATE = new Date(0L);
	private static final String KANAL_REFERANSE_ID = "kanalReferanseId";

	private HentMinTilgjengeligJournalpostListeV2ResponseMapper mapper;

	@Mock
	private SkjermingService skjermingService;

	@Before
	public void setUp() throws Exception {
		mapper = new HentMinTilgjengeligJournalpostListeV2ResponseMapper();
	}

	@Test
	public void shouldMap() {
		Journalpost journalpost1 = createJournalpost(JOURNALPOST_ID1);
		journalpost1.setJournalposttype(JournalpostTypeCode.I);
		journalpost1.setMottakskanal(MottaksKanalCode.ALTINN);
		Journalpost journalpost2 = createJournalpost(JOURNALPOST_ID2);
		journalpost2.setJournalposttype(JournalpostTypeCode.U);
		journalpost2.setUtsendingskanal(UtsendingsKanalCode.EESSI);
		List<Journalpost> journalposts = Lists.newArrayList(journalpost1, journalpost2);
		HentTilgjengeligJournalpostListeResponse response = mapper.mapList(createInnsynJournalpostToList(journalposts));
		assertThat(response.getJournalpostListe(), hasSize(2));
		for (no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost journalpost : response.getJournalpostListe()) {
			assertThat(journalpost.getArkivtema().getValue(), is(FAGOMRADE.name()));
			assertThat(journalpost.getEksternPart(), is(AVSENDER_MOTTAKER));
			if (JOURNALPOST_ID1.equals(Long.valueOf(journalpost.getJournalpostId()))) {
				assertThat(Long.valueOf(journalpost.getJournalpostId()), is(JOURNALPOST_ID1));
				assertThat(journalpost.getKommunikasjonsretning().getValue(), is(JournalpostTypeCode.I.name()));
				assertThat(journalpost.getKommunikasjonskanal(), is(MottaksKanalCode.ALTINN.name()));
			} else {
				assertThat(Long.valueOf(journalpost.getJournalpostId()), is(JOURNALPOST_ID2));
				assertThat(journalpost.getKommunikasjonsretning().getValue(), is(JournalpostTypeCode.U.name()));
				assertThat(journalpost.getKommunikasjonskanal(), is(UtsendingsKanalCode.EESSI.name()));
			}
			assertThat(journalpost.getKanalReferanseId(), is(KANAL_REFERANSE_ID));
			assertThat(journalpost.getMottatt(), is(DateConverterUtil.convertDateToXMLGregorianCalendar(MOTTATT_DATO)));
			assertThat(journalpost.getSendt(), is(DateConverterUtil.convertDateToXMLGregorianCalendar(JOURNAL_DATO)));
			assertThat(journalpost.getFerdigstilt(), is(DateConverterUtil.convertDateToXMLGregorianCalendar(JOURNAL_DATO)));
			assertThat(journalpost.getOpprettet(), is(DateConverterUtil.convertDateToXMLGregorianCalendar(CHANGE_STAMP_DATE)));
			assertThat(journalpost.getBrukerErAvsenderMottaker(), is(AvsenderMottaker.JA));
			assertSak(journalpost.getGjelderSak());
			assertDokInfoRels(journalpost.getDokumentinfoRelasjonListe(), 2);
		}
	}

	@Test
	public void shouldMapKassert() {
		Journalpost journalpost1 = createJournalpostKassert(JOURNALPOST_ID1);
		journalpost1.setJournalposttype(JournalpostTypeCode.I);
		journalpost1.setMottakskanal(MottaksKanalCode.ALTINN);
		Journalpost journalpost2 = createJournalpostKassert(JOURNALPOST_ID2);
		journalpost2.setJournalposttype(JournalpostTypeCode.U);
		journalpost2.setUtsendingskanal(UtsendingsKanalCode.EESSI);
		List<Journalpost> journalposts = Lists.newArrayList(journalpost1, journalpost2);
		HentTilgjengeligJournalpostListeResponse response = mapper.mapList(createInnsynJournalpostToList(journalposts));
		assertThat(response.getJournalpostListe(), hasSize(2));
		for (no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost journalpost : response.getJournalpostListe()) {
			assertThat(journalpost.getArkivtema().getValue(), is(FAGOMRADE.name()));
			assertThat(journalpost.getEksternPart(), is(AVSENDER_MOTTAKER));
			if (JOURNALPOST_ID1.equals(Long.valueOf(journalpost.getJournalpostId()))) {
				assertThat(Long.valueOf(journalpost.getJournalpostId()), is(JOURNALPOST_ID1));
				assertThat(journalpost.getKommunikasjonsretning().getValue(), is(JournalpostTypeCode.I.name()));
				assertThat(journalpost.getKommunikasjonskanal(), is(MottaksKanalCode.ALTINN.name()));
			} else {
				assertThat(Long.valueOf(journalpost.getJournalpostId()), is(JOURNALPOST_ID2));
				assertThat(journalpost.getKommunikasjonsretning().getValue(), is(JournalpostTypeCode.U.name()));
				assertThat(journalpost.getKommunikasjonskanal(), is(UtsendingsKanalCode.EESSI.name()));
			}
			assertThat(journalpost.getKanalReferanseId(), is(KANAL_REFERANSE_ID));
			assertThat(journalpost.getMottatt(), is(DateConverterUtil.convertDateToXMLGregorianCalendar(MOTTATT_DATO)));
			assertThat(journalpost.getSendt(), is(DateConverterUtil.convertDateToXMLGregorianCalendar(JOURNAL_DATO)));
			assertThat(journalpost.getFerdigstilt(), is(DateConverterUtil.convertDateToXMLGregorianCalendar(JOURNAL_DATO)));
			assertThat(journalpost.getOpprettet(), is(DateConverterUtil.convertDateToXMLGregorianCalendar(CHANGE_STAMP_DATE)));
			assertThat(journalpost.getBrukerErAvsenderMottaker(), is(AvsenderMottaker.JA));
			assertSak(journalpost.getGjelderSak());


			assertThat(journalpost.getDokumentinfoRelasjonListe().get(0).getJournalfoertDokument().getBeskriverInnhold().getVariantformat().getValue(), is("ARKIV"));
			assertThat(journalpost.getDokumentinfoRelasjonListe().get(1).getJournalfoertDokument().getBeskriverInnhold().getVariantformat().getValue(), is("ARKIV"));
		}
	}

	@Test
	public void shouldSortResponse() throws Exception {
		Journalpost jounalpost = getJournalpostBuilder()
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.journalpostDokumentInfoRelasjonId(5L)
								.dokumentInfo(createSimpleDokumentInfo())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.journalpostDokumentInfoRelasjonId(3L)
								.dokumentInfo(createSimpleDokumentInfo())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.journalpostDokumentInfoRelasjonId(1L)
								.dokumentInfo(createSimpleDokumentInfo())
								.build())
				.build();

		no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost mapped
				= mapper.map(createInnsynJournalpost(jounalpost, InnsynJournalpostTo.DokumentInnsyn.JA, InnsynJournalpostTo.AvsenderMottaker.JA));

		assertThat(mapped.getDokumentinfoRelasjonListe(), hasSize(3));
		assertThat(mapped.getDokumentinfoRelasjonListe().get(0).getDokumentinfoRelasjonId(), is(String.valueOf(3L)));
		assertThat(mapped.getDokumentinfoRelasjonListe().get(1).getDokumentinfoRelasjonId(), is(String.valueOf(1L)));
		assertThat(mapped.getDokumentinfoRelasjonListe().get(2).getDokumentinfoRelasjonId(), is(String.valueOf(5L)));
	}

	@Test
	public void sortingShouldHandleEmptyResponse() throws Exception {
		Journalpost journalpost = new Journalpost();

		no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost mapped =
				mapper.map(createInnsynJournalpost(journalpost, InnsynJournalpostTo.DokumentInnsyn.JA, InnsynJournalpostTo.AvsenderMottaker.JA));

		assertThat(mapped.getDokumentinfoRelasjonListe(), is(empty()));
	}

	@Test
	public void shouldMapEkspederDatoToSendtDato() {
		Journalpost journalpost = createJournalpost(JOURNALPOST_ID1);
		Date ekspedertDato = new Date(0L);
		journalpost.setEkspedertDato(ekspedertDato);
		no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost mapped
				= mapper.map(createInnsynJournalpost(journalpost, InnsynJournalpostTo.DokumentInnsyn.JA, InnsynJournalpostTo.AvsenderMottaker.JA));
		assertThat(mapped.getSendt(), is(DateConverterUtil.convertDateToXMLGregorianCalendar(ekspedertDato)));
	}

	@Test
	public void shouldMapDatoSendtPrintToSendtDato() {
		Journalpost journalpost = createJournalpost(JOURNALPOST_ID1);
		Date sendtPrint = new Date(0L);
		journalpost.setSendtPrintDato(sendtPrint);
		no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost mapped
				= mapper.map(createInnsynJournalpost(journalpost, InnsynJournalpostTo.DokumentInnsyn.JA, InnsynJournalpostTo.AvsenderMottaker.JA));
		assertThat(mapped.getSendt(), is(DateConverterUtil.convertDateToXMLGregorianCalendar(sendtPrint)));
	}

	@Test
	public void shouldMapNewestDokInfoFerdigStiltDateToFerdigstiltDato() {
		Journalpost journalpost = createJournalpost(JOURNALPOST_ID1);
		journalpost.setJournalDato(null);
		no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost mapped
				= mapper.map(createInnsynJournalpost(journalpost, InnsynJournalpostTo.DokumentInnsyn.JA, InnsynJournalpostTo.AvsenderMottaker.JA));
		assertThat(mapped.getFerdigstilt(), is(DateConverterUtil.convertDateToXMLGregorianCalendar(DOKUMENT_FERDIG_DATO)));
	}

	@Test
	public void shouldMapJournalpostNotAvsenderMottaker() {
		Journalpost journalpost = createJournalpost(JOURNALPOST_ID1);
		no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost mapped
				= mapper.map(createInnsynJournalpost(journalpost, InnsynJournalpostTo.DokumentInnsyn.JA, InnsynJournalpostTo.AvsenderMottaker.NEI));
		assertThat(mapped.getBrukerErAvsenderMottaker(), is(AvsenderMottaker.NEI));
	}

	@Test
	public void shouldMapAvsenderMottakerCannotBeDecided() {
		Journalpost journalpost = createJournalpost(JOURNALPOST_ID1);
		no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost mapped
				= mapper.map(createInnsynJournalpost(journalpost, InnsynJournalpostTo.DokumentInnsyn.JA, InnsynJournalpostTo.AvsenderMottaker.KAN_IKKE_AVGJOERES));
		assertThat(mapped.getBrukerErAvsenderMottaker(), is(AvsenderMottaker.KAN_IKKE_AVGJØRES));
	}

	private void assertDokInfoRels(List<DokumentinfoRelasjon> dokumentinfoRelasjonListe, int size) {
		assertThat(dokumentinfoRelasjonListe, hasSize(size));

		DokumentinfoRelasjon hovedDok = dokumentinfoRelasjonListe.get(0);
		assertThat(hovedDok.getDokumentTilknyttetJournalpost()
				.getValue(), is(TilknyttetJournalpostSomCode.HOVEDDOKUMENT.name()));
		assertThat(hovedDok.getDokumentinfoRelasjonId(), is(Long.toString(DOK_INFO_REL_ID)));
		assertDokument(hovedDok.getJournalfoertDokument());

		DokumentinfoRelasjon vedlegg = dokumentinfoRelasjonListe.get(1);
		assertThat(vedlegg.getDokumentTilknyttetJournalpost().getValue(), is(TilknyttetJournalpostSomCode.VEDLEGG.name()));
		assertThat(vedlegg.getDokumentinfoRelasjonId(), is(Long.toString(DOK_INFO_REL_ID + 1)));
		assertDokument(vedlegg.getJournalfoertDokument());
	}

	private void assertDokument(JournalfoertDokumentInfo journalfoertDokument) {
		assertDokumentInnhold(journalfoertDokument.getBeskriverInnhold());
		assertThat(journalfoertDokument.getTittel(), is(DOKUMENT_TITTEL));
		assertSkannetInnholds(journalfoertDokument.getSkannetInnholdListe(), 2);
		assertThat(Long.valueOf(journalfoertDokument.getDokumentId()), is(DOKUMENT_INFO_ID));
		assertThat(journalfoertDokument.getInnsynDokument().value(), is(InnsynDokument.JA.value()));
	}

	private void assertSkannetInnholds(List<SkannetInnhold> skannetInnholdListe, int size) {
		assertThat(skannetInnholdListe, hasSize(size));
		for (SkannetInnhold skannetInnhold : skannetInnholdListe) {
			assertThat(Long.valueOf(skannetInnhold.getSkannetInnholdId()), is(SKANNET_INNHOLD_ID));
			assertThat(skannetInnhold.getVedleggInnhold(), is(VEDLEGG_INNHOLD));
		}
	}

	private void assertDokumentInnhold(DokumentInnhold beskriverInnhold) {
		assertThat(beskriverInnhold.getFiltype().getValue(), is(FIL_TYPE.name()));
		assertThat(beskriverInnhold.getVariantformat().getValue(), is(VARIANT_FORMAT.name()));
	}

	private void assertSak(Sak gjelderSak) {
		assertThat(gjelderSak.getSakId(), is(SAKS_ID));
		assertThat(gjelderSak.getFagsystem().getValue(), is(FAGSYSTEM.name()));
	}

	private List<InnsynJournalpostTo> createInnsynJournalpostToList(List<Journalpost> journalposts) {
		List<InnsynJournalpostTo> innsynJournalposts = new ArrayList<>();
		for (Journalpost journalpost : journalposts) {
			innsynJournalposts.add(createInnsynJournalpost(journalpost, InnsynJournalpostTo.DokumentInnsyn.JA, InnsynJournalpostTo.AvsenderMottaker.JA));
		}
		return innsynJournalposts;
	}

	private InnsynJournalpostTo createInnsynJournalpost(Journalpost journalpost, InnsynJournalpostTo.DokumentInnsyn innsynDokument, InnsynJournalpostTo.AvsenderMottaker avsenderMottaker) {
		InnsynJournalpostTo innsynJournalpostTo = new InnsynJournalpostTo(journalpost);
		innsynJournalpostTo.setAvsenderMottaker(avsenderMottaker);
		innsynJournalpostTo.putDokumentInnsyn(innsynDokument, DOKUMENT_INFO_ID);
		return innsynJournalpostTo;
	}


	private JournalpostDokumentInfoRelasjon createDokInfoRelKasert(TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode, Date dokumentFerdig, Long dokInfoRelId) {
		return getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(tilknyttetJournalpostSomCode)
				.journalpostDokumentInfoRelasjonId(dokInfoRelId)
				.dokumentInfo(getDokumentInfoBuilder()
						.dokumentstatus(DOKUMENT_STATUS)
						.filDetaljerList(FilDetaljer.builder()
										.variantFormat(VARIANT_FORMAT)
										.filtype(FIL_TYPE)
										.skjermingType(SkjermingTypeCode.POL)
										.build(),
								FilDetaljer.builder()
										.variantFormat(VARIANT_FORMAT)
										.filtype(FIL_TYPE)
										.skjermingType(SkjermingTypeCode.POL)
										.build())
						.tittel(DOKUMENT_TITTEL)
						.dokumentFerdigDato(dokumentFerdig)
						.skannetInnhold(SkannetInnholdBuilder.getSkannetInnholdBuilder()
										.skannetInnholdId(SKANNET_INNHOLD_ID)
										.vedleggInnhold(VEDLEGG_INNHOLD)
										.build(),
								SkannetInnholdBuilder.getSkannetInnholdBuilder()
										.skannetInnholdId(SKANNET_INNHOLD_ID)
										.vedleggInnhold(VEDLEGG_INNHOLD)
										.build())
						.dokumentInfoId(DOKUMENT_INFO_ID)
						.build())
				.build();
	}

	private Journalpost createJournalpostKassert(Long journalpostId) {
		return getJournalpostBuilder()
				.changeStamp(new ChangeStamp(null, CHANGE_STAMP_DATE, null, null))
				.fagomrade(FAGOMRADE)
				.journalDato(JOURNAL_DATO)
				.mottattDato(MOTTATT_DATO)
				.avsenderMottaker(AVSENDER_MOTTAKER)
				.journalpostId(journalpostId)
				.journalpostType(JOURNALPOST_TYPE)
				.kanalReferanseId(KANAL_REFERANSE_ID)
				.saksrelasjon(SaksrelasjonBuilder.getSaksrelasjonBuilder()
						.sakId(SAKS_ID)
						.fagsystem(FAGSYSTEM)
						.build())
				.dokumentInfoRelasjoner(
						createDokInfoRelKasert(TilknyttetJournalpostSomCode.HOVEDDOKUMENT, DOKUMENT_FERDIG_DATO, DOK_INFO_REL_ID),
						createDokInfoRelKasert(TilknyttetJournalpostSomCode.VEDLEGG, DOKUMENT_FERDIG_DATO_OLD, DOK_INFO_REL_ID + 1))
				.build();
	}

	private Journalpost createJournalpost(Long journalpostId) {
		return getJournalpostBuilder()
				.changeStamp(new ChangeStamp(null, CHANGE_STAMP_DATE, null, null))
				.fagomrade(FAGOMRADE)
				.journalDato(JOURNAL_DATO)
				.mottattDato(MOTTATT_DATO)
				.avsenderMottaker(AVSENDER_MOTTAKER)
				.journalpostId(journalpostId)
				.journalpostType(JOURNALPOST_TYPE)
				.kanalReferanseId(KANAL_REFERANSE_ID)
				.saksrelasjon(SaksrelasjonBuilder.getSaksrelasjonBuilder()
						.sakId(SAKS_ID)
						.fagsystem(FAGSYSTEM)
						.build())
				.dokumentInfoRelasjoner(
						createDokInfoRel(TilknyttetJournalpostSomCode.HOVEDDOKUMENT, DOKUMENT_FERDIG_DATO, DOK_INFO_REL_ID),
						createDokInfoRel(TilknyttetJournalpostSomCode.VEDLEGG, DOKUMENT_FERDIG_DATO_OLD, DOK_INFO_REL_ID + 1))
				.build();
	}

	private JournalpostDokumentInfoRelasjon createDokInfoRel(TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode, Date dokumentFerdig, Long dokInfoRelId) {
		return getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(tilknyttetJournalpostSomCode)
				.journalpostDokumentInfoRelasjonId(dokInfoRelId)
				.dokumentInfo(createDokumentInfo(dokumentFerdig))
				.build();
	}

	private DokumentInfo createDokumentInfo(Date dokumentFerdigDato) {
		return getDokumentInfoBuilder()
				.dokumentstatus(DOKUMENT_STATUS)
				.filDetaljerList(createFilDetaljer(), createFilDetaljer())
				.tittel(DOKUMENT_TITTEL)
				.dokumentFerdigDato(dokumentFerdigDato)
				.skannetInnhold(SkannetInnholdBuilder.getSkannetInnholdBuilder()
								.skannetInnholdId(SKANNET_INNHOLD_ID)
								.vedleggInnhold(VEDLEGG_INNHOLD)
								.build(),
						SkannetInnholdBuilder.getSkannetInnholdBuilder()
								.skannetInnholdId(SKANNET_INNHOLD_ID)
								.vedleggInnhold(VEDLEGG_INNHOLD)
								.build())
				.dokumentInfoId(DOKUMENT_INFO_ID)
				.build();
	}


	private DokumentInfo createSimpleDokumentInfo() {
		return getDokumentInfoBuilder()
				.dokumentInfoId(DOKUMENT_INFO_ID)
				.build();
	}

	private FilDetaljer createFilDetaljer() {
		return FilDetaljerBuilder.getFilDetaljerBuilder()
				.variantFormat(VARIANT_FORMAT)
				.filtype(FIL_TYPE)
				.build();
	}
}