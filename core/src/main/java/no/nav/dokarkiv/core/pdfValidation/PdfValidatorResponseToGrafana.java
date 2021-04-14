package no.nav.dokarkiv.core.pdfValidation;

import lombok.Getter;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import org.springframework.beans.BeanUtils;

@Getter
public class PdfValidatorResponseToGrafana extends PdfValidatorResponse{

	/*
	antall dokumenter på ugyldig format
	tema/fagområde
	journalposttype (inngående, utgående, notat)
	hvem (team/servicebruker) som arkiverer
	Tenker vi kanskje kun bør samle statistikk på arkivvarianten (om det arkiveres flere), og kun de som har filtype "PDF" og "PDFA".
	Hadde det gått an å lage grafana-board som også viser andel oppgitte PDFA som faktisk er pdfa, pluss det samme for oppgitte PDF?
	Jeg tror også det er nyttig å skille på hoveddokument/vedlegg.
	 */

	private String filUuid;

	private String teamOrServiceUser;

	private String arkivvariant;

	private String dokumenttype;

	@Setter
	private String tilknyttetSom;

	public PdfValidatorResponseToGrafana(PdfValidatorResponse response, FilDetaljer filDetaljer){
		BeanUtils.copyProperties(this, response);
		this.filUuid = filDetaljer.getFilUuid();
		this.teamOrServiceUser = filDetaljer.getOpprettetKildeNavn() == null ? "ukjent" : filDetaljer.getOpprettetKildeNavn() ;
		this.arkivvariant = filDetaljer.getVariantFormat() == null ? "Ukjent format" : filDetaljer.getVariantFormat().toString();
		this.dokumenttype = filDetaljer.getFiltype().toString();

	}
}
