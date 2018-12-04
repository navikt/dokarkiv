package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import static java.lang.String.format;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import org.springframework.util.Assert;

/**
 * Request object for TJOARK203
 *
 * @author Paul Magne Lunde, Visma Consulting
 */
@Data
@Builder
public class JournalforInngaaendeForsendelseV2RequestTo {
	private static final String ERROR_DESC = "Missing required field in request: %s";
	private static final String ERROR_DESC_COLLECTION = "Missing or empty list of required field in request: ";

	private boolean forsokEndeligJf;
	private Journalpost journalpost;

	public JournalforInngaaendeForsendelseV2RequestTo(boolean forsokEndeligJf, Journalpost journalpost) {
		this.journalpost = journalpost;
		this.forsokEndeligJf = forsokEndeligJf;
	}

	public void validate() {
		Assert.notNull(forsokEndeligJf, format(ERROR_DESC, "ForsokEndeligJF"));
		Assert.notNull(journalpost, format(ERROR_DESC, "Journalpost"));
		Assert.notNull(journalpost.getOpprettetAvNavn(), format(ERROR_DESC, "OpprettetAvNavn"));
		Assert.notNull(journalpost.getMottattDato(), format(ERROR_DESC, "MottattDato"));
		Assert.notNull(journalpost.getMottakskanal(), format(ERROR_DESC, "Mottakskanal"));
		Assert.hasText(journalpost.getKanalReferanseId(), format(ERROR_DESC, "KanalReferanseId"));
		validateBrukere();
		validateSaksrelasjon();
		validateJournalpostDokumentInfoRelasjoner();
	}

	private void validateSaksrelasjon() {
		if (journalpost.getSaksrelasjon() != null) {
			Assert.notNull(journalpost.getSaksrelasjon().getSakId(), format(ERROR_DESC, "Saksrelasjon.SaksNummer"));
			Assert.notNull(journalpost.getSaksrelasjon().getFagsystem(), format(ERROR_DESC, "Saksrelasjon.Fagsystem"));
		}
	}

	private void validateBrukere() {
		if (journalpost.getBrukere() != null) {
			for (Bruker bruker : journalpost.getBrukere()) {
				Assert.notNull(bruker, ERROR_DESC + "Bruker");
				Assert.notNull(bruker.getBrukerId(), format(ERROR_DESC, "Bruker.BrukerId"));
				Assert.notNull(bruker.getBrukerType(), format(ERROR_DESC, "Bruker.BrukerType"));
			}
		}
	}

	private void validateJournalpostDokumentInfoRelasjoner() {
		Assert.notEmpty(this.journalpost.getJournalpostDokumentInfoRelasjoner(), ERROR_DESC_COLLECTION + "JournalpostDokumentInfoRelasjoner");
		for (JournalpostDokumentInfoRelasjon relasjon : this.journalpost.getJournalpostDokumentInfoRelasjoner()) {
			Assert.notNull(relasjon, ERROR_DESC_COLLECTION + "JournalpostDokumentInfoRelasjon");
			Assert.notNull(relasjon.getTilknyttetJournalpostSom(), format(ERROR_DESC, "JournalpostDokumentInfoRelasjoner.TilknyttetJournalpostSom"));
			validateDokumentInfo(relasjon);
			validateFildetaljer(relasjon);
			validateSkannetInnhold(relasjon);
		}
	}

	private void validateDokumentInfo(JournalpostDokumentInfoRelasjon relasjon) {
		Assert.notNull(relasjon.getDokumentInfo(), ERROR_DESC + "JournalpostDokumentInfoRelasjoner.DokumentInfo");
		Assert.notNull(relasjon.getDokumentInfo()
				.getKategori(), ERROR_DESC + "JournalpostDokumentInfoRelasjoner.DokumentInfo.Kategori");
		Assert.notNull(relasjon.getDokumentInfo()
				.getDokumenttypeId(), ERROR_DESC + "JournalpostDokumentInfoRelasjoner.DokumentInfo.DokumenttypeId");
	}

	private void validateFildetaljer(JournalpostDokumentInfoRelasjon relasjon) {
		Assert.notEmpty(relasjon.getDokumentInfo()
				.getFildetaljerListe(), ERROR_DESC_COLLECTION + "JournalpostDokumentInfoRelasjoner.DokumentInfo.FildetaljerListe");
		for (FilDetaljer filDetaljer : relasjon.getDokumentInfo().getFildetaljerListe()) {
			Assert.notNull(filDetaljer, ERROR_DESC + "JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer");
			Assert.notNull(filDetaljer.getFiltype(), ERROR_DESC + "JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer.Filtype");
			Assert.notNull(filDetaljer.getVariantFormat(), ERROR_DESC + "JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer.VariantFormat");
			Assert.notNull(filDetaljer.getFileContent(), ERROR_DESC + "JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer.Dokument");
		}
	}

	private void validateSkannetInnhold(JournalpostDokumentInfoRelasjon relasjon) {
		if (relasjon.getDokumentInfo().getSkannetInnholdListe() != null) {
			for (SkannetInnhold skannetInnhold : relasjon.getDokumentInfo().getSkannetInnholdListe()) {
				Assert.notNull(skannetInnhold, ERROR_DESC + "JournalpostDokumentInfoRelasjoner.DokumentInfo.SkannetInnhold");
				Assert.notNull(skannetInnhold.getVedleggInnhold(), ERROR_DESC + "JournalpostDokumentInfoRelasjoner.DokumentInfo.SkannetInnhold.VedleggInnhold");
				Assert.notNull(skannetInnhold.getDokumenttypeid(), ERROR_DESC + "JournalpostDokumentInfoRelasjoner.DokumentInfo.SkannetInnhold.DokumenttypeId");
			}
		}
	}

	public Journalpost getJournalpost() {
		return journalpost;
	}

	public void setJournalpost(Journalpost journalpost) {
		this.journalpost = journalpost;
	}

	public boolean isForsokEndeligJf() {
		return forsokEndeligJf;
	}

	public void setForsokEndeligJf(boolean forsokEndeligJf) {
		this.forsokEndeligJf = forsokEndeligJf;
	}
}
