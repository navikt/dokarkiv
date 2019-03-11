package no.nav.dokarkiv.journalpost.v1.rjoark202.util;

import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BREVKODE1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BREVKODE2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.INNHOLD;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.KANALREFERANSE_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.SAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TILLEGGSOPPLYSNING_NOKKEL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TILLEGGSOPPLYSNING_VERDI;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequestUtenDokumenter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import no.nav.dokarkiv.core.domain.codes.Behandlingstema;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.util.TestUtils;
import org.junit.Test;

public class JournalpostMapperTest {

	private JournalpostMapper mapper = new JournalpostMapper();

	@Test
	public void shouldMapInngaaendeJournalpost() {
		OpprettJournalpostRequest request = createRequest(JournalpostType.INNGAAENDE);
		Journalpost jp = mapper.map(request);

		assertEquals(JournalpostTypeCode.I, jp.getJournalposttype());
		assertEquals(JournalStatusCode.M, jp.getJournalstatus());
		assertEquals(AVSENDER_ID_PERSON, jp.getAvsenderMottakerId());
		assertEquals(TestUtils.AVSENDER_NAVN, jp.getAvsenderMottaker());
		assertEquals(BRUKER_ID_PERSON, jp.getBrukere().iterator().next().getBrukerId());
		assertEquals(BrukerTypeCode.PERSON, jp.getBrukere().iterator().next().getBrukerType());
		assertEquals(FagomradeCode.FOR, jp.getFagomrade());
		assertEquals(Behandlingstema.ab0001, jp.getBehandlingstema());
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

		JournalpostDokumentInfoRelasjon relasjon2 = jp.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).iterator().next();
		assertEquals(TilknyttetJournalpostSomCode.VEDLEGG, relasjon2.getTilknyttetJournalpostSom());
		DokumentInfo dokumentInfo2 = relasjon2.getDokumentInfo();
		assertEquals(BREVKODE2, dokumentInfo2.getBrevkode());
		assertEquals(DOKUMENT_TITTEL2, dokumentInfo2.getTittel());
		assertEquals(DokumentKategoriCode.SED, dokumentInfo2.getKategori());
	}

	@Test
	public void shouldMapUtgaaendeJournalpost() {
		OpprettJournalpostRequest request = createRequest(JournalpostType.UTGAAENDE);
		Journalpost jp = mapper.map(request);

		assertEquals(JournalpostTypeCode.U, jp.getJournalposttype());
		assertNull(jp.getMottakskanal());
		assertEquals(UtsendingsKanalCode.NAV_NO, jp.getUtsendingskanal());
		assertEquals(JournalStatusCode.D, jp.getJournalstatus());
	}

	@Test
	public void shouldMapNotat() {
		OpprettJournalpostRequest request = createRequest(JournalpostType.NOTAT);
		Journalpost jp = mapper.map(request);

		assertEquals(JournalpostTypeCode.N, jp.getJournalposttype());
		assertNull(jp.getMottakskanal());
		assertEquals(UtsendingsKanalCode.NAV_NO, jp.getUtsendingskanal());
		assertEquals(JournalStatusCode.D, jp.getJournalstatus());
	}

	@Test
	public void shouldMapInngaaendeJournalpostUtenDokumenter() {
		OpprettJournalpostRequest request = createRequestUtenDokumenter(JournalpostType.INNGAAENDE);
		Journalpost jp = mapper.map(request);

		assertEquals(JournalpostTypeCode.I, jp.getJournalposttype());
		assertEquals(JournalStatusCode.OD, jp.getJournalstatus());
	}

	@Test
	public void shouldMapUtgaaendeJournalpostUtenDokumenter() {
		OpprettJournalpostRequest request = createRequestUtenDokumenter(JournalpostType.UTGAAENDE);
		Journalpost jp = mapper.map(request);

		assertEquals(JournalpostTypeCode.U, jp.getJournalposttype());
		assertEquals(JournalStatusCode.R, jp.getJournalstatus());
	}

}