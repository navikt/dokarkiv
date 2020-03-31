package no.nav.dokarkiv.core.domain.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Joakim Bjørnstad, Jbit AS
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
