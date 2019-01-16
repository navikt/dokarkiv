package no.nav.dokarkiv.core.domain.entities;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObject;
import no.nav.dokarkiv.core.domain.codes.AksjonTypeCode;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Entity
@Table(name = "T_AKSJONSLOGG")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AksjonsLogg extends AbstractPersistentVersionedDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hendelselogg_seq")
	@GenericGenerator(name = "hendelselogg_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@Parameter(name = "sequence_name", value = "t_aksjonslogg_seq"),
					@Parameter(name = "initial_value", value = "200000000")})
	@Column(name = "aksjonslogg_id", nullable = false, length = 11)
	private Long aksjonsloggId;

	@Column(name = "journalpost_id", nullable = false, length = 11)
	private Long journalpostId;

	@Column(name = "dokument_info_id", length = 11)
	private Long dokumentInfoId;

	@Column(name = "applikasjon", length = 50, nullable = false)
	private String applikasjon;

	@Enumerated(EnumType.STRING)
	@Column(name = "aksjon", length = 50, nullable = false)
	private AksjonTypeCode aksjon;

	@Column(name = "hjemmel", length = 50)
	private String hjemmel;

	@Column(name = "bruker", length = 50)
	private String bruker;

	@Column(name = "arkiv_element", length = 50)
	private String arkivElement;

	@Column(name = "fra_verdi", length = 50)
	private String fraVerdi;

	@Column(name = "til_verdi", length = 50)
	private String tilVerdi;

	@Column(name = "utfoert_av", length = 50)
	private String utfoertAv;

	@Column(name = "melding", length = 4000)
	private String melding;
}
