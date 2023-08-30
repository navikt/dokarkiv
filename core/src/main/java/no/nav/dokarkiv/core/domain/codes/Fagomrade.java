package no.nav.dokarkiv.core.domain.codes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Data
@Builder
@AllArgsConstructor
@Table(name = "T_K_FAGOMRADE")
public class Fagomrade {

	@Id
	@Column(name = "k_fagomrade")
	private String kode;

	@Column(name = "dekode", length = 200, nullable = false)
	private String dekode;

	@Column(name = "er_gyldig", length = 1, nullable = false)
	@Type(type = "org.hibernate.type.TrueFalseType")
	private Boolean erGyldig;

	@Column(name = "dato_tom", columnDefinition = "DATE")
	private LocalDate datoTilOgMed;

	public Fagomrade() {
	}
}
