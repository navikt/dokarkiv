package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Inneholder metadata om gyldige urler for å hente dokumenter.
 *
 * Brukes av {@link no.nav.dokarkiv.core.repository.JoarkDeleteRepository} for permanent sletting av journalposter fra joarkadmin
 */
@Entity
@Table(name = "T_DOK_URL_INFO")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
public class DokumentUrlInfo extends AbstractPersistentVersionedDomainObject {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dokumentUrlInfo_seq")
	@GenericGenerator(name = "dokumentUrlInfo_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@Parameter(name = "sequence_name", value = "T_DOK_URL_INFO_SEQ"),
					@Parameter(name = "initial_value", value = "200000000")})
	@Column(name = "dok_url_info_id", nullable = false)
	private Long dokumentUrlInfoId;

	@ToString.Exclude
	@Column(name = "doctoken", nullable = false, length = 36)
	private String doctoken;

	@Column(name = "tidspunkt", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date tidspunkt;

	@Column(name = "fil_uuid", nullable = false, length = 36)
	private String filUuid;

	@Column(name = "ttl_minutes")
	private Long timeToLiveMinutes;

	@ToString.Exclude
	@ManyToOne
	@JoinColumn(name = "journalpost_id", nullable = false)
	private Journalpost journalpost;

	/**
	 * Default constructor.
	 */
	public DokumentUrlInfo() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 *
	 * @param dokumentUrlInfoId DB-id for the instance.
	 * @param version           DB-version for the instance.
	 */
	public DokumentUrlInfo(Long dokumentUrlInfoId, long version) {
		this.dokumentUrlInfoId = dokumentUrlInfoId;
		setVersion(version);
	}
}
