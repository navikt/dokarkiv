package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.oppdaterjournalpostarkiverdokument.Fildetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OppdaterJournalpostArkiverDokumentRequest;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Øyvind Ølberg, Visma Consulting
 */
public class OppdaterJournalpostArkiverDokumentDataUtil {

	protected static final String BREVKODE = "brevkode";
	protected static final FagomradeCode FAGOMRADE = FagomradeCode.PEN;
	protected static final String LAND = "Norge";
	protected static final String DOKUMENT_TYPE_ID = "dokumentTypeId";
	protected static final String JOURNALFOERENDE_ENHET_REF = "2009";
	protected static final String HOVEDDOKUMENT = TilknyttetJournalpostSomCode.HOVEDDOKUMENT.name();
	protected static final String OPPRETTET_AV_NAVN = "Max Mekker";
	protected static final boolean SENSITIVITET = true;
	protected static final String TITTEL = "The Sound of Music";
	protected static final String KATEGORI = DokumentKategoriCode.B.name();
	protected static final String INNHOLD = "Sanger fra verden";
	protected static final String DOKUMENT_INNHOLD = "ustrukturertInnhold";
	protected static final String VARIANTFORMAT = "ARKIV";
	protected static final String EKSTERNPART_NAVN = "Jippi Hurra";
	protected static final Date DATO_DOKUMENT = new Date(1234567890);

	public static OppdaterJournalpostArkiverDokumentRequest createWsRequest(Long journalpostId, Long dokumentInfoId) {
		OppdaterJournalpostArkiverDokumentRequest wsRequest = new OppdaterJournalpostArkiverDokumentRequest();
		wsRequest.setDokumentInfoId(dokumentInfoId);
		wsRequest.setJournalpostId(journalpostId);
		wsRequest.setEndretAvNavn(EKSTERNPART_NAVN);
		wsRequest.setDatoDokument(toXMLGregorianCalendar(DATO_DOKUMENT));
		wsRequest.setUtsendingskanal(UtsendingsKanalCode.ALTINN.name());
		wsRequest.withFildetaljerListe(createFildetaljer());
		return wsRequest;
	}

	private static Fildetaljer createFildetaljer() {
		Fildetaljer fildetaljer = new Fildetaljer();
		fildetaljer.setFiltype(FilTypeCode.XML.name());
		fildetaljer.setVariantformat(VARIANTFORMAT);
		fildetaljer.setRedigerbartDokument(DOKUMENT_INNHOLD.getBytes());
		return fildetaljer;
	}

	private static XMLGregorianCalendar toXMLGregorianCalendar(Date date) {
		try {
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(date);
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch(DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
}
