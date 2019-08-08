package no.nav.dokarkiv.sak;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.sak.validering.ExactlyOneOf;
import no.nav.dokarkiv.sak.validering.NotNullWhenDependsOnHasValue;
import no.nav.dokarkiv.sak.validering.Organisasjonsnummer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ExactlyOneOf(fields = {"aktoerId", "orgnr"})
@NotNullWhenDependsOnHasValue(field = "applikasjon", dependsOnField = "fagsakNr")
public class SakJson {
	private Long id;

	@NotNull(message = "{no.nav.dokarkiv.sak.tema.NotNull}")
	@Size(max = 40)
	private String tema;

	@Size(max = 40)
	private String applikasjon;

	@Size(message = "{no.nav.dokarkiv.sak.aktoerId.Size}", max = 40)
	private String aktoerId;

	@Organisasjonsnummer
	@Size(message = "{no.nav.dokarkiv.sak.orgnr.Size}", max = 9)
	private String orgnr;

	private String fagsakNr;

	private String opprettetAv;
	private LocalDateTime opprettetTidspunkt;


	public SakJson() {
		//JaxRS
	}

	SakJson(Sak sak) {
		this.id = sak.getSakId();
		this.tema = sak.getTema();
		this.aktoerId = sak.getAktoerId();
		this.orgnr = sak.getOrgnr();
		this.fagsakNr = sak.getFagsakNr();
		this.applikasjon = sak.getApplikasjon();
		this.opprettetAv = sak.getOpprettetAv();
		this.opprettetTidspunkt = sak.getOpprettetTidspunkt();
	}


	@JsonProperty("id")
	public Long getId() {
		return id;
	}

	@JsonProperty("tema")
	@ApiModelProperty(value = "Kode for tema iht. felles kodeverk", example = "AAP")
	public String getTema() {
		return tema;
	}

	public void setTema(String tema) {
		this.tema = tema;
	}

	@JsonProperty("applikasjon")
	@ApiModelProperty(value = "Kode for applikasjon iht. felles kodeverk", notes = "For generelle saker skal denne være blank (Legacy = FS22). For fagsaker, i.e saker der det refereres" +
			"til et fagsaknr, så skal man benytte applikasjonskoden for fagsystemet der saken behandles", example = "IT01")
	public String getApplikasjon() {
		return applikasjon;
	}

	public void setApplikasjon(String applikasjon) {
		this.applikasjon = applikasjon;
	}

	@JsonProperty("aktoerId")
	@ApiModelProperty(value = "Id til aktøren saken gjelder", example = "***gammelt_fnr***")
	public String getAktoerId() {
		return aktoerId;
	}

	public void setAktoerId(String aktoerId) {
		this.aktoerId = aktoerId;
	}

	@JsonProperty("opprettetAv")
	@ApiModelProperty("Brukerident til den som opprettet saken")
	public String getOpprettetAv() {
		return opprettetAv;
	}

	@JsonProperty("orgnr")
	@ApiModelProperty(value = "Orgnr til foretaket saken gjelder")
	public String getOrgnr() {
		return orgnr;
	}

	public void setOrgnr(String orgnr) {
		this.orgnr = orgnr;
	}

	@JsonProperty("fagsakNr")
	@ApiModelProperty("Fagsaknr for den aktuelle saken - hvis aktuelt")
	public String getFagsakNr() {
		return fagsakNr;
	}

	public void setFagsakNr(String fagsakNr) {
		this.fagsakNr = fagsakNr;
	}

	@JsonProperty("opprettetTidspunkt")
	@ApiModelProperty("Opprettet tidspunkt iht. ISO-8601")
	public String getOpprettetTidspunkt() {
		return ZonedDateTime.of(opprettetTidspunkt, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}

	Sak toSak(String opprettetAv) {
		return Sak.builder()
				.aktoerId(aktoerId)
				.orgnr(orgnr)
				.tema(tema)
				.fagsakNr(fagsakNr)
				.applikasjon(applikasjon)
				.opprettetAv(opprettetAv)
				.opprettetTidspunkt(LocalDateTime.now())
				.build();
	}
}
