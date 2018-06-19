package no.nav.dokarkiv.core.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Entity
@Table(name = "t_jp_tillegg")
public class Tilleggsopplysninger extends AbstractPersistentVersionedDomainObjectWithKilde {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = ***gammelt_fnr***94040373L;

	@Id
	@Column(name = "journalpost_id")
	private Long journalpost_id;

	@Column(name = "nokkel")
	private String nokkel;

	@Column(name = "verdi")
	private String verdi;

	/**
	 * Returns an id for an item/entity.
	 *
	 * @return The unique identification (amongst enitites of same type).
	 */
	@Override
	public Long getId() {
		return journalpost_id;
	}
}
