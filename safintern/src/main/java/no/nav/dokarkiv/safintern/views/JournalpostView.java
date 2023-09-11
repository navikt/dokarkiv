package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Limit;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.SubqueryProvider;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import no.nav.dokarkiv.core.domain.codes.BehandlingstemaCti;
import no.nav.dokarkiv.core.domain.codes.Fagomrade;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;

import java.util.List;
import java.util.Map;

@JsonPropertyOrder({
		"journalpostId",
		"type",
		"fagomraade",
		"fagomraadenavn",
		"status",
		"saksrelasjon"
})
@EntityView(Journalpost.class)
public interface JournalpostView {
	@IdMapping
	Long getJournalpostId();

	@Mapping("fagomrade")
	FagomradeCode getFagomraade();

	@MappingSubquery(FagomraadenavnSubqueryProvider.class)
	String getFagomraadenavn();

	@Mapping("journalstatus")
	JournalStatusCode getStatus();

	@Mapping("journalposttype")
	JournalpostTypeCode getType();

	@Mapping("kanalReferanseId")
	String getKanalreferanseId();

	@Mapping("mottakskanal")
	MottaksKanalCode getMottakskanal();

	@Mapping("utsendingskanal")
	UtsendingsKanalCode getUtsendingskanal();

	@Mapping("behandlingstema")
	String getBehandlingstema();

	@MappingSubquery(BehandlingstemanavnSubqueryProvider.class)
	String getBehandlingstemanavn();

	@Mapping("innhold")
	String getInnhold();

	@Mapping("journalForendeEnhetId")
	String getJournalfoerendeEnhet();

	@Mapping("journalfortAvNavn")
	String getJournalfoertAvNavn();

	@Mapping("opprettetAvNavn")
	String getOpprettetAvNavn();

	@Mapping("antallRetur")
	Integer getAntallRetur();

	@Mapping("innsyn")
	InnsynCode getInnsyn();

	@Mapping("skjermingType")
	SkjermingTypeCode getSkjerming();

	@Mapping("this")
	RelevanteDatoerView getRelevanteDatoer();

	@Mapping("this")
	AvsenderMottakerView getAvsenderMottaker();

	@Mapping("saksrelasjon")
	SaksrelasjonView getSaksrelasjon();

	@Mapping("brukere")
	@Limit(limit = "1", order = {"brukerInfoId DESC"})
	BrukerView getBruker();

	@MappingCorrelatedSimple(
			correlationBasis = "journalpostId",
			correlated = UtsendingsInfo.class,
			correlationExpression = "journalpostId IN correlationKey",
			fetch = FetchStrategy.JOIN
	)
	UtsendingsInfoView getUtsendingsInfo();

	Map<String, String> getTilleggsopplysninger();

	@Mapping("journalpostDokumentInfoRelasjoner")
	List<DokumentinfoView> getDokumenter();

	class FagomraadenavnSubqueryProvider implements SubqueryProvider {

		@Override
		public <T> T createSubquery(SubqueryInitiator<T> subqueryBuilder) {
			return subqueryBuilder.from(Fagomrade.class, "f")
					.select("dekode")
					.where("f.kode").eqExpression("EMBEDDING_VIEW(fagomrade)")
					.end();
		}
	}

	class BehandlingstemanavnSubqueryProvider implements SubqueryProvider {

		@Override
		public <T> T createSubquery(SubqueryInitiator<T> subqueryBuilder) {
			return subqueryBuilder.from(BehandlingstemaCti.class, "bt")
					.select("decode")
					.where("bt.code").eqExpression("EMBEDDING_VIEW(behandlingstema)")
					.end();
		}
	}
}
