Example of Spring RestTemplate interceptor with `BufferingClientHttpResponseWrapper` 
that reads the response's body into memory, thus allowing for multiple invocations of `getBody()`

```java
@Bean
public RestTemplate restTemplate(RestTemplateBuilder templateBuilder) {
   ClientHttpRequestFactory requestFactory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
   return templateBuilder
      .interceptors((request, bytes, execution) -> {
         log.info("[i] Interceptor: invoked {} {}", request.getMethod(), request.getURI());
         ClientHttpRequest delegate = requestFactory.createRequest(request.getURI(), request.getMethod());
request.getHeaders().forEach((key, values) -> values.forEach(value -> delegate.getHeaders().add(key, value)));
         ClientHttpResponse response = delegate.execute();
         String body = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());
         log.info("[i] Interceptor: response body is '{}'", body);
         return response;
      })
      .rootUri("http://localhost:8080")
      .build();
}
```