package no.nav.dokarkiv.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;

import java.time.LocalDateTime;

import static org.hibernate.annotations.CascadeType.DETACH;
import static org.hibernate.annotations.CascadeType.MERGE;
import static org.hibernate.annotations.CascadeType.PERSIST;
import static org.hibernate.annotations.CascadeType.REMOVE;


/**
 * Sporer endringene på metadata felt knyttet til Journalpost eller DokumentInfo.
 *
 * @see AksjonsLogg
 */
@Entity
@Table(name = "t_arkiv_element_endring")
@Builder
@Getter
@Setter
@Immutable
@NoArgsConstructor
@AllArgsConstructor

public class ArkivElementEndring {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "arkivelementendring_seq")
	@GenericGenerator(name = "arkivelementendring_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@Parameter(name = "sequence_name", value = "t_arkivelementendring_seq")})
	@Column(name = "arkiv_element_endring_id", nullable = false, length = 11)
	private Long arkivElementEndringId;

	@Column(name = "tidspunkt", nullable = false)
	private LocalDateTime tidspunkt;

	@Column(name = "arkiv_element", length = 500, nullable = false)
	private String arkivElement;

	@Column(name = "fra_verdi", length = 500)
	private String fraVerdi;

	@Column(name = "til_verdi", length = 500)
	private String tilVerdi;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "aksjonslogg_id", nullable = false)
	@Cascade({PERSIST, MERGE, REMOVE, DETACH})
	private AksjonsLogg aksjonsLogg;

	/**
	 * Brukes i test
	 */
	public String toStringElementFraTil() {
		// Håndterer tilVerdi som er en timestamp, i tilfelle disse er litt forskjellig under test.
		if (tilVerdi != null && tilVerdi.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
			return String.format("ArkivElementEndring(arkivElement=%s, fraVerdi=%s, tilVerdi=%s)", arkivElement, fraVerdi, tilVerdi.split("\\.")[0]);
		} else {
			return String.format("ArkivElementEndring(arkivElement=%s, fraVerdi=%s, tilVerdi=%s)", arkivElement, fraVerdi, tilVerdi);
		}
	}
}
