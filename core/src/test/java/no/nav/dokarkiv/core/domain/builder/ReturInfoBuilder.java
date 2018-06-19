package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.codes.ArsakReturCode;
import no.nav.dokarkiv.core.domain.entities.ReturInfo;

import java.util.Date;

/**
 * Builder for ReturInfo.
 *
 * @author Thao Thanh Nguyen, Visma Sirius
 */
@Deprecated // bruk lombok builder istedet
public class ReturInfoBuilder extends Builder<ReturInfo> {

	private ReturInfoBuilder(){ 
	}
	
	public static ReturInfoBuilder getReturInfoBuilder() {
		return new ReturInfoBuilder();
	}
	
	private Long returInfoId;
	private Date returDato;
	private ArsakReturCode arsakRetur;
	private String adresseSendtIgjen;
	private Date sendtIgjenDato;
	private String opprettetKildeNavn;
	private String endretKildeNavn;
	private ChangeStamp changeStamp;
	
	public ReturInfoBuilder returInfoId(Long value) {this.returInfoId = value; return this; }
	public ReturInfoBuilder returDato(Date value) {this.returDato = value; return this; }
	public ReturInfoBuilder arsakRetur(ArsakReturCode value) {this.arsakRetur = value; return this; }
	public ReturInfoBuilder adresseSendtIgjen(String value) {this.adresseSendtIgjen = value; return this; }
	public ReturInfoBuilder sendtIgjenDato(Date value) {this.sendtIgjenDato = value; return this; }
	public ReturInfoBuilder opprettetKildeNavn(String value) { this.opprettetKildeNavn = value; return this; }
	public ReturInfoBuilder endretKildeNavn(String value) { this.endretKildeNavn = value; return this; }
	public ReturInfoBuilder changeStamp(ChangeStamp value) { this.changeStamp = value; return this; }

	@Override
	public ReturInfo build() {
		ReturInfo returInfo = new ReturInfo(returInfoId, 1);
		returInfo.setReturDato(returDato);
		returInfo.setArsakRetur(arsakRetur);
		returInfo.setAdresseSendtIgjen(adresseSendtIgjen);
		returInfo.setSendtIgjenDato(sendtIgjenDato);
		returInfo.setOpprettetKildeNavn(opprettetKildeNavn);
		returInfo.setEndretKildeNavn(endretKildeNavn);
		returInfo.setChangeStamp(changeStamp);
		return returInfo;
	}
	
	
}
