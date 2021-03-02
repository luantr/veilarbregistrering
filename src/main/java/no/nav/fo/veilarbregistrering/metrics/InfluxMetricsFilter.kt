package no.nav.fo.veilarbregistrering.metrics

import no.nav.common.metrics.MetricsClient
import no.nav.common.utils.EnvironmentUtils
import okhttp3.Interceptor
import okhttp3.Response

class InfluxMetricsFilter(private val metricsClient: MetricsClient, private val event: Event) : okhttp3.Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val startTime = System.nanoTime()
        val request = chain.request()
        var throwable: Throwable? = null
        var response: okhttp3.Response? = null

        try {
            response = chain.proceed(request)
        } catch (t: Throwable) {
            throwable = t
        } finally {
            reportTimer(event, startTime, response?.code() ?: -1, failureCause(throwable, response))
        }

        return when (throwable) {
            null -> response ?: throw IllegalStateException("Error in InfluxMetrixFilter, missing response")
            else -> throw throwable
        }
    }

    private fun failureCause(throwable: Throwable?, response: okhttp3.Response?): String? =
        when {
            throwable != null -> throwable.message
            response == null -> null
            !response.isSuccessful -> "Response code ${response.code()}"
            else -> null
        }

    private fun reportTimer(event: Event, startTime: Long, httpStatus: Int, failureCause: String? = null) {
        no.nav.common.metrics.Event("${event.key}.timer")
            .also { metricsEvent ->
                metricsEvent.addFieldToReport("value", (System.nanoTime() - startTime) / 1e6)
                metricsEvent.addTagToReport("httpStatus", httpStatus.toString())
                metricsEvent.addTagToReport(
                    "environment",
                    EnvironmentUtils.getOptionalProperty("APP_ENVIRONMENT_NAME").orElse("local")
                )
                failureCause?.let { metricsEvent.addFieldToReport("aarsak", failureCause) }
                metricsClient.report(metricsEvent)
            }
    }

}
