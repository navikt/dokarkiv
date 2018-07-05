package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import no.nav.dokarkiv.arkiverdokumentmottak.ValidatorTestConfig;
import no.nav.dokarkiv.core.domain.builder.BrukerBuilder;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ValidatorTestConfig.class})
public class JournalforInngaaendeForsendelseV2ValidatorTest {
	private static final boolean SENSITIVITET = true;
	private static final String BREVKODE = "brevkode";
	private static final String DOKUMENT_TYPE_ID = "dokumentTypeId";
	private static final String JOURNALFOERENDE_ENHET_REF = "2009";
	private static final TilknyttetJournalpostSomCode HOVEDDOKUMENT = TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
	private static final TilknyttetJournalpostSomCode VEDLEGG = TilknyttetJournalpostSomCode.VEDLEGG;
	private static final String OPPRETTET_AV_NAVN = "Banjo Kazooie";
	private static final String TITTEL = "Once Upon a Time In Mehico";
	private static final DokumentKategoriCode KATEGORI = DokumentKategoriCode.B;
	private static final String INNHOLD = "Antonio Banderas";
	private static final VariantFormatCode VARIANTFORMAT_AKTIV = VariantFormatCode.ARKIV;
	private static final VariantFormatCode VARIANTFORMAT_ORIGINAL = VariantFormatCode.ORIGINAL;
	private static final String PERSONIDENT = "***gammelt_fnr***";
	private static final String EKSTERNPART_NAVN = "Mario & Luigi";
	private static final FagsystemCode FAGSYSTEMKODE = FagsystemCode.AO01;
	private static final String SAKSID = "312";
	private static final BrukerTypeCode BRUKER_TYPE_CODE = BrukerTypeCode.PERSON;
	private static final String TILLEGGSOPPLYSNING_KEY = "tilleggsopplysning-1";
	private static final String TILLEGGSOPPLYSNING_VALUE = "Tillegg 1";
	private static final Date DATO_DOKUMENT = new Date(1234567890);
	private static final Date DATO_MOTTATT = new Date(1234567891);
	private static final FagomradeCode FAGOMRADE = FagomradeCode.PEN;
	private static final MottaksKanalCode MOTTAKS_KANAL_CODE = MottaksKanalCode.ALTINN;
	private static final FilTypeCode FIL_TYPE_CODE = FilTypeCode.XML;
	private static final Map<String, String> TILLEGGSOPPLYSNINGER = new HashMap<>();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private Journalpost journalpost;

	@Inject
	private JournalforInngaaendeForsendelseV2Validator validator;

	@Before
	public void setUp() throws Exception {
		TILLEGGSOPPLYSNINGER.put(TILLEGGSOPPLYSNING_KEY, TILLEGGSOPPLYSNING_VALUE);
		journalpost = createJournalpost();
	}

	@Test
	public void testValidRequest() throws Exception {
		validator.validate(journalpost);
	}

	@Test
	public void testNullVariantFormat() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("FilDetaljer.variantFormat must be set");

		journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getFildetaljerListe()
				.iterator()
				.next()
				.setVariantFormat(null);

		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullFagomraade() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Journalpost.fagomrade");

