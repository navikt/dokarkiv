package no.nav.dokarkiv.innsynjournal.v2.tjoark059;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.JournalpostNotSupportedException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.UgyldigInputException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.orm.hibernate5.HibernateTemplate;

import java.util.Date;
import java.util.List;


/**
 * Unit tests for {@link IdentifiserJournalpostService}
 *
 * @author Ketill Fenne, Visma Consulting
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class DefaultIdentifiserJournalpostServiceTest {

	private static final String KANAL_REFERANSE_ID = "KanalReferanseId";
	private static final String MOTTAKSKANAL= "NAV_NO";
	private static final Long DOK_INFO_REL_ID = 3L;
	private static final String DOKUMENT_TITTEL = "tittel";
	private static final Long HOVEDDOKUMENT_INFO_ID = 10L;
	private static final Long VEDLEGGDOKUMENT_INFO_ID = 11L;
	private static final Long JOURNALPOST_ID = 0L;
	private static final DokumentStatusCode DOKUMENT_STATUS = DokumentStatusCode.FERDIGSTILT;
	private static final Date DOKUMENT_FERDIG_DATO = new Date(1L);
	private static final Date DOKUMENT_FERDIG_DATO_OLD = new Date(0L);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private JoarkRepository joarkRepository;
	@Mock
	private HibernateTemplate hibernateTemplate;
	@Mock
	private Session sessionMock;
	@Mock
	private SessionFactory sessionFactoryMock;

	@InjectMocks
	private DefaultIdentifiserJournalpostService service;

	private List<Journalpost> journalposts;

	@Before
	public void setUp() throws Exception {
		when(hibernateTemplate.getSessionFactory()).thenReturn(sessionFactoryMock);
		when(sessionFactoryMock.getCurrentSession()).thenReturn(sessionMock);
//		when(joarkRepository.findJournalpostByKanal(any(String.class), any(MottaksKanalCode.class)))
//				.thenReturn(journalposts); FIXME
	}

	@Test
	public void shouldCreateParams() throws Exception {
		journalposts = Lists.newArrayList(createJournalpost(JournalpostTypeCode.I));
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<MottaksKanalCode> captorMottaksKanalCode = ArgumentCaptor.forClass(MottaksKanalCode.class);
//		when(joarkRepository.findJournalpostByKanal(captor.capture(), captorMottaksKanalCode.capture()))
//				.thenReturn(journalposts);

		service.identifiserJournalpost(createRequest(KANAL_REFERANSE_ID, MOTTAKSKANAL));

		assertThat(captor.getValue(), is(KANAL_REFERANSE_ID));
		assertThat(captorMottaksKanalCode.getValue().name(), is(MOTTAKSKANAL));
	}

	@Test
	public void shouldThrowIfEmptyKanalReferanse() throws Exception {
		thrown.expect(UgyldigInputException.class);
		thrown.expectMessage("KanalReferanseId cannot be empty");
		service.identifiserJournalpost(createRequest(null, null));
	}


	@Test
	public void shouldThrowWrongJournalpostType() throws Exception {
		thrown.expect(JournalpostIkkeInngaaendeException.class);
		thrown.expectMessage("Journalposten, journalpostId="+JOURNALPOST_ID+", som ble funnet er ikke inngående");
		journalposts = Lists.newArrayList(createJournalpost(JournalpostTypeCode.U));
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<MottaksKanalCode> captorMottaksKanalCode = ArgumentCaptor.forClass(MottaksKanalCode.class);
//		when(joarkRepository.findJournalpostByKanal(captor.capture(), captorMottaksKanalCode.capture()))
//				.thenReturn(journalposts);

		service.identifiserJournalpost(createRequest(KANAL_REFERANSE_ID, MOTTAKSKANAL));
	}

	@Test
	public void shouldThrowJournalportManglerHoveddokument() throws Exception {
		thrown.expect(JournalpostNotSupportedException.class);
		thrown.expectMessage("Journalposten, journalpostId="+JOURNALPOST_ID+", mangler hoveddokument");
		journalposts = Lists.newArrayList(createJournalpostNoHoveddokument(JournalpostTypeCode.I));
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<MottaksKanalCode> captorMottaksKanalCode = ArgumentCaptor.forClass(MottaksKanalCode.class);
//		when(joarkRepository.findJournalpostByKanal(captor.capture(), captorMottaksKanalCode.capture()))
//				.thenReturn(journalposts);

		service.identifiserJournalpost(createRequest(KANAL_REFERANSE_ID, MOTTAKSKANAL));
	}

	private IdentifiserJournalpostToRequest createRequest(String kanalReferanseId, String mottakskanal) {
		IdentifiserJournalpostToRequest identifiserJournalpostToRequest = new IdentifiserJournalpostToRequest();
		identifiserJournalpostToRequest.setKanalReferanseId(kanalReferanseId);
		identifiserJournalpostToRequest.setMottaksKanal(MottaksKanalCode.NAV_NO);
		return identifiserJournalpostToRequest;
	}

	private Journalpost createJournalpost(JournalpostTypeCode typeCode) {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.journalpostType(typeCode)
				.dokumentInfoRelasjoner(
						createDokInfoRel(TilknyttetJournalpostSomCode.HOVEDDOKUMENT, DOKUMENT_FERDIG_DATO, DOK_INFO_REL_ID, HOVEDDOKUMENT_INFO_ID),
						createDokInfoRel(TilknyttetJournalpostSomCode.VEDLEGG, DOKUMENT_FERDIG_DATO_OLD, DOK_INFO_REL_ID + 1, VEDLEGGDOKUMENT_INFO_ID ),
						createDokInfoRel(TilknyttetJournalpostSomCode.VEDLEGG, DOKUMENT_FERDIG_DATO_OLD, DOK_INFO_REL_ID + 2, VEDLEGGDOKUMENT_INFO_ID ))
				.build();
	}

	private Journalpost createJournalpostNoHoveddokument(JournalpostTypeCode typeCode) {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.journalpostType(typeCode)
				.dokumentInfoRelasjoner(
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
				.tittel(DOKUMENT_TITTEL)
				.dokumentInfoId(dokumentInfoId)
				.build();
	}

}
