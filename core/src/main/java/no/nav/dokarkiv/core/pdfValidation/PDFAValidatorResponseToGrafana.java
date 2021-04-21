package no.nav.dokarkiv.core.pdfValidation;

import lombok.Getter;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import org.springframework.beans.BeanUtils;

@Getter
public class PDFAValidatorResponseToGrafana extends PDFAValidatorResponse {

	private String filUuid;
	private String teamOrServiceUser;
	private String arkivvariant;
	private String dokumenttype;
	private long dokumentinfoId;

	public PDFAValidatorResponseToGrafana(PDFAValidatorResponse response, FilDetaljer filDetaljer){
		BeanUtils.copyProperties(this, response);
		this.filUuid = filDetaljer.getFilUuid();
		this.teamOrServiceUser = filDetaljer.getOpprettetKildeNavn() == null ? "ukjent" : filDetaljer.getOpprettetKildeNavn() ;
		this.arkivvariant = filDetaljer.getVariantFormat() == null ? "Ukjent format" : filDetaljer.getVariantFormat().toString();
		this.dokumenttype = filDetaljer.getFiltype().toString();
		this.dokumentinfoId = filDetaljer.getDokumentInfo().getDokumentInfoId();

	}

}
