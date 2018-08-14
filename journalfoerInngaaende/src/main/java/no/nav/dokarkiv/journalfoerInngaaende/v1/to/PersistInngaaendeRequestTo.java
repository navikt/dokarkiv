package no.nav.dokarkiv.journalfoerInngaaende.v1.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Paul Magne Lunde, Visma Consulting
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersistInngaaendeRequestTo {
    private String journalpostId;
    private boolean forsoekEndeligJf;
    private String journafEnhet;
    private AktoerTo avsender;
    private AktoerTo bruker;
    private ArkivsakTo arkivsak;
    private String tema;
    private String journalpostTittel;
}
