package no.nav.dokarkiv.hentjournalinfo.objects;

import lombok.Builder;
import lombok.Data;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
public class GraphQlMap {
    private String key;
    private String value;
}
