package no.nav.dokarkiv.core.consumer.aktoer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.util.DateConverterUtil;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.IdentDetaljer;
import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;

/**
 * Unit test class for {@link HentAktoerIdForIdentResponseMapper}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class HentAktoerIdForIdentResponseMapperTest {

	private static final String AKTOER_ID = "aktoerId";

	private HentAktoerIdForIdentResponseMapper mapper = new HentAktoerIdForIdentResponseMapper();

	@Test
	public void shouldMapAktoer() throws Exception {
		HentAktoerIdForIdentResponseTo responseTo = mapper.map(createHentIdentForAktoerIdResponse());

		assertThat(responseTo.getAktoerId(), is(AKTOER_ID));
	}

	@Test
	public void shouldMapEmptyHistoriskeIdenterList() throws Exception {
		HentAktoerIdForIdentResponseTo responseTo = mapper.map(createHentIdentForAktoerIdResponse());

		assertThat(responseTo.getHistoriskeIdenter().size(), is(0));
	}

	@Test
	public void shouldMapHistoriskeIdenterListWithOneElement() throws Exception {
		String historiskIdent = "historiskIdent";
		Date fom = Date.from(LocalDate.of(2014, Month.JANUARY, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
		HentAktoerIdForIdentResponse response = createHentIdentForAktoerIdResponse();
		response.getIdentHistorikk().add(createWsIdentdetaljer(historiskIdent, fom));

		HentAktoerIdForIdentResponseTo responseTo = mapper.map(response);

		assertThat(responseTo.getHistoriskeIdenter().size(), is(1));
		assertThat(responseTo.getHistoriskeIdenter().get(0).getFnr(), is(historiskIdent));
		assertThat(responseTo.getHistoriskeIdenter().get(0).getDatoFom(), is(fom));
	}

	@Test
	public void shouldMapHistoriskeIdenterListWithTwoElements() throws Exception {
		IdentDetaljer historiskIdent1 = createWsIdentdetaljer("historiskIdent1", Date.from(LocalDate.of(2014, Month.JANUARY, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
		IdentDetaljer historiskIdent2 = createWsIdentdetaljer("historiskIdent2", Date.from(LocalDate.of(2014, Month.FEBRUARY, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()));

		HentAktoerIdForIdentResponse response = createHentIdentForAktoerIdResponse();
		response.getIdentHistorikk().add(historiskIdent1);
		response.getIdentHistorikk().add(historiskIdent2);

		HentAktoerIdForIdentResponseTo responseTo = mapper.map(response);

		assertThat(responseTo.getHistoriskeIdenter().size(), is(2));
		assertThat(responseTo.getHistoriskeIdenter().get(0).getFnr(), is("historiskIdent1"));
		assertThat(responseTo.getHistoriskeIdenter().get(1).getFnr(), is("historiskIdent2"));
	}

	private HentAktoerIdForIdentResponse createHentIdentForAktoerIdResponse() {
		HentAktoerIdForIdentResponse wsResponse = new HentAktoerIdForIdentResponse();
		wsResponse.setAktoerId(AKTOER_ID);
		return wsResponse;
	}

	private IdentDetaljer createWsIdentdetaljer(String ident, Date fomDate) {
		IdentDetaljer identDetaljer = new IdentDetaljer();
		identDetaljer.setTpsId(ident);
		identDetaljer.setDatoFom(DateConverterUtil.convertDateToXMLGregorianCalendar(fomDate));
		return identDetaljer;
	}


}