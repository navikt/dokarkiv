package no.nav.dokarkiv.core.domain.codes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "T_K_INNSYN")
public class Innsyn {

	@Id
	@Column(name = "k_innsyn")
	private String kode;

	@Column(name = "beskrivelse", length = 200, nullable = false)
	private String beskrivelse;
}
