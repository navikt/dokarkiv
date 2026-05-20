package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;

/**
 * Builder for JournalpostDokumentInfoRelasjon.
 */
@Deprecated // bruk lombok builder istedet
public class JournalpostDokumentInfoRelasjonBuilder extends Builder<JournalpostDokumentInfoRelasjon> {

	private JournalpostDokumentInfoRelasjonBuilder(){
	}

	public static JournalpostDokumentInfoRelasjonBuilder getJournalpostDokumentInfoRelasjonBuilder(){
		return new JournalpostDokumentInfoRelasjonBuilder();
	}

	private Long id;
	private DokumentInfo dokumentInfo;
	private TilknyttetJournalpostSomCode tilknyttetJournalpostSom;
	private String tilknyttetAvNavn;

	public JournalpostDokumentInfoRelasjonBuilder journalpostDokumentInfoRelasjonId(Long value){
			this.id = value; return this; }
	public JournalpostDokumentInfoRelasjonBuilder dokumentInfo(DokumentInfo value){ this.dokumentInfo = value; return this; }
	public JournalpostDokumentInfoRelasjonBuilder tilknyttetJournalpostSom(TilknyttetJournalpostSomCode value){
			this.tilknyttetJournalpostSom = value; return this; }
	public JournalpostDokumentInfoRelasjonBuilder tilknyttetAvNavn(String value){ this.tilknyttetAvNavn = value; return this; }

	@Override
	public JournalpostDokumentInfoRelasjon build() {
		JournalpostDokumentInfoRelasjon jpDokInfoRelasjon = new JournalpostDokumentInfoRelasjon(id, 1);
		jpDokInfoRelasjon.setDokumentInfo(dokumentInfo);
		jpDokInfoRelasjon.setTilknyttetJournalpostSom(tilknyttetJournalpostSom);
		jpDokInfoRelasjon.setTilknyttetAvNavn(tilknyttetAvNavn);
		return jpDokInfoRelasjon;
	}

}
