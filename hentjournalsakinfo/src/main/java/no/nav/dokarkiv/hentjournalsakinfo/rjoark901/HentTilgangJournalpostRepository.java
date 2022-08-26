package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import org.hibernate.query.Query;
import org.hibernate.transform.ResultTransformer;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Repository
class HentTilgangJournalpostRepository {

    private final EntityManager entityManager;

    @Inject
    public HentTilgangJournalpostRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    TilgangJournalpostDto hentTilgangJournalpost(Long journalpostId, Long dokumentInfoId, VariantFormatCode variantFormat) {

        List resultList = entityManager
                .createQuery(
                        """ 
                                select jp.journalpostId,
                                       jp.journalstatus,
                                       jp.journalposttype,
                                       jp.fagomrade,
                                       cs.createdDate,
                                       jp.journalDato,
                                       jp.mottakskanal,
                                       jp.skjermingType,
                                       jp.avsenderMottakerId,
                                       br.brukerId,
                                       br.brukerType,
                                       sr.sakId,
                                       sr.fagsystem,
                                       sr.feilregistrert,
                                       sa.aktoerId,
                                       sa.tema,
                                       sa.fagsakNr,
                                       sa.orgnr,
                                       sa.applikasjon,
                                       sa.opprettetAv,
                                       sa.opprettetTidspunkt,
                                       di.dokumentInfoId,
                                       di.dokumentstatus,
                                       di.brevkode,
                                       di.kategori,
                                       di.organInternt,
                                       di.innskrenketPartsinnsyn,
                                       di.innskrenketPartsinnsynFraTredjepart,
                                       di.kassert,
                                       jr.skjermingType,
                                       fd.variantFormat,
                                       fd.skjermingType,
                                       jp.innsyn
                                from Journalpost jp
                                         left join jp.brukere br
                                         join jp.changeStamp cs
                                         left join jp.saksrelasjon sr on sr.journalpost.journalpostId = :journalpostId
                                         left join Sak sa on sr.sakId = sa.sakId
                                         join jp.journalpostDokumentInfoRelasjoner jr
                                         join jr.dokumentInfo di
                                              on di.dokumentInfoId = :dokumentInfoId and jr.journalpost.journalpostId = :journalpostId
                                         join FilDetaljer fd on fd.dokumentInfo.dokumentInfoId = :dokumentInfoId and fd.variantFormat = :variantFormat
                                where jp.journalpostId = :journalpostId
                                order by br.brukerInfoId desc
                                """
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
                                return HentTilgangJournalpostDtoMapper.mapTupleTilgangJournalPost(tuple);
                            }

                            @Override
                            public List transformList(List collection) {
                                return collection;
                            }
                        }
                )
                .getResultList();

        if (!resultList.isEmpty()) {
            return (TilgangJournalpostDto) resultList.get(0);
        } else {
            return null;
        }
    }
}