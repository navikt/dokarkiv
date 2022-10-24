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

	UtsendingsInfo(Journalpost journalpost, FysiskPostadresse fysiskPostAdresse) {
		this.journalpost = journalpost;
		this.fysiskPostadresse = fysiskPostAdresse;
	}

	UtsendingsInfo(Journalpost journalpost, DigitalPostadresse digitalPostadresse) {
		this.journalpost = journalpost;
		this.digitalPostadresse = digitalPostadresse;
	}

	UtsendingsInfo(Journalpost journalpost, NavNoVarsling navNoVarsling) {
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
		@Column(name = "varslingstekst", length = 4000)
		private String varslingstekst;
	}

	public long getId() {
		return getJournalpostId();
	}
}
