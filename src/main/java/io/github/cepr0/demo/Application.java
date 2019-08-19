package io.github.cepr0.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

@Slf4j
@RestController
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class)
				.bannerMode(Banner.Mode.OFF)
				.run(args);
	}

	@GetMapping("/users/{id}")
	public User get(@PathVariable int id) {
		log.info("[i] Controller: received request GET /users/{}", id);
		return new User(id, "John Smith");
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder templateBuilder) {
		ClientHttpRequestFactory requestFactory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
		return templateBuilder
				.interceptors((request, bytes, execution) -> {
					log.info("[i] Interceptor: invoked {} {}", request.getMethod(), request.getURI());
					ClientHttpRequest delegate = requestFactory.createRequest(request.getURI(), request.getMethod());
					ClientHttpResponse response = delegate.execute();
					String body = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());
					log.info("[i] Interceptor: response body is '{}'", body);
					return response;
				})
				.rootUri("http://localhost:8080")
				.build();
	}

	@Bean
	ApplicationRunner run(RestTemplate restTemplate) {
		return args -> {
			ResponseEntity<User> response = restTemplate.getForEntity("/users/{id}", User.class, 1);
			if (response.getStatusCode().is2xxSuccessful()) {
				log.info("[i] User: {}", response.getBody());
			} else {
				log.error("[!] Error: {}", response.getStatusCode());
			}
		};
	}
}
