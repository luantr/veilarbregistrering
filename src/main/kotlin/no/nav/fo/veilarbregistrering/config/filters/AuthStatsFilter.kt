package no.nav.fo.veilarbregistrering.config.filters

import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTParser
import io.micrometer.core.instrument.Tag
import no.nav.common.auth.Constants
import no.nav.fo.veilarbregistrering.log.loggerFor
import no.nav.fo.veilarbregistrering.log.secureLogger
import no.nav.fo.veilarbregistrering.metrics.Events
import no.nav.fo.veilarbregistrering.metrics.MetricsService
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import java.text.ParseException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class AuthStatsFilter(private val metricsService: MetricsService) : Filter {

    private val ID_PORTEN = "ID-PORTEN"
    private val AAD = "AAD"
    private val TOKEN_X = "TOKENX"
    private val STS = "STS"

    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, chain: FilterChain) {
        val request: HttpServletRequest = servletRequest as HttpServletRequest
        val consumerId = getConsumerId(request)

        val cookieNames = request.cookies?.map { it.name } ?: emptyList()
        val headerValue = request.getHeader(HttpHeaders.AUTHORIZATION)
        val bearerToken = headerValue?.substring("Bearer ".length)
        val selvbetjeningToken =
            request.cookies?.filter { it.name == Constants.AZURE_AD_B2C_ID_TOKEN_COOKIE_NAME }?.map { it.value }
                ?.firstOrNull()
        val type = when {
            Constants.AZURE_AD_B2C_ID_TOKEN_COOKIE_NAME in cookieNames -> selvbetjeningToken?.let { checkTokenForType(it) }
                ?: ID_PORTEN
            Constants.AZURE_AD_ID_TOKEN_COOKIE_NAME in cookieNames -> AAD
            !bearerToken.isNullOrBlank() -> checkTokenForType(bearerToken)
            else -> null
        }

        try {
            type?.let {
                MDC.put(TOKEN_TYPE, type)
                metricsService.registrer(Events.REGISTRERING_TOKEN, Tag.of("type", type), Tag.of("consumerId", consumerId))
                log.info("Authentication with: [$it] request path: [${request.servletPath}] consumer: [$consumerId]")
                if (type == STS) {
                    secureLogger.info("Bruk av STS-token mot $consumerId. Token fra cookie: $selvbetjeningToken Token fra Auth-header: $bearerToken")
                }
            }
            chain.doFilter(servletRequest, servletResponse)
        } finally {
            MDC.remove(TOKEN_TYPE)
        }
    }

    private fun checkTokenForType(token: String): String =
        try {
            val jwt = JWTParser.parse(token)
            when {
                jwt.erAzureAdToken() -> AAD
                jwt.erIdPortenToken() -> ID_PORTEN
                jwt.erTokenXToken() -> TOKEN_X
                else -> STS
            }
        } catch (e: ParseException) {
            log.warn("Couldn't parse token $token")
            when {
                token.contains("microsoftonline.com") -> AAD
                token.contains("difi.no") -> ID_PORTEN
                token.contains("tokendings") -> TOKEN_X
                else -> STS
            }
        }

    companion object {
        private const val TOKEN_TYPE = "tokenType"
        private val log = loggerFor<AuthStatsFilter>()

        private fun getConsumerId(request: HttpServletRequest): String = request.getHeader("Nav-Consumer-Id") ?: "UKJENT"
    }
}

fun JWT.erAzureAdToken(): Boolean = this.jwtClaimsSet.issuer.contains("microsoftonline.com")
fun JWT.erIdPortenToken(): Boolean = this.jwtClaimsSet.issuer.contains("difi.no")
fun JWT.erTokenXToken(): Boolean = this.jwtClaimsSet.issuer.contains("tokendings")