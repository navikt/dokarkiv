package no.nav.dokarkiv.core.domain.entities;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.AVBRUTT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for Journalpost.
 *
 * @author Per Kristian Foss, Visma Sirius
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@ExtendWith(MockitoExtension.class)
public class JournalpostTest {

	@InjectMocks
	private Journalpost journalpost;

	@Test
	public void shouldReturnJournalpostRelasjonerWhenNotSkjermet() {

		Journalpost journalpost = getJournalpostBuilder().dokumentInfoRelasjoner(
				JournalpostDokumentInfoRelasjon.builder()
						.tilknyttetJournalpostSom(HOVEDDOKUMENT)
						.build(),
				JournalpostDokumentInfoRelasjon.builder()
						.tilknyttetJournalpostSom(VEDLEGG)
						.build()
		).build();

		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().size(), is(2));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjonerAdmin().size(), is(2));
	}

	@Test
	public void shouldUpdateJournalpostRelasjoner() {

		Journalpost journalpost = getJournalpostBuilder().dokumentInfoRelasjoner(
				JournalpostDokumentInfoRelasjon.builder()
						.tilknyttetJournalpostSom(HOVEDDOKUMENT)
						.dokumentInfo(new DokumentInfo())
						.build(),
				JournalpostDokumentInfoRelasjon.builder()
						.tilknyttetJournalpostSom(VEDLEGG)
						.build()
		).build();

		journalpost.findHoveddokumentDokumentInfoRelasjon().setTilknyttetAvNavn("testnavn");
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setDokumentstatus(AVBRUTT);

		assertThat(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentstatus(), is(AVBRUTT));
		assertThat(journalpost.findHoveddokumentDokumentInfoRelasjon().getTilknyttetAvNavn(), is("testnavn"));

		journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(HOVEDDOKUMENT).iterator().next().setTilknyttetAvNavn("testnavn2");
		assertThat(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(HOVEDDOKUMENT).iterator().next().getTilknyttetAvNavn(), is("testnavn2"));


	}

	@Test
	public void shouldNotReturnSkjermetJournalpostRelasjoner() {

		Journalpost journalpost = getJournalpostBuilder().dokumentInfoRelasjoner(
				JournalpostDokumentInfoRelasjon.builder()
						.tilknyttetJournalpostSom(HOVEDDOKUMENT)
						.skjermingType(SkjermingTypeCode.POL)
						.build(),
				JournalpostDokumentInfoRelasjon.builder()
						.tilknyttetJournalpostSom(VEDLEGG)
						.skjermingType(SkjermingTypeCode.POL)
						.build()
		).build();

		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().size(), is(0));
		assertThat(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG).size(), is(0));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjonerAdmin().size(), is(2));
	}

	@Test
	public void shouldOnlyReturnNotSkjermetJournalpostRelasjoner() {

		Journalpost journalpost = getJournalpostBuilder().dokumentInfoRelasjoner(
				JournalpostDokumentInfoRelasjon.builder()
						.tilknyttetJournalpostSom(HOVEDDOKUMENT)
						.build(),
				JournalpostDokumentInfoRelasjon.builder()
						.tilknyttetJournalpostSom(VEDLEGG)
						.skjermingType(SkjermingTypeCode.POL)
						.build()
		).build();

		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().size(), is(1));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getTilknyttetJournalpostSom(), is(HOVEDDOKUMENT));
		assertThat(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(HOVEDDOKUMENT).size(), is(1));
		assertThat(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG).size(), is(0));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjonerAdmin().size(), is(2));
	}

	@Test
	public void shouldNotReturnSkjermetFildetaljer() {

		Journalpost journalpost = getJournalpostBuilder().dokumentInfoRelasjoner(
				JournalpostDokumentInfoRelasjon.builder()
						.tilknyttetJournalpostSom(HOVEDDOKUMENT)
						.dokumentInfo(DokumentInfo.builder()
								.fildetaljerListe(new HashSet<>(Arrays.asList(
										FilDetaljer.builder()
												.variantFormat(VariantFormatCode.ARKIV)
												.filUuid("uuid1")
												.skjermingType(SkjermingTypeCode.POL)
												.build(),
										FilDetaljer.builder()
												.variantFormat(SLADDET)
												.filUuid("uuid2")
												.build()
								)))
								.build())
						.build(),
				JournalpostDokumentInfoRelasjon.builder()
						.tilknyttetJournalpostSom(VEDLEGG)
						.skjermingType(SkjermingTypeCode.POL)
						.build()
		).build();

		assertThat(journalpost.findFilDetaljerByFilUuid("uuid2").getVariantFormat(), is(SLADDET));
		assertThat(journalpost.findFilDetaljerByFilUuid("uuid1").getVariantFormat(), is(SLADDET));
	}

	@Test
	public void testRemoveAllUsers() {
		journalpost.addBruker(new Bruker());
		journalpost.addBruker(new Bruker());

		journalpost.removeBrukere(journalpost.getBrukere());
		assertThat(journalpost.getBrukere().size(), is(equalTo(0)));
	}

	@Test
	public void shouldSetSaksrelasjonRelationBothWays() {
		journalpost = new Journalpost();
		Saksrelasjon saksrelasjon = new Saksrelasjon();

		journalpost.setSaksrelasjon(saksrelasjon);
		assertThat(journalpost.getSaksrelasjon(), is(saksrelasjon));
		assertThat(saksrelasjon.getJournalpost(), is(journalpost));
	}

	@Test
	public void shouldSetDokumentInfoRelasjonBothWays() {
		journalpost = new Journalpost();
		JournalpostDokumentInfoRelasjon relasjon = new JournalpostDokumentInfoRelasjon();

		journalpost.addJournalpostDokumentInfoRelasjon(relasjon);
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().size(), is(1));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next(), is(relasjon));
		assertThat(relasjon.getJournalpost(), is(journalpost));
	}

	@Test
	public void shouldCheckForLenientStatus() {
		assertLenientStatus(JournalStatusCode.MO, true);
		assertLenientStatus(JournalStatusCode.M, true);
		assertLenientStatus(JournalStatusCode.U, true);
		assertLenientStatus(JournalStatusCode.UB, true);
		assertLenientStatus(JournalStatusCode.R, false);
		assertLenientStatus(JournalStatusCode.D, false);
		assertLenientStatus(JournalStatusCode.FS, false);
		assertLenientStatus(JournalStatusCode.FL, false);
	}

	private void assertLenientStatus(JournalStatusCode journalStatus, boolean expectedResult) {
		journalpost = getJournalpostBuilder().journalStatus(journalStatus).build();
		assertThat(journalpost.hasLenientStatus(), is(expectedResult));
	}

	@Test
	public void shoudlCheckIfInFerdigOgSentralPrintJournalforingStatus() {
		Journalpost journalpost = new Journalpost();
		journalpost.setJournalstatus(JournalStatusCode.FS);
		assertTrue(journalpost.hasFerdigOgSentralPrintJournalforingStatus());
	}

	@Test
	public void shoudlCheckIfNotInFerdigOgSentralPrintJournalforingStatus() {
		Journalpost journalpost = new Journalpost();
		journalpost.setJournalstatus(JournalStatusCode.FL);
		assertFalse(journalpost.hasFerdigOgSentralPrintJournalforingStatus());
	}

	@Test
	public void shouldCheckInngaendeStatus() {
		assertInngaendeStatus(JournalStatusCode.MO, true);
		assertInngaendeStatus(JournalStatusCode.M, true);
		assertInngaendeStatus(JournalStatusCode.U, true);
		assertInngaendeStatus(JournalStatusCode.UB, true);
		assertInngaendeStatus(JournalStatusCode.J, true);
		assertInngaendeStatus(JournalStatusCode.R, false);
		assertInngaendeStatus(JournalStatusCode.D, false);
		assertInngaendeStatus(JournalStatusCode.FS, false);
		assertInngaendeStatus(JournalStatusCode.FL, false);
	}

	private void assertInngaendeStatus(JournalStatusCode journalStatus, boolean expectedResult) {
		journalpost = getJournalpostBuilder().journalStatus(journalStatus).build();
		assertThat(journalpost.hasInngaendeStatus(), is(expectedResult));
	}

	@Test
	public void shouldThrowExceptionForNonUniqueDokumentInfoRelasjoner() {
		long dokumentInfoId = 200;
		journalpost = getJournalpostBuilder().dokumentInfoRelasjoner(
				getJournalpostDokumentInfoRelasjonBuilder().dokumentInfo(
						getDokumentInfoBuilder().dokumentInfoId(dokumentInfoId).build()).build(),
				getJournalpostDokumentInfoRelasjonBuilder().dokumentInfo(
						getDokumentInfoBuilder().dokumentInfoId(dokumentInfoId).build()).build()).build();
		try {
			journalpost.verifyUniqueDokumentInfoRelasjoner();
		} catch (InvalidJournalpostStructureException e) {
			assertThat(e.getMessage(), containsString("2"));
			assertThat(e.getMessage(), containsString(String.valueOf(dokumentInfoId)));
		}
	}

	@Test
	public void shouldThrowExceptionForDuplicateDokumentVarianter() {
		VariantFormatCode arkiv = VariantFormatCode.ARKIV;
		journalpost = getJournalpostBuilder().dokumentInfoRelasjoner(
				getJournalpostDokumentInfoRelasjonBuilder().dokumentInfo(
								getDokumentInfoBuilder().filDetaljerList(getFilDetaljerBuilder().variantFormat(arkiv).build()).build())
						.build(),
				getJournalpostDokumentInfoRelasjonBuilder().dokumentInfo(
						getDokumentInfoBuilder().filDetaljerList(getFilDetaljerBuilder().variantFormat(arkiv).build(),
								getFilDetaljerBuilder().variantFormat(arkiv).build()).build()).build()).build();
		try {
			journalpost.verifyNoDokumentVariantDuplicates();
			fail();
		} catch (InvalidJournalpostStructureException e) {
			assertThat(e.getMessage(), containsString(arkiv.name()));
		}
	}

	@Test
	public void shouldNotThrowNPEWhenCoutingIfVariantFormatIsNull() {
		VariantFormatCode arkiv = VariantFormatCode.ARKIV;
		journalpost = getJournalpostBuilder().dokumentInfoRelasjoner(
				getJournalpostDokumentInfoRelasjonBuilder().dokumentInfo(
						getDokumentInfoBuilder().filDetaljerList(getFilDetaljerBuilder().variantFormat(arkiv).build(),
								getFilDetaljerBuilder().variantFormat(null).build()).build()).build()).build();
		journalpost.verifyNoDokumentVariantDuplicates();
	}

	@Test
	public void shouldVerifyStructureForEndeligJournalforingWhenStatusNotFinal() {
		Journalpost journalpost = getJournalpostBuilder().journalStatus(JournalStatusCode.M).build();

		journalpost.verifyStructureForEndeligJournalforing();
	}

	@Test
	public void shouldThrowExceptionForNoHoveddok() {
		Journalpost journalpost = getJournalpostBuilder().journalStatus(JournalStatusCode.J).build();

		assertThrows(InvalidJournalpostStructureException.class,
				journalpost::verifyOnlyOneHoveddokument,
				"Journalpost must contain a hoveddokument");
	}

	@Test
	public void shouldThrowExceptionForTooManyHoveddoks() {
		Journalpost journalpost = createJournalpostWithTwoDokumentInfoRelasjoner(JournalStatusCode.FS, HOVEDDOKUMENT,
				HOVEDDOKUMENT);

		assertThrows(InvalidJournalpostStructureException.class,
				journalpost::verifyOnlyOneHoveddokument,
				"Journalpost cannot contain more than one hoveddokument");
	}

	@Test
	public void shouldThrowExceptionWhenMoreThanOneHoveddokIsSet() {
		Journalpost journalpost = createJournalpostWithTwoDokumentInfoRelasjoner(JournalStatusCode.FL, HOVEDDOKUMENT,
				HOVEDDOKUMENT);

		assertExceptionThrownWithMessage(journalpost, "more than one hoveddokument");
	}

	@Test
	public void shouldThrowExceptionForMissingJournalForendeEnhetIdAndStatusM() {
		Journalpost journalpost = getJournalpostBuilder()
				.journalpostId(100L)
				.journalStatus(JournalStatusCode.M)
				.fagomrade(FagomradeCode.BAR)
				.journalpostType(JournalpostTypeCode.I)
				.innhold("Innhold")
				.endretAvNavn("Navn")
				.build();
		try {
			journalpost.verifyJournalforendeEnhetIdForMidlertidigJournalforing();
			fail();
		} catch (InvalidArgumentException e) {
			assertThat(e.getMessage(), containsString("Journalpost.journalForendeEnhetId must be set"));
		}
	}

	@Test
	public void shouldThrowExceptionForMissingArkivVariant() {
		journalpost = getJournalpostBuilder()
				.journalStatus(JournalStatusCode.J)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(HOVEDDOKUMENT)
								.dokumentInfo(
										getDokumentInfoBuilder().filDetaljerList(
												getFilDetaljerBuilder().variantFormat(VariantFormatCode.ARKIV).build()).build())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(VEDLEGG)
								.dokumentInfo(
										getDokumentInfoBuilder().filDetaljerList(
														getFilDetaljerBuilder().variantFormat(VariantFormatCode.PRODUKSJON).build())
												.build()).build()).build();

		assertExceptionThrownWithMessage(journalpost, "arkiv variant");
	}

	@Test
	public void shouldThrowExceptionForDokumentUnderRedigering() {
		journalpost = getJournalpostBuilder()
				.journalStatus(JournalStatusCode.FS)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(HOVEDDOKUMENT)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING)
												.filDetaljerList(
														getFilDetaljerBuilder().variantFormat(VariantFormatCode.ARKIV).build())
												.build()).build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(VEDLEGG)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
												.filDetaljerList(
														getFilDetaljerBuilder().variantFormat(VariantFormatCode.ARKIV).build())
												.build()).build()).build();

		assertExceptionThrownWithMessage(journalpost, "under redigering");
	}

	@Test
	public void shouldClearDokumentInfoRelasjoner() {
		Journalpost journalpost = new Journalpost();
		journalpost.addJournalpostDokumentInfoRelasjon(new JournalpostDokumentInfoRelasjon());

		journalpost.clearJournalpostDokumentInfoRelasjoner();

		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().isEmpty(), is(true));
	}

	@Test
	public void shouldClearBrukere() {
		Journalpost journalpost = new Journalpost();
		journalpost.addBruker(new Bruker());

		journalpost.clearBrukere();

		assertThat(journalpost.getBrukere().isEmpty(), is(true));
	}

	private Journalpost createJournalpostWithTwoDokumentInfoRelasjoner(JournalStatusCode journalstatus,
																	   TilknyttetJournalpostSomCode tilknyttet1, TilknyttetJournalpostSomCode tilknyttet2) {
		return getJournalpostBuilder()
				.journalStatus(journalstatus)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder().tilknyttetJournalpostSom(tilknyttet1).build(),
						getJournalpostDokumentInfoRelasjonBuilder().tilknyttetJournalpostSom(tilknyttet2).build()).build();
	}

	private void assertExceptionThrownWithMessage(Journalpost journalpost, String... messages) {
		try {
			journalpost.verifyStructureForEndeligJournalforing();
			fail();
		} catch (InvalidJournalpostStructureException e) {
			for (String message : messages) {
				assertThat(e.getMessage(), containsString(message));
			}
		}
	}

}
