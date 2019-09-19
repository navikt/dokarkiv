package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
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

		List resultList = entityManager
				.createQuery(
						"select jp.journalpostId, " +
								"jp.journalstatus, " +
								"jp.journalposttype, " +
								"jp.fagomrade, " +
								"cs.createdDate, " +
								"jp.mottakskanal, " +
								"jp.skjermingType, " +
								"jp.avsenderMottakerId, " +
								"br.brukerId, " +
								"br.brukerType, " +
								"sr.sakId, " +
								"sr.fagsystem, " +
								"sa.aktoerId, " +
								"sa.tema, " +
								"sa.fagsakNr, " +
								"sa.orgnr, " +
								"sa.applikasjon, " +
								"sa.opprettetAv, " +
								"sa.opprettetTidspunkt, " +
								"di.dokumentInfoId, " +
								"di.dokumentstatus, " +
								"di.brevkode, " +
								"jr.skjermingType, " +
								"fd.variantFormat, " +
								"fd.skjermingType " +
								"from Journalpost jp " +
								"left join jp.brukere br " +
								"join jp.changeStamp cs " +
								"left join jp.saksrelasjon sr on sr.journalpost.journalpostId = :journalpostId " +
								"left join Sak sa on sr.sakId = sa.sakId " +
								"join jp.journalpostDokumentInfoRelasjoner jr " +
								"join jr.dokumentInfo di on di.dokumentInfoId = :dokumentInfoId and jr.journalpost.journalpostId = :journalpostId " +
								"join FilDetaljer fd on fd.dokumentInfo.dokumentInfoId = :dokumentInfoId and fd.variantFormat = :variantFormat " +
								"where jp.journalpostId = :journalpostId " +
								"order by br.brukerInfoId desc"
				)
				.setParameter("journalpostId", journalpostId)
				.setParameter("dokumentInfoId", dokumentInfoId)
				.setParameter("variantFormat", variantFormat)
				.setMaxResults(1)
				.unwrap(Query.class)
				.setResultTransformer(
						new ResultTransformer() {
							@Override
							public Object transformTuple(
									Object[] tuple,
									String[] aliases) {
								return new TilgangJournalpostDto(
										isNull(tuple[0]) ? null : ((Long) tuple[0]).toString(),
										(JournalStatusCode) tuple[1],
										(JournalpostTypeCode) tuple[2],
										(FagomradeCode) tuple[3],
										isNull(tuple[4]) ? null : ((Timestamp) tuple[4]).toLocalDateTime(),
										(MottaksKanalCode) tuple[5],
										(SkjermingTypeCode) tuple[6],
										(String) tuple[7],
										new TilgangBrukerDto((String) tuple[8],
												(BrukerTypeCode) tuple[9]),
										new TilgangSakDto((String) tuple[10],
												(FagsystemCode) tuple[11],
												(String) tuple[12],
												(String) tuple[13],
												(String) tuple[14],
												(String) tuple[15],
												(String) tuple[16],
												(String) tuple[17],
												(String) tuple[18]),
										new TilgangDokumentInfoDto(isNull(tuple[19]) ? null : ((Long) tuple[19]).toString(),
												isNull(tuple[20]) ? null : (DokumentStatusCode) tuple[20],
												(String) tuple[21],
												(SkjermingTypeCode) tuple[22],
												new TilgangVariantDto((VariantFormatCode) tuple[23],
														(SkjermingTypeCode) tuple[24])
										));
							}

							@Override
							public List transformList(List collection) {
								return collection;
							}
						}
				)
				.getResultList();
		return (TilgangJournalpostDto) resultList.get(0);
	}

	private boolean isNull(Object o) {
		return o == null;
	}
}