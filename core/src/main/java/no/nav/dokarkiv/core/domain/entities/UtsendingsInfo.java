package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "t_utsendings_info")
@NoArgsConstructor
@Getter
public class UtsendingsInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_utsendings_info")
	@GenericGenerator(name = "seq_utsendings_info", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@Parameter(name = "sequence_name", value = "seq_utsendings_info")})
	@Column(name = "utsendings_info_id")
	private Long utsendingsInfoId;
	@OneToOne
	@JoinColumn(name = "journalpost_id", referencedColumnName = "journalpost_id", nullable = false)
	private Journalpost journalpost;

	@Embedded
	private FysiskPostadresse fysiskPostadresse;
	@Embedded
	private DigitalPostadresse digitalPostadresse;
	@Embedded
	private NavNoVarsling navNoVarsling;

	public UtsendingsInfo(Journalpost journalpost, FysiskPostAdresse fysiskPostAdresse) {
		this.journalpost = journalpost;
		this.fysiskPostAdresse = fysiskPostAdresse;
	}

	public UtsendingsInfo(Journalpost journalpost, SikkerDigitalPostAdresse sikkerDigitalPostAdresse) {
		this.journalpost = journalpost;
		this.sikkerDigitalPostAdresse = sikkerDigitalPostAdresse;
	}

	public UtsendingsInfo(Journalpost journalpost, NavNoVarsling navNoVarsling) {
		this.journalpost = journalpost;
		this.navNoVarsling = navNoVarsling;
	}

	@Embeddable
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	class FysiskPostAdresse  {
		@Column(name = "adresselinje1")
		private String adresselinje1;
		@Column(name = "adresselinje2")
		private String adresselinje2;
		@Column(name = "adresselinje3")
		private String adresselinje3;
		@Column(name = "postnummer")
		private String postnummer;
		@Column(name = "poststed")
		private String poststed;
		@Column(name = "landkode")
		private String landkode;
	}

	@Embeddable
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	class SikkerDigitalPostAdresse  {
		@Column(name = "digitalpostkasseadresse")
		private String digitalPostkasseAdresse;
		@Column(name = "digitalpostkasseleverandor")
		private String digitalPostkasseLeverandor;
	}

	@Embeddable
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	class NavNoVarsling  {
		@Column(name = "digital_kontaktinformasjon")
		private String navDigitalKontaktinformasjon;
		@Column(name = "varslingstekst")
		private String navVarslingstekst;
	}
}
