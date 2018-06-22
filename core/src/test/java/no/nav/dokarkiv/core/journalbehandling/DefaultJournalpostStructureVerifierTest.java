package no.nav.dokarkiv.core.journalbehandling;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for DefaultJournalpostStructureVerifier. Validation failures is tested
 * in the domain object tests, so we only test happy cases here.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class DefaultJournalpostStructureVerifierTest {

	private DefaultJournalpostStructureVerifier verifier;

	private Journalpost journalpost;

	@Before
	public void setUp() {
		verifier = new DefaultJournalpostStructureVerifier();
	}

	@Test
	public void shouldVerifyCorrectJournalpost() throws Exception {
		journalpost =
				getJournalpostBuilder()
						.journalStatus(JournalStatusCode.M)
						.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
								.dokumentInfo(getDokumentInfoBuilder()
										.filDetaljerList(getFilDetaljerBuilder()
												.variantFormat(VariantFormatCode.SKANNING_META)
												.build())
										.build())
								.build())
						.build();

		assertSuccessfulStructureVerification(journalpost);
	}

	@Test
	public void shouldVerifyCorrectJournalpostForEndeligJournalforing() throws Exception {
		journalpost =
				getJournalpostBuilder()
						.journalStatus(JournalStatusCode.FL)
						.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(getDokumentInfoBuilder()
										.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
										.filDetaljerList(getFilDetaljerBuilder()
														.variantFormat(VariantFormatCode.ARKIV)
														.build(),
												getFilDetaljerBuilder()
														.variantFormat(
																VariantFormatCode.BREVBESTILLING)
														.build())
										.build())
								.build())
						.build();

		assertSuccessfulStructureVerification(journalpost);
	}

	private void assertSuccessfulStructureVerification(Journalpost journalpost) {
		verifier.verifyJournalpostStructure(journalpost);
	}

}