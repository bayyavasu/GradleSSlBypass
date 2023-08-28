package com.example.GradleTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.tcp.TcpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;

@Service
public class MyWebClientService {

//    public WebClient buildWebClient() {
//        Object timeoutValue = 100000;
//		TcpClient tcpClient = TcpClient
//                .create()
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutValue )
//                .doOnConnected(connection -> {
//                    connection.addHandlerLast(new ReadTimeoutHandler(timeoutValue, TimeUnit.MILLISECONDS));
//                    connection.addHandlerLast(new WriteTimeoutHandler(timeoutValue, TimeUnit.MILLISECONDS));
//                });
//
//        return WebClient
//                .builder()
//                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//                .codecs(configurer -> configurer
//                        .defaultCodecs()
//                        .maxInMemorySize(16 * 1024 * 1024))
//                .filter(logRequest())
//                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
//                .build();
//    }
//
    public static WebClient buildWebClientWithSSLBypass() throws NoSuchAlgorithmException, KeyManagementException {
    	SSLContext sslContext = SSLContext.getInstance("TLS");
    	TrustManager[] trustAllCertificates = new TrustManager[]{
    		    new X509TrustManager() {
    		        public X509Certificate[] getAcceptedIssuers() {
    		            return null;
    		        }
    		        public void checkClientTrusted(X509Certificate[] certs, String authType) {
    		        }
    		        public void checkServerTrusted(X509Certificate[] certs, String authType) {
    		        }
    		    }
    		};

    	sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());

    	TcpClient tcpClient = TcpClient
    	        .create()
    	        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000000000)
    	        .doOnConnected(connection -> {
    	            connection.addHandlerLast(new ReadTimeoutHandler(1000000000, TimeUnit.MILLISECONDS));
    	            connection.addHandlerLast(new WriteTimeoutHandler(1000000000, TimeUnit.MILLISECONDS));
    	        });

    	HttpClient httpClient = HttpClient.from(tcpClient)
    	        .secure(sslProviderSpec -> sslProviderSpec.sslContext(new JdkSslContext(sslContext, false, ClientAuth.NONE)));
   // 	HttpClient httpClient1 = HttpClient.from(tcpClient)
    	
    	
    //	        .secure(spec -> spec.sslContext(new ReactorNettyCustomSslContext(sslContext)));  // Use your custom SSLContext wrapper


    	WebClient webClient = WebClient.builder()
    	        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    	        .codecs(configurer -> configurer
    	                .defaultCodecs()
    	                .maxInMemorySize(16 * 1024 * 1024))
    	        .filter(logRequest())
    	        .clientConnector(new ReactorClientHttpConnector(httpClient))
    	        .build();
		return webClient;

    }

     static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            return Mono.just(clientRequest);
        });
    }
    
    

    static void tryToGenerateToken(String clientId, String clientSecret) throws Exception {
//           final String PATH = "/confidential/oauth2/token";
           final String PATH = "/as/token.oauth2";

           MultiValueMap<String, String> formData = new LinkedMultiValueMap();

           formData.add("grant_type", "client_credentials");
           formData.add("client_id", clientId);
           formData.add("client_secret", clientSecret);
//           formData.add("scope", scope);
           System.out.println(clientId +"   "+clientSecret);
          // WebClient webClient = webClientFactory.create(issuerClaim);
           WebClient webClient = null;
           webClient = buildWebClientWithSSLBypass();
           
           
           
           
           
           
           
           
           WebClient webClient3 = WebClient.builder()
	                .baseUrl("https://self-signed.badssl.com/")
	                                .build();

	        Mono<String> responseMono2 = webClient3.get()
	                .uri("https://self-signed.badssl.com/")
	                .retrieve()
	                .bodyToMono(String.class);
           
           System.out.println("responseMono2 =====  "+responseMono2.block());
           
           
           Mono<String> responseMono = webClient.get()
	                .uri("https://expired.badssl.com/")
	                
	                .retrieve()
	                .bodyToMono(String.class);
//           Mono<String> masterDealerToken = webClient.post()
//                   .uri("https://sso-int.mercedes-benz.com/as/token.oauth2")  // Include the full path to the endpoint
//                   .bodyValue("grant_type=client_credentials&client_id="+clientId+"+client_secret="+clientSecret)
//                   .retrieve()
//                   .bodyToMono(String.class);
           System.out.println(responseMono.block());
       }
    public static void main(String[] args) throws Exception {
    	
    	
		tryToGenerateToken("97315b19-328d-47b0-8634-7e0c8a0355e8","b3e63547-3f11-4b6b-9f0c-b10f13fd9627" );
	}
}
