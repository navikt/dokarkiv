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
                        "select jp.journalpostId, jp.journalstatus, jp.journalposttype, jp.fagomrade, cs.createdDate, jp.journalDato, jp.mottakskanal,\n" +
                                "       jp.skjermingType, jp.avsenderMottakerId, br.brukerId, br.brukerType, sr.sakId, sr.fagsystem, sr.feilregistrert, \n" +
                                "       sa.aktoerId, sa.tema, sa.fagsakNr, sa.orgnr, sa.applikasjon, sa.opprettetAv, sa.opprettetTidspunkt, di.dokumentInfoId, \n" +
                                "       di.dokumentstatus, di.brevkode, di.kategori, di.organInternt, di.innskrenketPartsinnsyn, di.innskrenketPartsinnsynFraTredjepart, \n" +
                                "       di.kassert, jr.skjermingType, fd.variantFormat, fd.skjermingType, jp.innsyn\n" +
                                "from Journalpost jp\n" +
                                "         left join jp.brukere br\n" +
                                "         join jp.changeStamp cs\n" +
                                "         left join jp.saksrelasjon sr on sr.journalpost.journalpostId = :journalpostId\n" +
                                "         left join Sak sa on sr.sakId = sa.sakId\n" +
                                "         join jp.journalpostDokumentInfoRelasjoner jr\n" +
                                "         join jr.dokumentInfo di\n" +
                                "              on di.dokumentInfoId = :dokumentInfoId and jr.journalpost.journalpostId = :journalpostId\n" +
                                "         join FilDetaljer fd on fd.dokumentInfo.dokumentInfoId = :dokumentInfoId and fd.variantFormat = :variantFormat\n" +
                                "where jp.journalpostId = :journalpostId\n" +
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