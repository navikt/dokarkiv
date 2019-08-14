package no.nav.dokarkiv.sak;

import static io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation.HEADER;
import static java.util.stream.Collectors.toList;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_SAK_SAK;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.CREATE_ACTION;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.READ_ACTION;
import static no.nav.dokarkiv.sak.infrastruktur.ContextExtractor.getSubjectType;
import static no.nav.dokarkiv.sak.infrastruktur.SubjectType.SUBJECT_TYPE_EKSTERNBRUKER;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.BasicAuthDefinition;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.ResponseHeader;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.exceptions.AbacException;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.core.util.ErrorResponse;
import no.nav.dokarkiv.sak.dto.SakJson;
import no.nav.dokarkiv.sak.dto.SakSearchRequest;
import no.nav.dokarkiv.sak.repository.HentSakerRepository;
import no.nav.dokarkiv.sak.repository.SakSearchCriteria;
import no.nav.freg.abac.core.annotation.Abac;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/saker")
@Api(value = "/rest/saker", produces = "application/json")
@Slf4j
@SwaggerDefinition(
		info = @Info(
				title = "Sak API",
				version = "1",
				description = "Her dokumenteres tjenestegrensesnittet for Sak.\n\n" +
						"Tjenesten leveres kontinuerlig til produksjon. For å sikre oss mot å innføre regresjon som påvirker våre konsumenter, benytter vi " +
						"Pact. Det er konsumentens ansvar å gi oss pact-test, men ta gjerne kontakt ved behov for bistand ifm. dette. \n\n" +
						"Vi ber nye konsumenter om å ta kontakt med teamet, dette for å få gjennomført ev. avklaringer, sikre korrekte tilganger, pact-test, og for å sikre at tjenesten støtter " +
						"forventet volum og ev. SLA.\n\n" +
						"Merk at vi forventer at Headeren <strong>\"X-Correlation-ID\"</strong> er angitt for alle tjenestekall. Denne logges alltid i Sak, og benyttes for å kunne sammenstille hendelser " +
						"på tvers av kallkjeder. X-Correlation-ID skal oppgis ved forespørsel om bistand fra Team Oppgavehåntering vedr. feilsøk ifm. bruk av tjenesten\n" +
						"Vi anbefaler at korrelasjonsID genereres så tidlig som mulig hos konsument, bindes til tråden, og logges sammen med alle hendelser som danner grunnlaget for kallet mot Sak,\n\n" +
						"KorrelasjonsIDen skal være unik, og kan enten genereres med f.eks UUID.randomUUID() eller hvis aktuelt, hentes ut fra inngående tjenestekall (i.e. callId via modig-biblioteket)",
				contact = @Contact(
						name = "Team Oppgavehåndtering"
				))
		, securityDefinition =
@SecurityDefinition(apiKeyAuthDefinitions = {
		@ApiKeyAuthDefinition(
				name = "Authorization",
				key = "Bearer",
				in = HEADER,
				description = "OIDC-token (JWT via OAuth2.0). Dette preferert autentiseringsmekanisme, og <strong>skal</strong>" +
						" benyttes ved tjenestekall initiert av en bruker for å propagere konteksten (unntatt i særtilfeller - se Saml) \n" +
						" Følgende format må brukes i input-feltet \"Value\" under: <strong>\"Bearer {token}\"</strong>.\n" +
						" Eksempel på verdi i input-felt: <strong>Bearer eYdmifml0ejugm</strong>\n\n" +
						" Et gyldig token kommer til å ha mange flere karakterer enn i eksempelet."),

		@ApiKeyAuthDefinition(
				name = "Authorization",
				key = "Saml",
				in = HEADER,
				description = "P.t støttes ikke konvertering fra SAML til OIDC-token og det er derfor implementert støtte for Saml for å propagere brukercontext fra legacy-systemer " +
						" (i.e. fra et system som kun eksponerer soap-tjenester og som skal gjøre tjenestekall videre mot Oppgave.\n" +
						" I denne konteksten er et SAML token en SAML assertion som er Base 64 enkodet. \n" +
						" På grunn av begrensninger i header-lengde, må saml-assertion strippes for whitespaces før den encodes \n" +
						" Formatet skal være som følger: <strong>\"Saml {token}\"</strong>.\n" +
						" Eksempel på verdi i input-felt: <strong>Saml eYdmifml0ejugm</strong>\n\n" +
						" Et gyldig token kommer til å ha mange flere karakterer enn i eksempelet.")
},
		basicAuthDefinitions = {
				@BasicAuthDefinition(
						key = "Basic",
						description = "Basic auth kan brukes når det er snakk om system-til-system kommunikasjon")
		}
)
)
public class SakController {


	private final AbacSecurityService abacSecurityService;
	private final HentSakerRepository sakRepository;

