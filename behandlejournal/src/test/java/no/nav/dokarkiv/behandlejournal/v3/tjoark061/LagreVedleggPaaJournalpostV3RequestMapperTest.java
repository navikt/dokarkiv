package no.nav.dokarkiv.behandlejournal.v3.tjoark061;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Arkivfiltyper;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Dokumenttyper;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.NoekkelVerdiPar;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.NoekkelVerdiSett;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.UstrukturertInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Variantformater;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.lagrevedleggpaajournalpost.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.LagreVedleggPaaJournalpostRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for DefaultLagreVedleggPaaJournalpostResponseMapper
 *
 * @author Rune Romundstad, Visma Consulting
 */
public class LagreVedleggPaaJournalpostV3RequestMapperTest {
	private static final String SPORING_FORNAVN = "fornavn";
	private static final String SPORING_ETTERNAVN = "etternavn";
	private static final String APPLIKASJONS_ID = "applikasjonsid";
	private static final String JOURNALPOST_ID = "1";
	private static final String NOEKKELVERDI_KEY = "tittel";
	private static final String NOEKKELVERDI_VALUE = "verdi";
	private static final String FILNAVN = "filnavn";
	private static final String FILTYPE = "PDF";
	private static final String VARIANTFORMAT = "ARKIV";
	private static final String INNHOLD = "innhold";
	private static final String BREVKODE = "brevkode";
	private static final boolean BEGRENSET_PARTS_INNSYN = true;

	private LagreVedleggPaaJournalpostV3RequestMapper requestMapper;
	private LagreVedleggPaaJournalpostRequest wsRequest;

	@BeforeEach
	public void init() {
		requestMapper = new LagreVedleggPaaJournalpostV3RequestMapper();
		wsRequest = createRequest(JOURNALPOST_ID, createDokumentInfo());
	}

	@Test
	public void shouldMapFromWsRequestToDomainRequest() throws Exception {
		assertRequests(requestMapper.map(wsRequest));
	}

	private void assertRequests(
			no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostRequest domainRequest) throws Exception {
		assertThat(domainRequest.getJournalpostId(), is(Long.parseLong(JOURNALPOST_ID)));
		assertThat(domainRequest.getSporingsMetaData().getPersonFornavn(), is(SPORING_FORNAVN));
		assertThat(domainRequest.getSporingsMetaData().getPersonEtternavn(), is(SPORING_ETTERNAVN));
		assertThat(domainRequest.getSporingsMetaData().getApplikasjonsID(), is(APPLIKASJONS_ID));
		assertDokumentInfo(domainRequest.getDokumentInfo());
	}

	private void assertDokumentInfo(DokumentInfo dokumentInfo) throws Exception {
		assertThat(dokumentInfo.getBrevkode(), is(BREVKODE));
		assertTilleggsopplysninger(dokumentInfo.getTilleggsopplysninger());
		assertFilDetaljer(dokumentInfo.getFildetaljerListe().iterator().next());
	}

	private void assertTilleggsopplysninger(Map<String, String> tilleggsopplysninger) {
		assertTrue(tilleggsopplysninger.containsKey(NOEKKELVERDI_KEY));
		assertThat(tilleggsopplysninger.get(NOEKKELVERDI_KEY), is(NOEKKELVERDI_VALUE));
	}

	private void assertFilDetaljer(FilDetaljer fildetaljer) throws Exception {
		assertThat(fildetaljer.getFilnavn(), is(FILNAVN));
		assertThat(fildetaljer.getFiltype().toString(), is(FILTYPE));
		assertThat(fildetaljer.getVariantFormat().toString(), is(VARIANTFORMAT));
		assertThat(fildetaljer.getFileContent(), is(INNHOLD.getBytes()));
	}

	private LagreVedleggPaaJournalpostRequest createRequest(
			String journalpostId, JournalfoertDokumentInfo dokumentInfo) {
		LagreVedleggPaaJournalpostRequest request = new LagreVedleggPaaJournalpostRequest();
		request.setJournalpostId(journalpostId);
		request.setJournalfortDokumentInfo(dokumentInfo);
		request.setPersonFornavn(SPORING_FORNAVN);
		request.setPersonEtternavn(SPORING_ETTERNAVN);
		request.setApplikasjonsID(APPLIKASJONS_ID);
		return request;
	}

	private JournalfoertDokumentInfo createDokumentInfo() {
		JournalfoertDokumentInfo dokumentInfo = new JournalfoertDokumentInfo();
		dokumentInfo.setBegrensetPartsInnsyn(BEGRENSET_PARTS_INNSYN);
		Dokumenttyper dokumenttyper = new Dokumenttyper();
		dokumenttyper.setValue(BREVKODE);
		dokumentInfo.setDokumentType(dokumenttyper);
		dokumentInfo.setTilleggsopplysninger(createNoekkelVerdiSett());
		dokumentInfo.getBeskriverInnhold().add(createDokumentInnhold());
		return dokumentInfo;
	}

	private UstrukturertInnhold createDokumentInnhold() {
		UstrukturertInnhold innhold = new UstrukturertInnhold();
		innhold.setFilnavn(FILNAVN);
		Arkivfiltyper arkivfiltyper = new Arkivfiltyper();
		arkivfiltyper.setValue(FILTYPE);
		innhold.setFiltype(arkivfiltyper);
		Variantformater variantformater = new Variantformater();
		variantformater.setValue(VARIANTFORMAT);
		innhold.setVariantformat(variantformater);
		innhold.setInnhold(INNHOLD.getBytes());
		return innhold;
	}

	private NoekkelVerdiSett createNoekkelVerdiSett() {
		NoekkelVerdiSett noekkelVerdiSett = new NoekkelVerdiSett();
		noekkelVerdiSett.getInneholderNoekkelVerdiPar().add(createNoekkelVerdiPar());
		return noekkelVerdiSett;
	}

	private NoekkelVerdiPar createNoekkelVerdiPar() {
		NoekkelVerdiPar noekkelVerdiPar = new NoekkelVerdiPar();
		noekkelVerdiPar.setNoekkel(NOEKKELVERDI_KEY);
		noekkelVerdiPar.setVerdi(NOEKKELVERDI_VALUE);
		return noekkelVerdiPar;
	}


}
