package no.nav.fo.veilarbregistrering.config.filters

import io.micrometer.core.instrument.Tag
import no.nav.common.auth.context.AuthContextHolder
import no.nav.fo.veilarbregistrering.metrics.Events
import no.nav.fo.veilarbregistrering.metrics.MetricsService
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class RequestMetricsFilter(
    private val authContextHolder: AuthContextHolder,
    private val metricsService: MetricsService
) : Filter {

    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, chain: FilterChain) {
        val request: HttpServletRequest = servletRequest as HttpServletRequest

        val consumerId = request.getHeader("Nav-Consumer-Id")
        val endepunkt = request.servletPath
        val rolle = authContextHolder.requireRole()

        metricsService.registrer(
            Events.INNKOMMENDE_REQUEST_EVENT,
            Tag.of("consumerId", consumerId),
            Tag.of("endepunkt", endepunkt),
            Tag.of("rolle", rolle.name)
        )

        chain.doFilter(servletRequest, servletResponse)
    }
}