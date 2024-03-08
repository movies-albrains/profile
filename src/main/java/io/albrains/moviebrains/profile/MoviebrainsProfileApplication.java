package io.albrains.moviebrains.profile;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class MoviebrainsProfileApplication implements CommandLineRunner {


	private final WebClient webclient;

	public MoviebrainsProfileApplication(@Qualifier("microservice") WebClient webclient) {
        this.webclient = webclient;
	}

    public static void main(String[] args) {
		SpringApplication.run(MoviebrainsProfileApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		authenticate();
		authenticateMicroservice();
	}

	private void authenticateMicroservice() {
		UserResponse response = webclient.get()
				.uri("http://localhost:9081/api/users/me")
				.retrieve()
				.bodyToMono(UserResponse.class)
				.block();
		System.out.println(response);
	}

	public void authenticate() {
		AccessTokenResponse response = RestClient.builder()
				.build()
				.post()
				.uri("http://localhost:9081/api/authenticate")
				.body(new AuthRequest("alex", "alex1234"))
				.retrieve()
				.body(AccessTokenResponse.class);
		System.out.println(response);
	}
}
