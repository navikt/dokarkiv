package no.nav.dokarkiv.innsynjournal.v2.tjoark059;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertThat;

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
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.innsynjournal.v2.InnsynJournalpostTo;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostResponse;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;

/**
 * Unit tests for {@link IdentifiserJournalpostV2ResponseMapper}
 *
 * @author Ketill Fenne, Visma Consulting.
 */
public class IdentifiserJournalpostResponseMapperTest {

	private static final String AVSENDER_MOTTAKER = "avsendermottaker";
	private static final FagomradeCode FAGOMRADE = FagomradeCode.PEN;
	private static final String SAKS_ID = "id";
	private static final FagsystemCode FAGSYSTEM = FagsystemCode.BID;
	private static final Long DOK_INFO_REL_ID = 3L;
	private static final String DOKUMENT_TITTEL = "tittel";
	private static final VariantFormatCode VARIANT_FORMAT = VariantFormatCode.ARKIV;
	private static final FilTypeCode FIL_TYPE = FilTypeCode.DOC;
	private static final Long SKANNET_INNHOLD_ID = 2L;
	private static final String VEDLEGG_INNHOLD = "vedleggInnhold";
	private static final Long HOVEDDOKUMENT_INFO_ID = 10L;
	private static final Long VEDLEGGDOKUMENT_INFO_ID = 11L;
	private static final Long JOURNALPOST_ID = 0L;
	private static final JournalpostTypeCode JOURNALPOST_TYPE = JournalpostTypeCode.U;
	private static final Date JOURNAL_DATO = Date.from(LocalDate.of(2014, Month.JANUARY, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
	private static final Date MOTTATT_DATO = Date.from(LocalDate.of(2015, Month.JANUARY, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
	private static final DokumentStatusCode DOKUMENT_STATUS = DokumentStatusCode.FERDIGSTILT;
	private static final Date DOKUMENT_FERDIG_DATO = new Date(1L);
	private static final Date DOKUMENT_FERDIG_DATO_OLD = new Date(0L);
	private static final Date CHANGE_STAMP_DATE = new Date(0L);
	private static final String KANAL_REFERANSE  = "KanalRef1";

	private IdentifiserJournalpostV2ResponseMapper mapper;

	@Before
	public void setUp() throws Exception {
		mapper = new IdentifiserJournalpostV2ResponseMapper();
	}

	@Test
	public void shouldMap() {
		InnsynJournalpostTo innsynJournalpostTo = createInnsynJournalpost(createJournalpost());

		innsynJournalpostTo.putDokumentInnsyn(InnsynJournalpostTo.DokumentInnsyn.JA, HOVEDDOKUMENT_INFO_ID);
		innsynJournalpostTo.putDokumentInnsyn(InnsynJournalpostTo.DokumentInnsyn.NEI, VEDLEGGDOKUMENT_INFO_ID);

		IdentifiserJournalpostResponse response = mapper.map(innsynJournalpostTo);
		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID.toString()));
		assertThat(response.getHoveddokument().getDokumentId(), is(HOVEDDOKUMENT_INFO_ID.toString()));
		assertThat(response.getHoveddokument().getTittel(), is(DOKUMENT_TITTEL));
		assertThat(response.getHoveddokument().getInnsynDokument().name(), is(InnsynJournalpostTo.DokumentInnsyn.JA.name()));

		assertThat(response.getVedleggListe(), hasSize(2));
		for (no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Dokument vedlegg : response.getVedleggListe()) {
			assertThat(vedlegg.getDokumentId(), is(VEDLEGGDOKUMENT_INFO_ID.toString()));
			assertThat(vedlegg.getTittel(), is(DOKUMENT_TITTEL));
			assertThat(vedlegg.getInnsynDokument().name(), is(InnsynJournalpostTo.DokumentInnsyn.NEI.name()));
		}
	}


	@Test
	public void shouldHandleEmptyResponse() throws Exception {
		Journalpost journalpost = new Journalpost();

		IdentifiserJournalpostResponse mapped =
				mapper.map(createInnsynJournalpost(journalpost));

		assertThat(mapped.getJournalpostId(), isEmptyOrNullString());
	}


	private InnsynJournalpostTo createInnsynJournalpost(Journalpost journalpost) {
		InnsynJournalpostTo innsynJournalpostTo = new InnsynJournalpostTo(journalpost);
		return innsynJournalpostTo;
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.changeStamp(new ChangeStamp(null, CHANGE_STAMP_DATE, null, null))
				.fagomrade(FAGOMRADE)
				.journalDato(JOURNAL_DATO)
				.mottattDato(MOTTATT_DATO)
				.avsenderMottaker(AVSENDER_MOTTAKER)
				.journalpostId(JOURNALPOST_ID)
				.journalpostType(JOURNALPOST_TYPE)
				.kanalReferanseId(KANAL_REFERANSE)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.saksrelasjon(SaksrelasjonBuilder.getSaksrelasjonBuilder()
						.sakId(SAKS_ID)
						.fagsystem(FAGSYSTEM)
						.build())
				.dokumentInfoRelasjoner(
						createDokInfoRel(TilknyttetJournalpostSomCode.HOVEDDOKUMENT, DOKUMENT_FERDIG_DATO, DOK_INFO_REL_ID, HOVEDDOKUMENT_INFO_ID),
						createDokInfoRel(TilknyttetJournalpostSomCode.VEDLEGG, DOKUMENT_FERDIG_DATO_OLD, DOK_INFO_REL_ID + 1, VEDLEGGDOKUMENT_INFO_ID ),
						createDokInfoRel(TilknyttetJournalpostSomCode.VEDLEGG, DOKUMENT_FERDIG_DATO_OLD, DOK_INFO_REL_ID + 2, VEDLEGGDOKUMENT_INFO_ID ))
				.build();
	}

	private JournalpostDokumentInfoRelasjon createDokInfoRel(TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode, Date dokumentFerdig, Long dokInfoRelId, Long dokumentInfoId) {
		return getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(tilknyttetJournalpostSomCode)
				.journalpostDokumentInfoRelasjonId(dokInfoRelId)
				.dokumentInfo(createDokumentInfo(dokumentFerdig,dokumentInfoId))
				.build();
	}

	private DokumentInfo createDokumentInfo(Date dokumentFerdigDato, Long dokumentInfoId) {
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
				.dokumentInfoId(dokumentInfoId)
				.build();
	}

	private FilDetaljer createFilDetaljer() {
		return FilDetaljerBuilder.getFilDetaljerBuilder()
				.variantFormat(VARIANT_FORMAT)
				.filtype(FIL_TYPE)
				.build();
	}
}