package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import org.hibernate.query.Query;
import org.hibernate.transform.ResultTransformer;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Repository
public class HentTilgangJournalpostRepository {

	private final EntityManager entityManager;

	@Inject
	public HentTilgangJournalpostRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	TilgangJournalpostDto hentTilgangJournalpost(Long journalpostId, Long dokumentInfoId, VariantFormatCode variantFormat) {

		return (TilgangJournalpostDto) entityManager
				.createQuery(
						"select jp.journalpostId, " +
								"jp.journalstatus, " +
								"jp.journalposttype, " +
								"jp.fagomrade, " +
								"cs.createdDate, " +
								"jp.mottakskanal, " +
								"jp.avsenderMottakerId, " +
								"br.brukerId, " +
								"br.brukerType, " +
								"sr.sakId, " +
								"sr.fagsystem, " +
								"di.dokumentInfoId, " +
								"di.dokumentstatus, " +
								"di.brevkode, " +
								"fd.variantFormat " +
								"from Journalpost jp " +
								"left join jp.brukere br " +
								"join jp.changeStamp cs " +
								"left join jp.saksrelasjon sr on sr.journalpost.journalpostId = :journalpostId " +
								"join jp.journalpostDokumentInfoRelasjoner jr " +
								"join jr.dokumentInfo di on di.dokumentInfoId = :dokumentInfoId and jr.journalpost.journalpostId = :journalpostId " +
								"join FilDetaljer fd on fd.dokumentInfo.dokumentInfoId = :dokumentInfoId and fd.variantFormat = :variantFormat " +
								"where jp.journalpostId = :journalpostId"
				)
				.setParameter("journalpostId", journalpostId)
				.setParameter("dokumentInfoId", dokumentInfoId)
				.setParameter("variantFormat", variantFormat)
				.unwrap(Query.class)
				.setResultTransformer(
						new ResultTransformer() {
							@Override
							public Object transformTuple(
									Object[] tuple,
									String[] aliases) {
								return new TilgangJournalpostDto(
										isNull(tuple[0]) ? null : ((Long) tuple[0]).toString(),
										isNull(tuple[1]) ? null : ((JournalStatusCode) tuple[1]).name(),
										isNull(tuple[2]) ? null : ((JournalpostTypeCode) tuple[2]).name(),
										isNull(tuple[3]) ? null : ((FagomradeCode) tuple[3]).name(),
										isNull(tuple[4]) ? null : ((Timestamp) tuple[4]).toLocalDateTime(),
										isNull(tuple[5]) ? null : ((MottaksKanalCode) tuple[5]).name(),
										(String) tuple[6],
										new TilgangBrukerDto((String) tuple[7], isNull(tuple[8]) ? null : ((BrukerTypeCode) tuple[8]).name()),
										new TilgangSakDto((String) tuple[9], isNull(tuple[10]) ? null : ((FagsystemCode) tuple[10]).name()),
										new TilgangDokumentInfoDto(isNull(tuple[11]) ? null : ((Long) tuple[11]).toString(), isNull(tuple[12]) ? null : ((DokumentStatusCode) tuple[12])
												.name(), (String) tuple[13], isNull(tuple[14]) ? null : ((VariantFormatCode) tuple[15]).name()));
							}

							@Override
							public List transformList(List collection) {
								return collection;
							}
						}
				)
				.getSingleResult();
	}

	private boolean isNull(Object o) {
		return o == null;
	}
}