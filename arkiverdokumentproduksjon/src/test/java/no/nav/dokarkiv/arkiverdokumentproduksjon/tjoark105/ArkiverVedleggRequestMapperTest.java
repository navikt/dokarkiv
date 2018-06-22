package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkivervedlegg.DokumentInfo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkivervedlegg.Fildetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkivervedlegg.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggRequest;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class ArkiverVedleggRequestMapperTest {
	public static final Long JOURNALPOST_ID = 200L;
	public static final String ENDRET_AV_NAVN = "Endre Tavnavn";
	public static final String TITTEL = "Tittelen";
	public static final String DOKUMENT_TYPE_ID = "1";
	public static final String BREVKODE = "BK";

	private ArkiverVedleggRequestMapper requestMapper;

	@Before
	public void setUp() {
		requestMapper = new ArkiverVedleggRequestMapper();
	}

	@Test
	public void shouldMapArkiverVedleggRequest() {
		ArkiverVedleggRequest request = createArkiverVedleggRequest();

		ArkiverVedleggRequestTo to = requestMapper.map(request);

		assertThat(to.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(to.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(to.getDokumentInfo().getBrevkode(), is(BREVKODE));
		assertThat(to.getDokumentInfo().getTittel(), is(TITTEL));
		assertThat(to.getDokumentInfo().getDokumenttypeId(), is(DOKUMENT_TYPE_ID));
		assertThat(to.getDokumentInfo().getKategori(), is(DokumentKategoriCode.B));
		FilDetaljer fd1 = to.getDokumentInfo().getFildetaljerListe().iterator().next();
		assertThat(fd1.getFiltype(), is(FilTypeCode.PDF));
		assertThat(fd1.getVariantFormat(), is(VariantFormatCode.ARKIV));
	}

	private ArkiverVedleggRequest createArkiverVedleggRequest() {
		ArkiverVedleggRequest request = new ArkiverVedleggRequest();
		Journalpost jp = createJournalpost();
		request.setJournalpost(jp);
		request.setFerdigstillDokument(false);
		return request;
	}

	private Journalpost createJournalpost() {
		Journalpost jp = new Journalpost();
		jp.setJournalpostId(JOURNALPOST_ID.toString());
		jp.setEndretAvNavn(ENDRET_AV_NAVN);
		jp.setDokumentInfo(createDokumentInfo());
		return jp;
	}

	private DokumentInfo createDokumentInfo() {
		DokumentInfo di = new DokumentInfo();
		di.setTittel(TITTEL);
		di.setSensitivt(true);
		di.setKategori(DokumentKategoriCode.B.name());
		di.setDokumentTypeId(DOKUMENT_TYPE_ID);
		di.setBrevkode(BREVKODE);
		di.getFildetaljer().add(createFildetaljer());
		return di;
	}

	private Fildetaljer createFildetaljer() {
		Fildetaljer fd = new Fildetaljer();
		fd.setIkkeRedigerbartDokument(new byte[]{'t', 'e', 's', 't'});
		fd.setFiltype(FilTypeCode.PDF.name());
		fd.setVariantformat(VariantFormatCode.ARKIV.name());
		return fd;
	}
}