		journalpost.setFagomrade(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullJournalforendeEnhetId() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Journalpost.journalForendeEnhetId");

		journalpost.setJournalForendeEnhetId(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullOpprettetAvNavn() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Journalpost.opprettetAvNavn");

		journalpost.setOpprettetAvNavn(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullInnhold() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Journalpost.innhold");

		journalpost.setInnhold(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullDokumentDato() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Missing required field in request: Journalpost.DokumentDato");

		journalpost.setDokumentDato(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullAvsenderMottaker() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Journalpost.avsenderMottaker");

		journalpost.setAvsenderMottaker(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullMottattDato() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Missing required field in request: Journalpost.MottatDato");

		journalpost.setMottattDato(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullMottakskanal() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Missing required field in request: Journalpost.Mottakskanal");

		journalpost.setMottakskanal(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullSakId() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Saksrelasjon.sakId");

		journalpost.getSaksrelasjon().setSakId(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullFagsystem() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Saksrelasjon.fagsystem");

		journalpost.getSaksrelasjon().setFagsystem(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullBrukerId() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Bruker.brukerId");

		for (Bruker bruker : journalpost.getBrukere()) {
			bruker.setBrukerId(null);
		}
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullBrukerType() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Bruker.brukerType");

		for (Bruker bruker : journalpost.getBrukere()) {
			bruker.setBrukerType(null);
		}
		validator.validate(journalpost);
	}


	@Test
	public void shouldFailOnNullTilknyttetJournalpostSomCode() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjon.tilknyttetJournalpostSom");

		for (JournalpostDokumentInfoRelasjon jdir : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			jdir.setTilknyttetJournalpostSom(null);
		}
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullKategori() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("DokumentInfo.kategori");

		for (JournalpostDokumentInfoRelasjon jdir : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			jdir.getDokumentInfo().setKategori(null);
		}
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullTittel() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("DokumentInfo.tittel");

		for (JournalpostDokumentInfoRelasjon jdir : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			jdir.getDokumentInfo().setTittel(null);
		}
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullDokumenttypeId() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Missing required field in request: DokumentInfo.DokumenttypeId");

		for (JournalpostDokumentInfoRelasjon jdir : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			jdir.getDokumentInfo().setDokumenttypeId(null);
		}
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnEmptyDokumenttypeId() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Missing required field in request: DokumentInfo.DokumenttypeId");

		for (JournalpostDokumentInfoRelasjon jdir : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			jdir.getDokumentInfo().setDokumenttypeId("");
		}
		validator.validate(journalpost);
	}

	@Test
	public void shouldNotFailOnNullSensitivt() throws Exception {
		for (JournalpostDokumentInfoRelasjon jdir : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			jdir.getDokumentInfo().setSensitivt(null);
		}
		validator.validate(journalpost);
	}

	@Test
	public void shouldFailOnNullFiltype() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("FilDetaljer.filtype");

		for (JournalpostDokumentInfoRelasjon jdir : journalpost
				.getJournalpostDokumentInfoRelasjoner()) {
			for (FilDetaljer filDetaljer : jdir.getDokumentInfo().getFildetaljerListe()) {
				filDetaljer.setFiltype(null);
			}
		}
		validator.validate(journalpost);
	}

	@Test
	public void testValidVariantformater() throws Exception {
		validator.validateVariantFormaterAndHoveddokument(journalpost);
	}

	@Test
	public void shouldFailOnNoVariantFormatArkiv() throws Exception {
		expectedException.expect(InvalidJournalpostStructureException.class);
		expectedException.expectMessage("All the Journalpost's DokumentInfos must contain an arkiv variant");

		for (JournalpostDokumentInfoRelasjon jdir : journalpost
				.getJournalpostDokumentInfoRelasjoner()) {
			for (FilDetaljer filDetaljer : jdir.getDokumentInfo().getFildetaljerListe()) {
				filDetaljer.setVariantFormat(VariantFormatCode.BREVBESTILLING);
			}
			validator.validateVariantFormaterAndHoveddokument(journalpost);
		}
	}

	@Test
	public void shouldFailOnMultipleFilVarianterWithSameFormat() throws Exception {
		expectedException.expect(InvalidJournalpostStructureException.class);
		expectedException.expectMessage("DokumentInfo cannot contain dokumentvariant duplicates, found 2 ARKIV varianter");

		for (JournalpostDokumentInfoRelasjon jdir : journalpost
				.getJournalpostDokumentInfoRelasjoner()) {
			for (FilDetaljer filDetaljer : jdir.getDokumentInfo().getFildetaljerListe()) {
				filDetaljer.setVariantFormat(VariantFormatCode.ARKIV);
			}
			validator.validateVariantFormaterAndHoveddokument(journalpost);
		}
	}

	@Test
	public void testValidHoveddokument() throws Exception {
		validator.validateVariantFormaterAndHoveddokument(journalpost);
	}

	@Test
	public void testTooFewHoveddokument() throws Exception {
		expectedException.expect(InvalidJournalpostStructureException.class);
		expectedException.expectMessage("Journalpost must contain a hoveddokument");

		for (JournalpostDokumentInfoRelasjon jdir : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			jdir.setTilknyttetJournalpostSom(VEDLEGG);
		}
		validator.validateVariantFormaterAndHoveddokument(journalpost);
	}

	@Test
	public void testTooManyHoveddokument() throws Exception {
		expectedException.expect(InvalidJournalpostStructureException.class);
		expectedException.expectMessage("Journalpost cannot contain more than one hoveddokument");

		for (JournalpostDokumentInfoRelasjon jp : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			jp.setTilknyttetJournalpostSom(HOVEDDOKUMENT);
		}
		validator.validateVariantFormaterAndHoveddokument(journalpost);
	}


	private Journalpost createJournalpost() {
		return JournalpostBuilder.getJournalpostBuilder()
				.fagomrade(FAGOMRADE)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.journalForendeEnhetId(JOURNALFOERENDE_ENHET_REF)
				.innhold(INNHOLD)
				.journalStatus(JournalStatusCode.J)
				.journalpostType(JournalpostTypeCode.I)
				.avsenderMottaker(EKSTERNPART_NAVN)
				.avsenderMottakerId(PERSONIDENT)
				.mottattDato(DATO_MOTTATT)
				.mottakskanal(MOTTAKS_KANAL_CODE)
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
								.dokumentInfo(
										DokumentInfoBuilder.getDokumentInfoBuilder()
												.kategori(KATEGORI)
												.tittel(TITTEL)
												.brevkode(BREVKODE)
												.dokumenttypeId(DOKUMENT_TYPE_ID)
												.sensitivt(SENSITIVITET)
												.filDetaljerList(
														FilDetaljerBuilder.getFilDetaljerBuilder()
																.filtype(FIL_TYPE_CODE)
																.variantFormat(VARIANTFORMAT_AKTIV)
																.fileContent(new byte[]{1, 2, 4, 8, 16, 32, 64})
																.build(),
														FilDetaljerBuilder.getFilDetaljerBuilder()
																.filtype(FIL_TYPE_CODE)
																.variantFormat(VARIANTFORMAT_ORIGINAL)
																.fileContent(new byte[]{64, 32, 16, 8, 4, 2, 1})
																.build()
												)
												.tilleggsopplysninger(TILLEGGSOPPLYSNINGER)
												.build()
								)
								.tilknyttetJournalpostSom(HOVEDDOKUMENT)
								.tilknyttetAvNavn("tilknyttetAvNavn")
								.build(),
						JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
								.dokumentInfo(
										DokumentInfoBuilder.getDokumentInfoBuilder()
												.kategori(KATEGORI)
												.tittel(TITTEL)
												.brevkode(BREVKODE)
												.dokumenttypeId(DOKUMENT_TYPE_ID)
												.sensitivt(SENSITIVITET)
												.filDetaljerList(
														FilDetaljerBuilder.getFilDetaljerBuilder()
																.filtype(FIL_TYPE_CODE)
																.variantFormat(VARIANTFORMAT_AKTIV)
																.fileContent(new byte[]{1, 2, 4, 8, 16, 32, 64})
																.build(),
														FilDetaljerBuilder.getFilDetaljerBuilder()
																.filtype(FIL_TYPE_CODE)
																.variantFormat(VARIANTFORMAT_ORIGINAL)
																.fileContent(new byte[]{64, 32, 16, 8, 4, 2, 1})
																.build()
												)
												.tilleggsopplysninger(TILLEGGSOPPLYSNINGER)
												.build()
								)
								.tilknyttetJournalpostSom(VEDLEGG)
								.tilknyttetAvNavn("tilknyttetAvNavn")
								.build()
				)
				.saksrelasjon(
						SaksrelasjonBuilder.getSaksrelasjonBuilder()
								.fagsystem(FAGSYSTEMKODE)
								.sakId(SAKSID)
								.build()
				)
				.brukere(
						BrukerBuilder.getBrukerBuilder()
								.brukerId(PERSONIDENT)
								.brukerType(BRUKER_TYPE_CODE)
								.build()
				)
				.dokumentDato(DATO_DOKUMENT)
				.build();
	}

}