package org.linkmessenger.request

import android.annotation.SuppressLint
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import org.linkmessenger.request.api.SocialApi
import org.thoughtcrime.securesms.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Inet4Address
import java.net.InetAddress
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

val networkModule = module {
    factory { TokenAuthInterceptor() }
    factory { provideOkHttpClient(get()) }
    factory { StorageClient() }
    factory { provideSocialApi(get()) }
    single { provideRetrofit(get()) }
}

fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder().baseUrl(BuildConfig.SOCIAL_URL).client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create()).build()
}

fun provideOkHttpClient(tokenAuthInterceptor: TokenAuthInterceptor): OkHttpClient {
    val clientBuilder = OkHttpClient().newBuilder().dns(DnsSelector()).addInterceptor(tokenAuthInterceptor)
    if (BuildConfig.DEBUG) {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        clientBuilder.addInterceptor(logging)
    }
    return clientBuilder.connectTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .readTimeout(10, TimeUnit.MINUTES).apply {
        ignoreAllSSLErrors()
    }.build()
}
fun provideSocialApi(retrofit: Retrofit): SocialApi = retrofit.create(SocialApi::class.java)
private fun OkHttpClient.Builder.ignoreAllSSLErrors(): OkHttpClient.Builder {
    val naiveTrustManager = @SuppressLint("CustomX509TrustManager")
    object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
    }

    val insecureSocketFactory = SSLContext.getInstance("TLSv1.2").apply {
        val trustAllCerts = arrayOf<TrustManager>(naiveTrustManager)
        init(null, trustAllCerts, SecureRandom())
    }.socketFactory

    sslSocketFactory(insecureSocketFactory, naiveTrustManager)
    hostnameVerifier { _, _ -> true }
    return this
}
class DnsSelector : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return Dns.SYSTEM.lookup(hostname).filter { Inet4Address::class.java.isInstance(it) }
    }
}