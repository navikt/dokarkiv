package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121;

import no.nav.dokarkiv.core.jaxws.ByteArrayDataSource;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.informasjon.Dokument;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentFerdigstilteDokumenterResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import java.util.List;

/**
 * Class for mapping the domain response to web service response for HentFerdigstilteDokumenter (TJOARK121)
 * 
 * @author Stig Strøm
 *
 */
@Component
public class HentFerdigstilteDokumenterResponseMapper {
	
	
	/**
	 * Maps domain response to web service response for HentFerdigstilteDokumenter
	 * 
	 * Converts bytearray to Datahandler for MTOM
	 * 
	 * @param domainResponseList
	 * @return
	 */
	public HentFerdigstilteDokumenterResponse map(List<HentFerdigstilteDokumenterResponseTo> domainResponseList) {
		HentFerdigstilteDokumenterResponse wsResponse = new HentFerdigstilteDokumenterResponse();
		for (HentFerdigstilteDokumenterResponseTo domainResponse : domainResponseList) {
			Dokument dokument = new Dokument();
			dokument.setDokumentInfoId(domainResponse.getDokumentInfoId());
			ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(domainResponse.getFil(), MediaType.APPLICATION_PDF_VALUE);
			dokument.setFil(new DataHandler(byteArrayDataSource));
			dokument.setTittel(domainResponse.getTittel());
			wsResponse.getDokumentListe().add(dokument);
		}
		return wsResponse;
	}

}
