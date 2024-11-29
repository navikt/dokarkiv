package no.nav.dokarkiv.core.domain.codes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
	private Boolean erGyldig;

	@Column(name = "dato_tom", columnDefinition = "DATE")
	private LocalDate datoTilOgMed;

	public Fagomrade() {
	}
}
