package com.example.GradleTest;

import org.springframework.core.io.ClassPathResource;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.CertificateException;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Objects;

public class CatFactApiClient {

	public static WebClient createWebClientWithSSL() throws Exception {
		// Load PEM certificate
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate certificate;
		try (FileInputStream inputStream = new FileInputStream("cert.pem")) {
			certificate = (X509Certificate) cf.generateCertificate(inputStream);
		}

		// Create a KeyStore and add the certificate
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, null);
		keyStore.setCertificateEntry("alias", certificate);

		// Create SSLContext
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, null, null); // You might need to configure KeyManager and TrustManager

//	        HttpClient httpClient = HttpClient.create()
//	                .secure(spec -> spec.sslContext(sslContext));

//	        WebClient webClient = WebClient.builder()
//	                .clientConnector(new ReactorClientHttpConnector(httpClient))
//	                .baseUrl("https://cat-fact.herokuapp.com/")
//	                .build();

		return null;
	}

	public static X509Certificate buildX509Certificate(String certPem)
			throws CertificateException, java.security.cert.CertificateException {
		X509Certificate x509Certificate = null;

		if (Objects.nonNull(certPem)) {
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

			certPem = certPem.replace("-----BEGIN CERTIFICATE-----", "").replace("-----END CERTIFICATE-----", "");
			System.out.println(certPem);

			byte[] certBytes = Base64.getMimeDecoder().decode(certPem); // get cert bytes
			ByteArrayInputStream certInputStream = new ByteArrayInputStream(certBytes);
			x509Certificate = (X509Certificate) certificateFactory.generateCertificate(certInputStream); // generate
																											// certificate
		}
		return x509Certificate;
	}

	public static SslContext buildSSLContext(X509Certificate certificate, String alias)
			throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
			UnrecoverableKeyException, java.security.cert.CertificateException {
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, null); // Use a null input stream + password to create an empty key store.

		keyStore.setCertificateEntry(alias, certificate); // add certificate to keystore

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, null);

		// Create an SSL context that uses that certificate
		return SslContextBuilder.forClient().keyManager(keyManagerFactory).build();
	}

	public static String readFileAsString(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		byte[] bytes = Files.readAllBytes(path);
		return new String(bytes);
	}

	public static void main(String[] args) throws Exception {
		String certificatePath = "cert.pem";
		String s = readFileAsString("G:/learning/GradleTest/GradleTest/cert.pem");
		// System.out.println("s ===== "+s);
		X509Certificate x509Certificate = buildX509Certificate(
				readFileAsString("G:/learning/GradleTest/GradleTest/cert.pem"));
		System.out.println("--------------");
		SslContext sslContext = buildSSLContext(x509Certificate, "my_cert");
		ReactorClientHttpConnector reactorClientHttpConnector = new ReactorClientHttpConnector(HttpClient.create()
		        .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext))
		);
		WebClient webClient1 = WebClient.builder()
		        .clientConnector(reactorClientHttpConnector)
		        .build();
		// WebClient webClient = createWebClientWithCertificate(certificatePath);
//
		WebClient webClient2 = WebClient.builder()
				 .baseUrl("https://expired.badssl.com/")
	                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
	                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1 MB buffer
	                .build();
		  WebClient webClient = WebClient.builder()
	                .baseUrl("https://self-signed.badssl.com/")
	                                .build();

	        Mono<String> responseMono = webClient1.get()
	                .uri("")
	                .retrieve()
	                .bodyToMono(String.class);

	        String response = responseMono.block();
	        System.out.println(response);
	    
//
	}
}
