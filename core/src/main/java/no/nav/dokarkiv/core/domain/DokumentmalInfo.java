package no.nav.dokarkiv.core.domain;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Domain entity representing dokumentmal info.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@Entity
@Table(name = "T_DOKUMENT_MAL_INFO")
@NamedQueries( { @NamedQuery(name = DokumentmalInfo.NQ_FIND_BY_BREV_KODER, 
							 query = "SELECT d FROM DokumentmalInfo d WHERE d.brevkode = :"
								 	+ DokumentmalInfo.NP_BREV_KODE)})
public class DokumentmalInfo extends AbstractPersistentDomainObject {

	/** Named Parameter */
	public static final String NP_BREV_KODE = "brevkode";
	/** Named Query */
	public static final String NQ_FIND_BY_BREV_KODER = "DokumentmalInfo.findDokumentmalInfosByBrevkoder";
	
	/** Serialization UID */
	private static final long serialVersionUID = -***gammelt_fnr***88597536L;

	@Id
	@Column(name = "brev_kode", nullable = false)
	private String brevkode;
	
	@Column(name = "brev_gruppe", nullable = false)
	private String brevgruppe;
	
	@Column(name = "tittel", nullable = false)
	private String tittel;
	
	@Column(name = "redigerbart", nullable = false)
	@Type(type = "org.hibernate.type.TrueFalseType")
	private Boolean redigerbart;
	
	@Column(name = "organ_internt")
	@Type(type = "org.hibernate.type.TrueFalseType")
	private Boolean organInternt;
	
	@Column(name = "sensitivt")
	@Type(type = "org.hibernate.type.TrueFalseType")
	private Boolean sensitivt;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "k_kategori_t", nullable = false)
	private DokumentKategoriCode dokumentKategori;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "k_journalpost_t", nullable = false)
	private JournalpostTypeCode journalpostType;

	/**
	 * Getter for the brevkode property.
	 *
	 * @return the brevkode
	 */
	public String getBrevkode() {
		return brevkode;
	}

	/**
	 * Setter for the brevkode property.
	 *
	 * @param brevkode the brevkode to set
	 */
	public void setBrevkode(String brevkode) {
		this.brevkode = brevkode;
	}

	/**
	 * Getter for the brevgruppe property.
	 *
	 * @return the brevgruppe
	 */
	public String getBrevgruppe() {
		return brevgruppe;
	}

	/**
	 * Setter for the brevgruppe property.
	 *
	 * @param brevgruppe the brevgruppe to set
	 */
	public void setBrevgruppe(String brevgruppe) {
		this.brevgruppe = brevgruppe;
	}

	/**
	 * Getter for the tittel property.
	 *
	 * @return the tittel
	 */
	public String getTittel() {
		return tittel;
	}

	/**
	 * Setter for the tittel property.
	 *
	 * @param tittel the tittel to set
	 */
	public void setTittel(String tittel) {
		this.tittel = tittel;
	}

	/**
	 * Getter for the redigerbart property.
	 *
	 * @return the redigerbart
	 */
	public Boolean getRedigerbart() {
		return redigerbart;
	}

	/**
	 * Setter for the redigerbart property.
	 *
	 * @param redigerbart the redigerbart to set
	 */
	public void setRedigerbart(Boolean redigerbart) {
		this.redigerbart = redigerbart;
	}

	/**
	 * Getter for the organInternt property.
	 *
	 * @return the organInternt
	 */
	public Boolean getOrganInternt() {
		return organInternt;
	}

	/**
	 * Setter for the organInternt property.
	 *
	 * @param organInternt the organInternt to set
	 */
	public void setOrganInternt(Boolean organInternt) {
		this.organInternt = organInternt;
	}

	/**
	 * Getter for the sensitivt property.
	 *
	 * @return the sensitivt
	 */
	public Boolean getSensitivt() {
		return sensitivt;
	}

	/**
	 * Setter for the sensitivt property.
	 *
	 * @param sensitivt the sensitivt to set
	 */
	public void setSensitivt(Boolean sensitivt) {
		this.sensitivt = sensitivt;
	}

	/**
	 * Getter for the dokumentKategori property.
	 *
	 * @return the dokumentKategori
	 */
	public DokumentKategoriCode getDokumentKategori() {
		return dokumentKategori;
	}

	/**
	 * Setter for the dokumentKategori property.
	 *
	 * @param dokumentKategori the dokumentKategori to set
	 */
	public void setDokumentKategori(DokumentKategoriCode dokumentKategori) {
		this.dokumentKategori = dokumentKategori;
	}

	/**
	 * Getter for the journalpostType property.
	 *
	 * @return the journalpostType
	 */
	public JournalpostTypeCode getJournalpostType() {
		return journalpostType;
	}

	/**
	 * Setter for the journalpostType property.
	 *
	 * @param journalpostType the journalpostType to set
	 */
	public void setJournalpostType(JournalpostTypeCode journalpostType) {
		this.journalpostType = journalpostType;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this).toString();
	}
	
}
