package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.DOKUMENTINFO_ID;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createDokumentInfo;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createJournalpost;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createRequest;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for {@link AngreLogiskSlettDokumentValidator}
 */
@RunWith(MockitoJUnitRunner.class)
public class AngreLogiskSlettDokumentValidatorTest {

	@InjectMocks
	private AngreLogiskSlettDokumentValidator validator;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void validerAngreLogiskSlettAvEttDokument_medUtilgjengeliggjortHoveddokument_skalValidereOK() {
		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);
		Journalpost journalpost = createJournalpost(DOKUMENTINFO_ID);
		Begrensning begrensning = Begrensning.builder()
				.journalpostId(journalpost.getJournalpostId())
				.begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
				.build();
		begrensning.setOpprettetKildeNavn("Opprettet kilde");

		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList =
				new ArrayList<JournalpostDokumentInfoRelasjon>(journalpost.getJournalpostDokumentInfoRelasjoner());

		validator.validerAngreLogiskSlettAvEttDokument(journalpostDokumentInfoRelasjonList, requestTo);
	}

	@Test
	public void validerAngreLogiskSlettAvEttDokument_medUtilgjengeliggjortVedlegg_skalValidereOK() {
		Journalpost journalpost = createJournalpost(DOKUMENTINFO_ID);
		journalpost.addJournalpostDokumentInfoRelasjon(getJournalpostDokumentInfoRelasjonBuilder()
				.journalpostDokumentInfoRelasjonId(13L)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(createDokumentInfo(DOKUMENTINFO_ID + 1))
				.build());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, vedlegg.getDokumentInfoId());

		Begrensning begrensning = Begrensning.builder()
				.journalpostId(journalpost.getJournalpostId())
				.dokumentInfoId(vedlegg.getDokumentInfoId())
				.begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
				.build();
		begrensning.setOpprettetKildeNavn("Opprettet kilde");

		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList =
				new ArrayList<JournalpostDokumentInfoRelasjon>(journalpost.getJournalpostDokumentInfoRelasjoner());

		validator.validerAngreLogiskSlettAvEttDokument(journalpostDokumentInfoRelasjonList, requestTo);
	}
}
