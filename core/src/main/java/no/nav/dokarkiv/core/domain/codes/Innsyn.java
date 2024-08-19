package no.nav.dokarkiv.core.domain.codes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
