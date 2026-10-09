package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.api.Sakstype;
import no.nav.dokarkiv.core.consumer.ereg.EregConsumer;
import no.nav.dokarkiv.core.consumer.ereg.EregResponse;
import no.nav.dokarkiv.core.consumer.pdl.IdentConsumer;
import no.nav.dokarkiv.core.consumer.pdl.PersonIkkeFunnetException;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import org.springframework.stereotype.Component;

import static no.nav.dokarkiv.core.api.Fagsaksystem.PP01;
import static no.nav.dokarkiv.core.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.core.api.Sakstype.GENERELL_SAK;
import static no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType.ORGNR;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/// Gjør alle eksterne oppslag (PDL/Ereg) for opprettelse av journalpost.
/// Skal kalles utenfor transaksjon slik at databaseforbindelser ikke holdes mens det ventes på web-kall.
@Component
public class OpprettJournalpostOppslagService {

	private final IdentConsumer identConsumer;
	private final EregConsumer eregConsumer;

	public OpprettJournalpostOppslagService(IdentConsumer identConsumer,
											EregConsumer eregConsumer) {
		this.identConsumer = identConsumer;
		this.eregConsumer = eregConsumer;
	}

	public OpprettJournalpostOppslag hentOppslag(OpprettJournalpostRequest request) {
		String aktoerIdForSak = skalIdentifisereEllerOppretteArkivsak(request) ? hentAktoerId(request.getBruker()) : null;
		String avsenderMottakerNavn = hentNavn(request.getAvsenderMottaker());
		IdentOppslag folkeregisterIdent = hentFolkeregisterIdentHvisBrukerAktoerId(request);

		return new OpprettJournalpostOppslag(aktoerIdForSak,
				avsenderMottakerNavn,
				folkeregisterIdent.ident(),
				folkeregisterIdent.brukerIkkeFunnet());
	}

	private static boolean erBrukerAktoerId(OpprettJournalpostRequest request) {
		return request.getBruker() != null && BrukerIdType.AKTOERID.equals(request.getBruker().getIdType());
	}

	static boolean skalIdentifisereEllerOppretteArkivsak(OpprettJournalpostRequest request) {
		if (request.getSak() == null) {
			return false;
		}
		Sakstype sakstype = request.getSak().getSakstype();
		return (FAGSAK.equals(sakstype) || GENERELL_SAK.equals(sakstype)) && !PP01.equals(request.getSak().getFagsaksystem());
	}

	private IdentOppslag hentFolkeregisterIdentHvisBrukerAktoerId(OpprettJournalpostRequest request) {
		if (erBrukerAktoerId(request)) {
			try {
				String brukerFolkeregisterIdent = identConsumer.hentFolkeregisterIdent(request.getBruker().getId());
				return new IdentOppslag(brukerFolkeregisterIdent, false);
			} catch (PersonIkkeFunnetException e) {
				// Hvis vi ikke har bruker så går vi videre
				return new IdentOppslag(null, true);
			}
		}
		return new IdentOppslag(null, false);
	}

	private String hentAktoerId(Bruker bruker) {
		return switch (bruker.getIdType()) {
			case AKTOERID -> bruker.getId();
			case FNR -> identConsumer.hentAktoerId(bruker.getId());
			default -> null;
		};
	}

	private String hentNavn(AvsenderMottaker avsenderMottaker) {
		if (avsenderMottaker == null || erAvsenderMottakerIdOgNavnNull(avsenderMottaker)) {
			return null;
		}

		if (isNotBlank(avsenderMottaker.getNavn())) {
			return avsenderMottaker.getNavn();
		} else if (isNotBlank(avsenderMottaker.getId())) {
			if (erAvsenderMottakerPerson(avsenderMottaker)) {
				return identConsumer.hentPersonnavn(avsenderMottaker.getId());
			} else if (erAvsenderMottakerOrganisasjon(avsenderMottaker)) {
				return hentOrganisasjonsnavn(avsenderMottaker);
			}
		}
		return null;
	}

	private String hentOrganisasjonsnavn(AvsenderMottaker avsenderMottaker) {
		EregResponse eregResponse = eregConsumer.hentOrganisasjonsnavn(avsenderMottaker.getId());

		if (eregResponse == null || eregResponse.navn() == null) {
			return null;
		}

		var navn = eregResponse.navn();

		return navn.erGyldig() ? navn.sammensattnavn() : null;
	}

	private static boolean erAvsenderMottakerPerson(AvsenderMottaker avsenderMottaker) {
		return avsenderMottaker.getIdType() == null || FNR == avsenderMottaker.getIdType();
	}

	private static boolean erAvsenderMottakerIdOgNavnNull(AvsenderMottaker avsenderMottaker) {
		return isBlank(avsenderMottaker.getNavn()) && isBlank(avsenderMottaker.getId());
	}

	private static boolean erAvsenderMottakerOrganisasjon(AvsenderMottaker avsenderMottaker) {
		return ORGNR == avsenderMottaker.getIdType();
	}
}
