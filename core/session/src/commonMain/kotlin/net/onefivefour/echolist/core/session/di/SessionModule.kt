package net.onefivefour.echolist.core.session.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import net.onefivefour.echolist.core.networking.data.client.ConnectRpcClient
import net.onefivefour.echolist.core.networking.data.config.NetworkConfigProvider
import net.onefivefour.echolist.core.networking.data.logging.LogLevel
import net.onefivefour.echolist.core.networking.data.logging.NetworkLoggingPlugin
import net.onefivefour.echolist.core.networking.di.createConnectRpcClient
import net.onefivefour.echolist.core.session.data.auth.AuthInterceptor
import net.onefivefour.echolist.core.session.data.repository.AuthRepositoryImpl
import net.onefivefour.echolist.core.session.domain.AuthEventBus
import net.onefivefour.echolist.core.session.domain.AuthRepository
import net.onefivefour.echolist.core.session.domain.SecureStorage
import net.onefivefour.echolist.core.session.domain.SessionManager
import org.koin.core.module.Module
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module

expect val sessionStorageModule: Module

private val publicAuthClient = named("public-auth-client")
private val publicAuthHttp = named("public-auth-http")

val sessionModule = module {
    includes(sessionStorageModule)
    single { AuthEventBus() }
    single { NetworkConfigProvider(secureStorage = get<SecureStorage>()) }
    // Login and refresh use a client without an auth interceptor: no dependency cycle or recursive refresh.
    single(publicAuthHttp) { configuredHttpClient(get()) } withOptions { onClose { it?.close() } }
    single<ConnectRpcClient>(publicAuthClient) { createConnectRpcClient(get(publicAuthHttp), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(publicAuthClient), get()) }
    single {
        SessionManager(
            authRepository = get(),
            authEventBus = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            sessionDataCleaners = getAll()
        )
    }
    single {
        val repository = get<AuthRepository>()
        val events = get<AuthEventBus>()
        configuredHttpClient(get()) {
            install(AuthInterceptor) {
                authRepository = repository
                authEventBus = events
            }
        }
    } withOptions { onClose { it?.close() } }
    single<ConnectRpcClient> { createConnectRpcClient(get<HttpClient>(), get()) }
}

private fun configuredHttpClient(
    configProvider: NetworkConfigProvider,
    configure: io.ktor.client.HttpClientConfig<*>.() -> Unit = {}
): HttpClient = HttpClient {
    install(NetworkLoggingPlugin) { minLogLevel = LogLevel.DEBUG }
    install(HttpTimeout) {
        requestTimeoutMillis = configProvider.config.requestTimeoutMs
        connectTimeoutMillis = configProvider.config.connectTimeoutMs
    }
    configure()
}