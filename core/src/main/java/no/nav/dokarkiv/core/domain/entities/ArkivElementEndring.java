package no.nav.dokarkiv.core.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;



/**
 * @author Ugur Alpay Cenar, Visma Consulting.
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
	@Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DETACH})
	private AksjonsLogg aksjonsLogg;

	public String toString() {
		return String.format("ArkivElementEndring(arkivElementEndringId=%s, arkivElement=%s)", arkivElementEndringId, arkivElement);
	}
}
