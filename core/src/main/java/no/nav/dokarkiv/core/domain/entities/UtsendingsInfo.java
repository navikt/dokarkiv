package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "t_utsendings_info")
@NoArgsConstructor
@Getter
public class UtsendingsInfo {

	@Id
	@Column(name = "journalpost_id", insertable = false, updatable = false)
	private long journalpostId;

	@MapsId
	@OneToOne
	@JoinColumn(name = "journalpost_id", referencedColumnName = "journalpost_id", nullable = false)
	private Journalpost journalpost;

	@Embedded
	private FysiskPostadresse fysiskPostadresse;
	@Embedded
	private DigitalPostadresse digitalPostadresse;
	@Embedded
	private NavNoVarsling navNoVarsling;

	public UtsendingsInfo(Journalpost journalpost, FysiskPostadresse fysiskPostAdresse) {
		this.journalpost = journalpost;
		this.fysiskPostadresse = fysiskPostAdresse;
	}

	public UtsendingsInfo(Journalpost journalpost, DigitalPostadresse digitalPostadresse) {
		this.journalpost = journalpost;
		this.digitalPostadresse = digitalPostadresse;
	}

	public UtsendingsInfo(Journalpost journalpost, NavNoVarsling navNoVarsling) {
		this.journalpost = journalpost;
		this.navNoVarsling = navNoVarsling;
	}

	@Embeddable
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class FysiskPostadresse {
		@Size(max = 200)
		@Column(name = "adresselinje1")
		private String adresselinje1;
		@Size(max = 200)
		@Column(name = "adresselinje2")
		private String adresselinje2;
		@Size(max = 200)
		@Column(name = "adresselinje3")
		private String adresselinje3;
		@Size(max = 10)
		@Column(name = "postnummer")
		private String postnummer;
		@Size(max = 200)
		@Column(name = "poststed")
		private String poststed;
		@Size(max = 2)
		@Column(name = "landkode")
		private String landkode;
	}

	@Embeddable
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DigitalPostadresse {
		@Size(max = 100)
		@Column(name = "digitalpostkasseadresse")
		private String adresse;
		@Size(max = 20)
		@Column(name = "digitalpostkasseleverandor")
		private String postkasseLeverandor;
	}

	@Embeddable
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class NavNoVarsling {
		@Size(max = 200)
		@Column(name = "digital_kontaktinformasjon")
		private String kontaktinformasjon;
		@Size(max = 4000)
		@Column(name = "varslingstekst")
		private String varslingstekst;
	}

	public long getId() {
		return getJournalpostId();
	}
}
