package no.nav.dokarkiv.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObject;

import java.util.Date;

import static jakarta.persistence.GenerationType.SEQUENCE;

/**
 * Inneholder metadata om gyldige urler for å hente dokumenter.
 * Fjernes etter at det er avklart at vi kan slette denne tabellen
 * <p>
 *
 * @deprecated Brukes av {@link no.nav.dokarkiv.core.repository.JoarkDeleteRepository} for permanent sletting av journalposter fra joarkadmin
 */
@Entity
@Table(name = "T_DOK_URL_INFO")
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
public class DokumentUrlInfo extends AbstractPersistentVersionedDomainObject {
	private static final String DOKUMENT_URL_INFO_SEQUENCE = "dokument_url_info_seq";
	private static final String DATABASE_DOKUMENT_URL_INFO_SEQUENCE = "T_DOK_URL_INFO_SEQ";

	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = DOKUMENT_URL_INFO_SEQUENCE)
	@SequenceGenerator(name = DOKUMENT_URL_INFO_SEQUENCE, sequenceName = DATABASE_DOKUMENT_URL_INFO_SEQUENCE, initialValue = 200000000, allocationSize = 1)
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
