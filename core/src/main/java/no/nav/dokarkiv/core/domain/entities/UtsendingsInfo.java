package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.util.ConverterUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.IOException;
import java.util.Map;

/**
 * Inneholder metadata om utsending av {@link Journalpost}
 */
@Entity
@Table(name = "t_utsendings_info")
@NoArgsConstructor
@Getter
@Setter
public class UtsendingsInfo {

	@Id
	private Long journalpostId;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "journalpost_id")
	private Journalpost journalpost;

	@Embedded
	private FysiskPostadresse fysiskPostadresse;
	@Embedded
	private DigitalPostadresse digitalPostadresse;
	@Embedded
	private NavNoVarsling navNoVarsling;

	public UtsendingsInfo(Journalpost journalpost, FysiskPostadresse fysiskPostAdresse) {
		UtsendingsKanalCode utsendingskanal = journalpost.getUtsendingskanal();
		if (utsendingskanal != UtsendingsKanalCode.S) {
			throw new IllegalArgumentException(String.format("Kan ikke sette UtsendingsInfo av type=%s for utsendingskanal=%s",
					UtsendingsInfo.FysiskPostadresse.class.getSimpleName(), utsendingskanal));
		}
		this.journalpost = journalpost;
		this.fysiskPostadresse = fysiskPostAdresse;
	}

	public UtsendingsInfo(Journalpost journalpost, DigitalPostadresse digitalPostadresse, EpostVarsel epostVarsel, SmsVarsel smsVarsel) {
		UtsendingsKanalCode utsendingskanal = journalpost.getUtsendingskanal();
		if (utsendingskanal != UtsendingsKanalCode.SDP) {
			throw new IllegalArgumentException(String.format("Kan ikke sette UtsendingsInfo av type=%s for utsendingskanal=%s",
					UtsendingsInfo.DigitalPostadresse.class.getSimpleName(), utsendingskanal));
		}
		this.journalpost = journalpost;
		this.digitalPostadresse = digitalPostadresse;
	}

	public UtsendingsInfo(Journalpost journalpost, NavNoVarsling navNoVarsling, EpostVarsel epostVarsel, SmsVarsel smsVarsel) {
		UtsendingsKanalCode utsendingskanal = journalpost.getUtsendingskanal();
		if (utsendingskanal != UtsendingsKanalCode.NAV_NO) {
			throw new IllegalArgumentException(String.format("Kan ikke sette UtsendingsInfo av type=%s for utsendingskanal=%s",
					UtsendingsInfo.NavNoVarsling.class.getSimpleName(), utsendingskanal));
		}
		this.journalpost = journalpost;
		this.navNoVarsling = navNoVarsling;
	}

	@Embeddable
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class FysiskPostadresse {
		@Column(name = "adresselinje1", length = 200)
		private String adresselinje1;
		@Column(name = "adresselinje2", length = 200)
		private String adresselinje2;
		@Column(name = "adresselinje3", length = 200)
		private String adresselinje3;
		@Column(name = "postnummer", length = 10)
		private String postnummer;
		@Column(name = "poststed", length = 200)
		private String poststed;
		@Column(name = "landkode", length = 2)
		private String landkode;
	}

	@Embeddable
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DigitalPostadresse {
		@Column(name = "digitalpostkasseadresse", length = 100)
		private String adresse;
		@Column(name = "digitalpostkasseleverandor", length = 20)
		private String postkasseLeverandor;
	}

	@Embeddable
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class NavNoVarsling {
		@Column(name = "digital_kontaktinformasjon", length = 200)
		private String kontaktinformasjon;

		/**
		 * @deprecated Bruk SmsVarsel og EpostVarsel for lik logikk som for SDP
		 */
		@Deprecated
		@Column(name = "varslingstekst", length = 4000)
		private String varslingstekst;
	}

	@Embeddable
	@NoArgsConstructor
	public static class SmsVarsel extends Varsling {
		public SmsVarsel(String varseltekst) {
			smsvarsel = marshallAndPrepareData(Map.of("tekst", varseltekst));
		}

		@Column(name = "smsvarsel", length = 4000)
		private String smsvarsel;

		@Override
		protected String getMarshalledData() {
			return smsvarsel;
		}

		public String getTekst() {
			return getData("tekst");
		}
	}

	@Embeddable
	@NoArgsConstructor
	public static class EpostVarsel extends Varsling {
		public EpostVarsel(String tittel, String varseltekst) {
			epostvarsel = marshallAndPrepareData(Map.of("tittel", tittel, "tekst", varseltekst));
		}

		@Column(name = "epostvarsel", length = 4000)
		private String epostvarsel;

		@Override
		protected String getMarshalledData() {
			return epostvarsel;
		}

		public String getTittel() {
			return getData("tittel");
		}

		public String getTekst() {
			return getData("tekst");
		}
	}

	private static abstract class Varsling {
		private Map<String, String> unmarshalledData;

		protected String marshallAndPrepareData(Map<String, String> unmarshalledData) {
			this.unmarshalledData = unmarshalledData;
			try {
				return ConverterUtils.objectToJsonString(unmarshalledData);
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}

		protected String getData(String key) {
			if (unmarshalledData == null) {
				try {
					unmarshalledData = ConverterUtils.jsonStringToKeyValueMap(getMarshalledData(), String.class);
				} catch (IOException e) {
					return null;
				}
			}
			return unmarshalledData.get(key);
		}

		protected abstract String getMarshalledData();
	}
}
