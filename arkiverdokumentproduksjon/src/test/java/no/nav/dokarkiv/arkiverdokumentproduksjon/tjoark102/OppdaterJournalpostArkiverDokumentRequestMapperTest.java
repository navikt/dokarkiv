package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.oppdaterjournalpostarkiverdokument.Fildetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OppdaterJournalpostArkiverDokumentRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.util.GregorianCalendar;
import java.util.Set;

import static no.nav.dokarkiv.core.CoreConfig.ZONEID_NORGE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ExtendWith(MockitoExtension.class)
public class OppdaterJournalpostArkiverDokumentRequestMapperTest {
	private static final Long JOURNALPOST_ID = 83409834L;
	private static final Long DOKUMENTINFO_ID = 9834928034L;
	private static final UtsendingsKanalCode UTSENDINGSKANAL = UtsendingsKanalCode.NAV_NO;
	private static final VariantFormatCode VARIANTFORMAT = VariantFormatCode.ARKIV;
	private static final FilTypeCode FILTYPE = FilTypeCode.XML;
	private static final String ENDRET_AV_NAVN = "Siri Saksbehandler";
	private static final String DOKUMENTINNHOLD = "test dokumeprivate";
	private static final LocalDateTime DATO_DOKUMENT = LocalDateTime.now();

	@InjectMocks
	private OppdaterJournalpostArkiverDokumentRequestMapper requestMapper;

	private OppdaterJournalpostArkiverDokumentRequest wsRequest;
	private OppdaterJournalpostArkiverDokumentRequestTo requestTo;

	@BeforeEach
	public void setUp() throws Exception {
		createRequest();
		RequestContextSetter.setRequestContextForUnitTest();
	}

	@Test
	public void shouldMapArkiverDokumentOgFerdigstillRequestToTransferObject() throws Exception {
		requestTo = requestMapper.map(wsRequest);
		assertThat(requestTo.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(requestTo.getDokumentInfoId(), is(DOKUMENTINFO_ID));
		assertThat(requestTo.getUtsendingskanal(), is(UTSENDINGSKANAL));
		assertThat(requestTo.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		Assertions.assertThat(requestTo.getDatoDokument()).isEqualToIgnoringNanos(DATO_DOKUMENT);
		assertFilDetaljer(requestTo);
	}

	private void assertFilDetaljer(OppdaterJournalpostArkiverDokumentRequestTo requestTo) {
		Set<FilDetaljer> filDetaljerSet = requestTo.getFildetaljer();
		FilDetaljer fileDetaljer = filDetaljerSet.iterator().next();
		assertThat(filDetaljerSet.size(), is(1));
		assertThat(fileDetaljer.getFiltype(), is(FILTYPE));
		assertThat(fileDetaljer.getVariantFormat(), is(VARIANTFORMAT));
		assertThat(fileDetaljer.getFileContent(), is(DOKUMENTINNHOLD.getBytes()));
		assertThat(fileDetaljer.getFilstorrelse(), is(Integer.toString(DOKUMENTINNHOLD.length())));
	}

	private void createRequest() throws Exception {
		wsRequest = new OppdaterJournalpostArkiverDokumentRequest();
		wsRequest.setJournalpostId(JOURNALPOST_ID);
		wsRequest.setDokumentInfoId(DOKUMENTINFO_ID);
		wsRequest.setUtsendingskanal(UTSENDINGSKANAL.name());
		wsRequest.setEndretAvNavn(ENDRET_AV_NAVN);
		wsRequest.setDatoDokument(toXMLGregorianCalendar(DATO_DOKUMENT));
		wsRequest.getFildetaljerListe().add(0, createFilDetaljer());
	}

	private Fildetaljer createFilDetaljer() {
		Fildetaljer fildetaljer = new Fildetaljer();
		fildetaljer.setFiltype(FILTYPE.name());
		fildetaljer.setVariantformat(VARIANTFORMAT.name());
		fildetaljer.setRedigerbartDokument(DOKUMENTINNHOLD.getBytes());
		return fildetaljer;
	}

	private static XMLGregorianCalendar toXMLGregorianCalendar(LocalDateTime dateTime) {
		try {
			GregorianCalendar c = GregorianCalendar.from(dateTime.atZone(ZONEID_NORGE));
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
}