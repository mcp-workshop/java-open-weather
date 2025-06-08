package es.mcpworkshop.server;

import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherController {

  WeatherService weatherService;

  public WeatherController(WeatherService weatherService) {
    this.weatherService = weatherService;
  }

  @GetMapping()
  public WeatherService.WeatherPrediction weather() {
    return weatherService.getPrediction(52.52d, 13.41d, LocalDateTime.now().plusDays(3));
  }
}
