package es.mcpworkshop.server;

import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OpenWeather {

  public static void main(String[] args) {
    SpringApplication.run(OpenWeather.class, args);
  }

  @Bean
  MethodToolCallbackProvider methodToolCallbackProvider(WeatherService weatherService) {
    return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
  }
}