	SakController(AbacSecurityService abacSecurityService, HentSakerRepository sakRepository) {
		this.abacSecurityService = abacSecurityService;

		this.sakRepository = sakRepository;
	}

	@ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
	public ResponseEntity exceptionHandler(Exception e) {
		List<String> violationMessages;
		if (e instanceof MethodArgumentNotValidException) {
			violationMessages = ((MethodArgumentNotValidException) e).getBindingResult()
					.getAllErrors()
					.stream()
					.map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
		} else {
			violationMessages = ((BindException) e).getBindingResult()
					.getAllErrors()
					.stream()
					.map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(MDC.get(MDCConstants.MDC_CALL_ID),
				StringUtils.join(violationMessages, ", ")));
	}

	@ResponseBody
	@GetMapping("/{id}")
	@ApiOperation(value = "Henter sak for en gitt id", response = SakJson.class, authorizations = {
			@Authorization(value = "Authorization"),
			@Authorization(value = "Saml"),
			@Authorization(value = "Basic")
	})
	@ApiImplicitParams({@ApiImplicitParam(name = "X-Correlation-ID", required = true, dataType = "string", paramType = "header")})
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, message = "OK"),
					@ApiResponse(code = 401, message = "Konsument mangler gyldig token"),
					@ApiResponse(code = 403, message = "Konsument har ikke tilgang til å gjennomføre handlingen"),
					@ApiResponse(code = 404, message = "Det finnes ingen sak for angitt id"),
					@ApiResponse(code = 500, message = "Ukjent feilsituasjon har oppstått i Sak"),
					@ApiResponse(code = 503, message = "En eller flere tjenester som sak er avhengig av er ikke tilgjengelige eller svarer ikke.")
			}
	)
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_SAK_SAK)},
			actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION))
	public ResponseEntity hentSak(@PathVariable final Long id) {

		log.info("Henter sak med id: {}", id);
		final Optional<Sak> sak = sakRepository.hentSak(id);

		final ResponseEntity response;
		if (sak.isPresent()) {

			final Sak eksisterendeSak = sak.get();
			response = checkUsersAccessToSak(eksisterendeSak);
		} else {

			log.warn("Mottatt oppslag på sak som ikke eksisterer, id: {}, consumer: {}", id, MDC.get(MDCConstants.MDC_CONSUMER_ID));
			response =
					ResponseEntity
							.status(HttpStatus.NOT_FOUND)
							.body(
									new ErrorResponse(
											MDC.get(MDCConstants.MDC_CALL_ID),
											String.format("Fant ingen sak med id: %s", id)
									)
							);
		}

		return response;
	}

	@GetMapping
	@ResponseBody
	@ApiOperation(value = "Finner saker for angitte søkekriterier",
			response = SakJson.class, responseContainer = "List", authorizations = {
			@Authorization(value = "Authorization"),
			@Authorization(value = "Saml"),
			@Authorization(value = "Basic")
	})
	@ApiImplicitParams({@ApiImplicitParam(name = "X-Correlation-ID", required = true, dataType = "string", paramType = "header")})
	@ApiResponses(
			value = {
					@ApiResponse(code = 200, message = "OK"),
					@ApiResponse(code = 400, message = "Ugyldig input"),
					@ApiResponse(code = 401, message = "Konsument mangler gyldig token"),
					@ApiResponse(code = 500, message = "Ukjent feilsituasjon har oppstått i Sak"),
					@ApiResponse(code = 503, message = "En eller flere tjenester som sak er avhengig av er ikke tilgjengelige eller svarer ikke.")
			}
	)
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_SAK_SAK)},
			actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION))
	public ResponseEntity finnSaker(
			@Valid final SakSearchRequest sakSearchRequest) {

		log.info("Søker etter saker for: {}", sakSearchRequest);
		ResponseEntity response;

		try {
			abacSecurityService.assertAccessToSakPep(sakSearchRequest.getAktoerId());
			final List<Sak> saker =
					sakRepository.finnSaker(sakSearchRequest.toCriteria());
			response =
					ResponseEntity
							.ok(saker
									.stream()
									.filter(this::harTilgangTilSakInterneRegler)
									.map(SakJson::new)
									.collect(toList())
							);
		} catch (AuthorizationException e) {
			response = ResponseEntity.ok(new ArrayList<>());
		} catch (AbacException e) {
			response = ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(
							new ErrorResponse(
									MDC.get(MDCConstants.MDC_CALL_ID),
									e.getMessage()
							)
					);
		}


		return response;
	}

	@PostMapping
	@ApiOperation(value = "Oppretter en ny sak", notes = "Merk at en sak enten skal tilhøre en aktør <b>eller</b> et foretak. Begge er p.t. ikke tillatt. ",
			authorizations = {
					@Authorization(value = "Authorization"),
					@Authorization(value = "Saml"),
					@Authorization(value = "Basic")
			})
	@ApiImplicitParams({@ApiImplicitParam(name = "X-Correlation-ID", required = true, dataType = "string", paramType = "header")})
	@ApiResponses(
			value = {
					@ApiResponse(code = 201, message = "Saken er opprettet", responseHeaders = @ResponseHeader(name = "location", description = "Angir URI til den opprettede saken")),
					@ApiResponse(code = 400, message = "Ugyldig input"),
					@ApiResponse(code = 401, message = "Konsument mangler gyldig token"),
					@ApiResponse(code = 403, message = "Konsument har ikke tilgang til å gjennomføre handlingen"),
					@ApiResponse(code = 409, message = "Det finnes allerede en sak for angitt kombinasjon av fagsaknr og applikasjon for aktør eller orgnr"),
					@ApiResponse(code = 500, message = "Ukjent feilsituasjon har oppstått i Sak"),
					@ApiResponse(code = 503, message = "En eller flere tjenester som sak er avhengig av er ikke tilgjengelige eller svarer ikke.")
			}
	)
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_SAK_SAK)},
			actions = @Abac.Attr(key = ACTION_ID, value = CREATE_ACTION))
	public ResponseEntity opprettSak(
			@Valid @RequestBody @ApiParam(value = "Saken som skal opprettes", required = true) final SakJson sakJson
	) throws URISyntaxException {
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		final String user = MDC.get(MDCConstants.MDC_USER_ID);
		final Sak innsendtSak = sakJson.toSak(user);
		final String aktoerId = innsendtSak.getAktoerId();

		log.info("Oppretter sak for {}", aktoerId);

		ResponseEntity response;

		try {
			abacSecurityService.assertAccessToSakPep(sakJson.getAktoerId());
			response = doOpprettSak(innsendtSak);
		} catch (AuthorizationException e) {
			response = ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body(new ErrorResponse(MDC.get(MDCConstants.MDC_CALL_ID), "Bruker kunne ikke autoriseres for denne operasjonen"));
		} catch (AbacException e) {
			response = ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(
							new ErrorResponse(
									MDC.get(MDCConstants.MDC_CALL_ID),
									e.getMessage()
							)
					);
		}

		return response;
	}

	private ResponseEntity doOpprettSak(final Sak sak) throws URISyntaxException {

		final ResponseEntity response;
		if (fagSakFinnesFraFoer(sak)) {

			response =
					ResponseEntity
							.status(HttpStatus.CONFLICT)
							.body(new ErrorResponse(
									MDC.get(MDCConstants.MDC_CALL_ID),
											String.format(
													"Det finnes allerede en sak for fagsaksnr: %s, applikasjon: %s, aktør: %s orgnr: %s",
													sak.getFagsakNr(),
													sak.getApplikasjon(),
													sak.getAktoerId(),
													sak.getOrgnr())
									)
							);
		} else {
			String baseUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();

			final Sak opprettetSak = sakRepository.lagre(sak);
			log.info("Opprettet: {}", opprettetSak);
			response =
					ResponseEntity
							.created(new URI(baseUrl + "/" + sak.getSakId()))
							.body(new SakJson(opprettetSak));
		}

		return response;
	}

	private boolean harTilgangTilSakInterneRegler(final Sak sak) {

		final boolean temaKontroll = Objects.equals("KTR", sak.getTema());
		final boolean harTilgang = !(temaKontroll && Objects.equals(getSubjectType(), SUBJECT_TYPE_EKSTERNBRUKER));
		if (!harTilgang) {
			log.info("Filtrerer ut sak med id: {} for ekstern bruker fordi den har tema: {} ", sak.getSakId(), sak.getTema());
		}

		return harTilgang;
	}

	private boolean fagSakFinnesFraFoer(Sak sak) {

		final SakSearchCriteria sakSearchCriteria =
				SakSearchCriteria
						.builder()
						.orgnr(sak.getOrgnr())
						.aktoerId(sak.getAktoerId())
						.fagsakNr(sak.getFagsakNr())
						.applikasjon(sak.getApplikasjon()).build();

		return sak.getFagsakNr() != null &&
				!sakRepository.finnSaker(sakSearchCriteria).isEmpty();
	}

	private ResponseEntity checkUsersAccessToSak(final Sak sak) {

		ResponseEntity response;
		try {
			abacSecurityService.assertAccessToSakPep(sak.getAktoerId());
			response =
					ResponseEntity
							.ok(new SakJson(sak));
		} catch (AuthorizationException e) {
			response =
					ResponseEntity
							.status(HttpStatus.FORBIDDEN)
							.body(
									new ErrorResponse(
											MDC.get(MDCConstants.MDC_CALL_ID),
											"Bruker kunne ikke autoriseres for denne operasjonen"
									)
							);
		} catch (AbacException e) {
			response = ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(
							new ErrorResponse(
									MDC.get(MDCConstants.MDC_CALL_ID),
									e.getMessage()
							)
					);
		}

		return response;
	}

}
