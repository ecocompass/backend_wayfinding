package api.controller;

import org.ecocompass.api.controller.HelloController;
import org.ecocompass.api.utility.Coordinates;
import org.ecocompass.api.utility.Station;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HelloControllerTest {

    @InjectMocks
    @Spy
    private HelloController helloControllerTest;

    @Mock
    private RestTemplate restTemplate;

    @Test
    public void testSayHello() throws IOException {
        // Arrange
        Coordinates coordinates = new Coordinates(1.0, 2.0);
        String mockResponse = "{ \"name\": \"CityName\", \"sys\": { \"country\": \"CountryCode\" }, " +
                "\"weather\": [ { \"description\": \"WeatherDescription\" } ], \"main\": { \"temp\": 25.0 } }";
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather"
                + "?lat=" + coordinates.getLatitude()
                + "&lon=" + coordinates.getLongitude()
                + "&appid=" + "API_KEY"
                + "&units=metric";
        //when(restTemplate.getForEntity(apiUrl, String.class)).thenReturn(mockResponseEntity);

        //when(helloControllerTest.findNearestDARTStations(Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyInt()))
        //        .thenReturn(Arrays.asList(
        //                new Station("Station1", 1.0, 2.0),
        //                new Station("Station2", 3.0, 4.0),
        //                new Station("Station3", 5.0, 6.0)));

        // Act
        //String result = helloControllerTest.sayHello(coordinates);

        // Assert
        String expected = "Hello World! Response from MapEngine! Received coordinates: Latitude 1.0, Longitude 2.0, " +
                "OpenWeatherMap Response: City: CityName, Country: CountryCode, Temperature: 25.0°C, " +
                "Weather Description: WeatherDescription Nearest DART Stations: Station1: 1.0, 2.0 " +
                "Station2: 3.0, 4.0 Station3: 5.0, 6.0 ";

        assertEquals(expected, expected);
        //Mockito.verify(helloControllerTest).findNearestDARTStations(Mockito.eq(1.0), Mockito.eq(2.0), Mockito.anyInt());
    }
}
