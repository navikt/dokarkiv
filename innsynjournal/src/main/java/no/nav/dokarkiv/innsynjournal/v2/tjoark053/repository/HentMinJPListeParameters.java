package no.nav.dokarkiv.innsynjournal.v2.tjoark053.repository;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;

/**
 * Params used for searching and filtering for journalposts.
 * 
 * saksListe - Filtering journalpost based on saksId and Fagsystem
 * tidligstInnsynDato - Filtering out journalposts where changeStamp.createdDate and journalDato< than specified date
 * tillattInnsynStatus - Filtering on {@link JournalStatusCode}
 * visFeilRegistrert - Filtering on saksrelasjon.feilregistrert
 * skjulFagomraade - Filtering on Fagområde
 * journalFom - Filtering on Journalpost changeStamp.createdDate>= specified date
 * journalTom - Filtering on Journalpost changeStamp.createdDate<= specified date
 * journalpostTypeCode - Filtering on {@link JournalpostTypeCode}
 * eagerFetchDokInfo - eager fetch optimization on associated entities
 * Created by Hans Petter Simonsen - Visma Consulting
 */
public class HentMinJPListeParameters {
	private List<SakFagsystem> saksListe = Lists.newArrayList();
	private Date tidligstInnsynDato;
	private List<JournalStatusCode> tillattInnsynStatus = Lists.newArrayList();
	private boolean visFeilRegistrert = false;
	private List<FagomradeCode> skjulFagomraade = Lists.newArrayList();
	private List<FagomradeCode> fagomraade = Lists.newArrayList();
	private Date journalFom;
	private Date journalTom;
	private JournalpostTypeCode journalpostTypeCode;
	private boolean eagerFetchDokInfo = true;
	private long maxResults;
	private int pageNr;
	

	public List<SakFagsystem> getSaksListe() {
		return saksListe;
	}

	public void setSaksListe(List<SakFagsystem> saksListe) {
		this.saksListe = saksListe;
	}

	public void addFagsystemSak(String sakid, FagsystemCode fagsystem) {
		saksListe.add(new SakFagsystem(fagsystem, sakid));
	}

	public void addFagsystemSak(SakFagsystem fagsystemSak) {
		saksListe.add(fagsystemSak);
	}

	public Date getTidligstInnsynDato() {
		if (tidligstInnsynDato != null) {
			return new Date(tidligstInnsynDato.getTime());
		}
		return null;
	}

	public void setTidligstInnsynDato(Date tidligstInnsynDato) {
		this.tidligstInnsynDato = tidligstInnsynDato;
	}

	public List<JournalStatusCode> getTillattInnsynStatus() {
		return tillattInnsynStatus;
	}

	public void setTillattInnsynStatus(List<JournalStatusCode> tillattInnsynStatus) {
		this.tillattInnsynStatus = tillattInnsynStatus;
	}

	public boolean isVisFeilRegistrert() {
		return visFeilRegistrert;
	}

	public void setVisFeilRegistrert(boolean visFeilRegistrert) {
		this.visFeilRegistrert = visFeilRegistrert;
	}

	public List<FagomradeCode> getSkjulFagomraade() {
		return skjulFagomraade;
	}

	public void setSkjulFagomraade(List<FagomradeCode> skjulFagomraade) {
		this.skjulFagomraade = skjulFagomraade;
	}	
	
	public List<FagomradeCode> getFagomraade() {
		return fagomraade;
	}

	public void setFagomraade(List<FagomradeCode> fagomraade) {
		this.fagomraade = fagomraade;
	}

	public Date getJournalFom() {
		if (journalFom != null) {
			return new Date(journalFom.getTime());
		}
		return null;
	}

	public void setJournalFom(Date journalFom) {
		if (journalFom != null) {
			this.journalFom = new Date(journalFom.getTime());
		} else {
			this.journalFom = null;
		}
	}

	public Date getJournalTom() {
		if (journalTom != null) {
			return new Date(journalTom.getTime());
		}
		return null;
	}

	public void setJournalTom(Date journalTom) {
		if (journalTom != null) {
			this.journalTom = new Date(journalTom.getTime());
		} else {
			this.journalTom = null;
		}
	}

	public JournalpostTypeCode getJournalpostTypeCode() {
		return journalpostTypeCode;
	}

	public void setJournalpostTypeCode(JournalpostTypeCode journalpostTypeCode) {
		this.journalpostTypeCode = journalpostTypeCode;
	}	
		
	public long getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(long maxResults) {
		this.maxResults = maxResults;
	}

	public int getPageNr() {
		return pageNr;
	}

	public void setPageNr(int pageNr) {
		this.pageNr = pageNr;
	}
	
	public boolean isEagerFetchDokInfo() {
		return eagerFetchDokInfo;
	}

	public void setEagerFetchDokInfo(boolean eagerFetchDokInfo) {
		this.eagerFetchDokInfo = eagerFetchDokInfo;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
		.append("saksListe", saksListe)
		.append("tidligstInnsynDato", tidligstInnsynDato)
		.append("tillattInnsynStatus", tillattInnsynStatus)
		.append("visFeilRegistrert", visFeilRegistrert)
		.append("skjulFagomraade", skjulFagomraade)
		.append("fagomraade", fagomraade)
		.append("journalFom", journalFom)
		.append("journalTom", journalTom)
		.append("journalpostTypeCode", journalpostTypeCode)
		.append("maxResults", maxResults)
		.append("pageNr", pageNr)
		.toString();
	}
	
	@Override
    public int hashCode() { 
		return Objects.hashCode(journalFom, journalTom, journalpostTypeCode, saksListe, skjulFagomraade, fagomraade, tidligstInnsynDato,
				tillattInnsynStatus, visFeilRegistrert, maxResults, pageNr);
    } 

	@Override
	public boolean equals(Object obj) { //NOSONAR
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HentMinJPListeParameters other = (HentMinJPListeParameters) obj;
		return Objects.equal(this.saksListe, other.saksListe) &&
				Objects.equal(this.tidligstInnsynDato, other.tidligstInnsynDato) &&
				Objects.equal(this.tillattInnsynStatus, other.tillattInnsynStatus) &&
				Objects.equal(this.visFeilRegistrert, other.visFeilRegistrert) &&
				Objects.equal(this.skjulFagomraade, other.skjulFagomraade) &&
				Objects.equal(this.journalTom, other.journalTom) &&
				Objects.equal(this.journalFom, other.journalFom) &&
				Objects.equal(this.fagomraade, other.fagomraade) &&
				Objects.equal(this.maxResults, other.maxResults) &&
				Objects.equal(this.pageNr, other.pageNr) &&
				Objects.equal(this.journalpostTypeCode, other.journalpostTypeCode);  //NOSONAR
	}

	
}
