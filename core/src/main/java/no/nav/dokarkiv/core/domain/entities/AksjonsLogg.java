package no.nav.dokarkiv.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.GenerationType.SEQUENCE;
import static org.hibernate.annotations.CascadeType.DETACH;
import static org.hibernate.annotations.CascadeType.MERGE;
import static org.hibernate.annotations.CascadeType.PERSIST;
import static org.hibernate.annotations.CascadeType.REMOVE;

/**
 * Inneholder vesentlige endringer på Journalpost eller DokumentInfo.
 *
 * @see ArkivElementEndring
 */
@Entity
@Table(name = "T_AKSJONSLOGG")
@Builder
@Data
@Immutable
@NoArgsConstructor
@AllArgsConstructor
public class AksjonsLogg {

	private static final String AKSJONSLOGG_SEQUENCE = "aksjonslogg_seq";
	private static final String DATABASE_AKSJONSLOGG_SEQ = "t_aksjonslogg_seq";

	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = AKSJONSLOGG_SEQUENCE)
	@SequenceGenerator(name = AKSJONSLOGG_SEQUENCE, sequenceName = DATABASE_AKSJONSLOGG_SEQ, allocationSize = 1)
	@Column(name = "aksjonslogg_id", nullable = false, length = 11)
	private Long aksjonsloggId;

	@Column(name = "tidspunkt", nullable = false)
	private LocalDateTime tidspunkt;

	@Column(name = "applikasjon", length = 50, nullable = false)
	private String applikasjon;

	@Enumerated(EnumType.STRING)
	@Column(name = "aksjon", length = 50, nullable = false)
	private AksjonsTypeCode aksjon;

	@Column(name = "journalpost_id", length = 11)
	private Long journalpostId;

	@Column(name = "dokument_info_id", length = 11)
	private Long dokumentInfoId;

	@Column(name = "utfoert_av", length = 50, nullable = false)
	private String utfoertAv;

	@Column(name = "bruker", length = 50)
	private String bruker;

	@Column(name = "arkivsaksnummer", length = 10)
	private String arkivsaksnummer;

	@Enumerated(EnumType.STRING)
	@Column(name = "arkivsaksystem", length = 50)
	private FagsystemCode arkivsaksystem;

	@Column(name = "hjemmel", length = 50)
	private String hjemmel;

	@Column(name = "melding", length = 1000)
	private String melding;

	@OneToMany(mappedBy = "aksjonsLogg")
	@Cascade({PERSIST, MERGE, REMOVE, DETACH})
	@Builder.Default
	private final Set<ArkivElementEndring> arkivElementEndringer = new HashSet<>();
}
