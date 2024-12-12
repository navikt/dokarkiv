package no.nav.dokarkiv.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Denne klassen brukes for å kunne adressere kolonnene i JournalpostDokumentInfoRelasjon uten å si til Hibernate at
 * den skal fetche DokumentInfo eller Journalpost entitetene (og dermed gjøre joins mot disse i tilfeller det ikke er nødvendig).
 */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Embeddable
public class JournalpostDokumentInfoRelasjonId {
	@Column(name = "journalpost_id", nullable = false, insertable = false, updatable = false)
	private Long journalpostId;
	@Column(name = "dokument_info_id", nullable = false, insertable = false, updatable = false)
	private Long dokumentInfoId;
}
