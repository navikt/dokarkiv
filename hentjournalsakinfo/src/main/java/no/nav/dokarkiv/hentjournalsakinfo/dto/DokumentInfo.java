package no.nav.dokarkiv.hentjournalsakinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;

import java.util.Date;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
@AllArgsConstructor
public class DokumentInfo {

	private final Long dokumentInfoId;
	private final String brevkode;
	private final String brevgruppe;
	private final String konvertertFraSystem;
	private final Boolean sensitivt;
	private final Boolean slettet;
	private final String endretAvNavn;
	private final DokumentKategoriCode kategori;
	private final DokumentStatusCode dokumentstatus;
	private final Date dokumentFerdigDato;
	private final String tittel;
	private final String konfidensialitet;
	private final String integritet;
	private final String tilgjengelighet;
	private final Boolean innskrenketPartsinnsyn;
	private final Boolean innskrenketPartsinnsynFraTredjepart;
	private final Boolean organInternt;
	private final Long originalJournalpostId;
	private final String dokumenttypeId;
//    private final Set<SkannetInnhold> skannetInnholdListe = new HashSet<>(); TODO Trenger vi denne?
//    private final Set<JournalpostDokumentInfoRelasjon> journalpostRelasjoner = new HashSet<>(); TODO Trenger vi denne?
//    private final Set<Fildetaljer> fildetaljerListe = new HashSet<>(); TODO Trenger vi denne?

}
