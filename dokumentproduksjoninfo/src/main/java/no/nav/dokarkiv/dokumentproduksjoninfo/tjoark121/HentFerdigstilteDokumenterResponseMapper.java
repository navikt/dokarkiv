package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121;

import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.informasjon.Dokument;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentFerdigstilteDokumenterResponse;
import org.springframework.stereotype.Component;

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
//			ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(domainResponse.getFil(), MediaType.PDF);
//			dokument.setFil(new DataHandler(byteArrayDataSource)); FIXME
			dokument.setTittel(domainResponse.getTittel());
			wsResponse.getDokumentListe().add(dokument);
		}
		return wsResponse;
	}

}
