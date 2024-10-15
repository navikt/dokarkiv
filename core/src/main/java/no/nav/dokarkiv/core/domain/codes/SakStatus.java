package no.nav.dokarkiv.core.domain.codes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "T_K_SAK_STATUS")
public class SakStatus {

	@Id
	@Column(name = "k_sak_status")
	private String kode;

	@Column(name = "dekode", length = 200, nullable = false)
	private String dekode;

	@Column(name = "er_gyldig", length = 1, nullable = false)
	@Type(type = "org.hibernate.type.TrueFalseType")
	private Boolean erGyldig;

	@Column(name = "dato_opprettet", columnDefinition = "DATE", nullable = false)
	private LocalDate datoOpprettet;

	@Column(name = "dato_fom", columnDefinition = "DATE", nullable = false)
	private LocalDate datoFraOgMed;

	@Column(name = "opprettet_av", length = 200, nullable = false)
	private String opprettetAv;

}
