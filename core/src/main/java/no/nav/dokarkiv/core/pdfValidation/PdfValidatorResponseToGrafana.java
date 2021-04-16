package no.nav.dokarkiv.core.pdfValidation;

import lombok.Getter;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import org.springframework.beans.BeanUtils;

@Getter
public class PdfValidatorResponseToGrafana extends PdfValidatorResponse{


	private String filUuid;

	private String teamOrServiceUser;

	private String arkivvariant;

	private String dokumenttype;

	private long dokumentinfoId;

	public PdfValidatorResponseToGrafana(PdfValidatorResponse response, FilDetaljer filDetaljer){
		BeanUtils.copyProperties(this, response);
		this.filUuid = filDetaljer.getFilUuid();
		this.teamOrServiceUser = filDetaljer.getOpprettetKildeNavn() == null ? "ukjent" : filDetaljer.getOpprettetKildeNavn() ;
		this.arkivvariant = filDetaljer.getVariantFormat() == null ? "Ukjent format" : filDetaljer.getVariantFormat().toString();
		this.dokumenttype = filDetaljer.getFiltype().toString();
		this.dokumentinfoId = filDetaljer.getDokumentInfo().getDokumentInfoId();

	}

}
