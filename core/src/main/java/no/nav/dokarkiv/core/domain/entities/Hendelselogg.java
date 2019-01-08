package no.nav.dokarkiv.core.domain.entities;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Entity
@Table(name = "T_HENDELSELOGG")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Hendelselogg extends AbstractPersistentVersionedDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hendelselogg_seq")
	@GenericGenerator(name = "hendelselogg_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@Parameter(name = "sequence_name", value = "t_hendelselogg_seq"),
					@Parameter(name = "initial_value", value = "200000000")})
	@Column(name = "hendelselogg_id", nullable = false)
	private Long hendelseloggId;

	@Column(name = "journalpost_id")
	private Long journalpostId;

	@Column(name = "dokument_info_id")
	private Long dokumentInfoId;

	@Column(name = "hendelse_type", length = 50, nullable = false)
	private String hendelseType;

	@Column(name = "bruker", length = 20, nullable = false)
	private String bruker;

	@Column(name = "opprettet_av_tjeneste", length = 20, nullable = false)
	private String opprettetAvTjeneste;

	@Column(name = "annen_info", length = 4000)
	private String annenInfo;

	@JsonSetter("annenInfo")
	public void setAnnenInfo(JsonNode jsonNode) {
		this.annenInfo = jsonNode.toString();
	}

	public void setAnnenInfo(String annenInfo) {
		this.annenInfo = annenInfo;
	}
}
