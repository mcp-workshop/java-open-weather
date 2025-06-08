package es.mcpworkshop.server;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WeatherService {

  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(WeatherService.class);

  private final RestClient restClient;

  public WeatherService() {
    this.restClient = RestClient.builder().baseUrl("https://api.open-meteo.com/v1").build();
  }

  @Tool(
      description =
          "Get the temperatures (in celsius), the rain percentage and the precipitation probability for a specific location for the next 7 days, hourly")
  public WeatherPrediction getPrediction(
      @ToolParam(description = "The location latitude") double latitude,
      @ToolParam(description = "The location longitude") double longitude,
      @ToolParam(description = "The local datetime of the prediction") LocalDateTime time) {
    logger.info(
        "Getting prediction for location lat: {}, long: {} at time {}", latitude, longitude, time);
    WeatherResponse weatherResponse =
        restClient
            .get()
            .uri(
                "/forecast?latitude={latitude}&longitude={longitude}&hourly=temperature_2m,precipitation_probability,rain&timezone=Europe/Berlin",
                latitude,
                longitude)
            .retrieve()
            .body(WeatherResponse.class);

    if (weatherResponse == null) {
      throw new RuntimeException("No history found");
    }

    Map<LocalDateTime, WeatherPrediction> weatherPredictions =
        getWeatherPredictionMap(weatherResponse);
    var dateToCheck =
        time.withMinute(0)
            .withSecond(0)
            .withNano(0)
            .atZone(ZoneId.of("Europe/London"))
            .toLocalDateTime();
    WeatherPrediction weatherPrediction = weatherPredictions.get(dateToCheck);
    logger.info("WeatherPrediction is {}", weatherPrediction);
    return weatherPrediction;
  }

  private Map<LocalDateTime, WeatherPrediction> getWeatherPredictionMap(
      WeatherResponse weatherResponse) {

    double rain;
    int precipitationProbability;
    double temperature;
    List<LocalDateTime> dateTimes = weatherResponse.hourly().time();
    Map<LocalDateTime, WeatherPrediction> weatherPredictions = new HashMap<>(dateTimes.size());
    for (int i = 0; i < dateTimes.size(); i++) {
      precipitationProbability = weatherResponse.hourly().precipitation_probability().get(i);
      rain = weatherResponse.hourly().rain().get(i);
      temperature = weatherResponse.hourly().temperature_2m().get(i);
      weatherPredictions.put(
          dateTimes.get(i), new WeatherPrediction(precipitationProbability, rain, temperature));
    }
    return weatherPredictions;
  }

  /** The response format from the Open-Meteo API */
  public record WeatherResponse(Hourly hourly) {
    public record Hourly(
        List<Integer> precipitation_probability,
        List<Double> rain,
        List<Double> temperature_2m,
        List<LocalDateTime> time) {}
  }

  public record WeatherPrediction(
      Integer precipitation_probability, Double rain, Double temperature) {}
}
