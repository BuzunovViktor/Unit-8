import core.Line;
import core.Station;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;


public class RouteCalculatorTest {

    private static String dataFile = "src/main/resources/map.json";
    private static StationIndex stationIndex;
    private static RouteCalculator routeCalculator;

    @BeforeAll
    static public void beforeClass() {
        createStationIndex();
        routeCalculator = new RouteCalculator(stationIndex);
    }

    private static void createStationIndex() {
        stationIndex = new StationIndex();
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonData = (JSONObject) parser.parse(readJSONFile(dataFile));
            JSONArray linesArray = (JSONArray) jsonData.get("lines");
            parseLines(linesArray);
            JSONObject stationsObject = (JSONObject) jsonData.get("stations");
            parseStations(stationsObject);
            JSONArray connectionsArray = (JSONArray) jsonData.get("connections");
            parseConnections(connectionsArray);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void parseConnections(JSONArray connectionsArray) {
        connectionsArray.forEach(connectionObject -> {
            JSONArray connection = (JSONArray) connectionObject;
            List<Station> connectionStations = new ArrayList<>();
            connection.forEach(item -> {
                JSONObject itemObject = (JSONObject) item;
                int lineNumber = ((Long) itemObject.get("line")).intValue();
                String stationName = (String) itemObject.get("station");
                Station station = stationIndex.getStation(stationName, lineNumber);
                if (station == null) {
                    throw new IllegalArgumentException("core.Station " +
                            stationName + " on line " + lineNumber + " not found");
                }
                connectionStations.add(station);
            });
            stationIndex.addConnection(connectionStations);
        });
    }

    private static void parseStations(JSONObject stationsObject) {
        stationsObject.keySet().forEach(lineNumberObject ->
        {
            int lineNumber = Integer.parseInt((String) lineNumberObject);
            Line line = stationIndex.getLine(lineNumber);
            JSONArray stationsArray = (JSONArray) stationsObject.get(lineNumberObject);
            stationsArray.forEach(stationObject ->
            {
                Station station = new Station((String) stationObject, line);
                stationIndex.addStation(station);
                line.addStation(station);
            });
        });
    }

    private static void parseLines(JSONArray linesArray) {
        linesArray.forEach(lineObject -> {
            JSONObject lineJsonObject = (JSONObject) lineObject;
            Line line = new Line(
                    ((Long) lineJsonObject.get("number")).intValue(),
                    (String) lineJsonObject.get("name")
            );
            stationIndex.addLine(line);
        });
    }

    public static String readJSONFile(String path) {
        StringBuilder builder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));
            lines.forEach(line -> builder.append(line));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return builder.toString();
    }


    @ParameterizedTest
    @MethodSource("getRoute")
    public void calculateDuration(List<Station> route, Double expected) {
        if (route != null) {
            Double duration = RouteCalculator.calculateDuration(route);
            assertEquals(expected, duration, 0.01);
        } else {
            assertThrows(NullPointerException.class, () -> {
                RouteCalculator.calculateDuration(route);
            });
        }

    }

    private static Stream<Arguments> getRoute() {

        List<Station> route1 = routeCalculator.getShortestRoute(stationIndex.getStation("Девяткино"),stationIndex.getStation("Гражданский проспект"));
        Double expectedDuration1 = 2.5;

        List<Station> route2 = routeCalculator.getShortestRoute(stationIndex.getStation("Горьковская"), stationIndex.getStation("Маяковская"));
        Double expectedDuration2 = 8.5;

        List<Station> route3 = routeCalculator.getShortestRoute(stationIndex.getStation("Волковская"),stationIndex.getStation("Ладожская"));
        Double expectedDuration3 = 23.5;

        Line line1 = new Line(1,"Line1");
        Line line2 = new Line(2,"Line2");
        Line line3 = new Line(3,"Line3");
        Station station11 = new Station("Station11",line1);
        Station station12 = new Station("Station12",line1);
        Station station13 = new Station("Station13",line1);
        Station station21 = new Station("Station21",line2);
        Station station22 = new Station("Station22",line2);
        Station station23 = new Station("Station23",line2);
        Station station31 = new Station("Station31",line3);
        Station station32 = new Station("Station32",line3);
        Station station33 = new Station("Station33",line3);

        ArrayList<Station> customRoute1 = new ArrayList<>();
        customRoute1.add(station11);
        customRoute1.add(station12);
        customRoute1.add(station13);
        Double expectedDuration4 = 5.0;

        ArrayList<Station> customRoute2 = new ArrayList<>();
        customRoute2.add(station13);
        customRoute2.add(station21);
        Double expectedDuration5 = 3.5;

        ArrayList<Station> customRoute3 = new ArrayList<>();
        customRoute3.add(station11);
        customRoute3.add(station12);
        customRoute3.add(station13);
        customRoute3.add(station21);
        customRoute3.add(station22);
        customRoute3.add(station23);
        customRoute3.add(station31);
        customRoute3.add(station32);
        customRoute3.add(station33);
        Double expectedDuration6 = 22.0;

        List<Station> directRoute = routeCalculator.getShortestRoute(stationIndex.getStation("Парнас"),stationIndex.getStation("Купчино"));
        Double expectedDuration7 = 42.5;

        return Stream.of(
                    Arguments.of(route1, expectedDuration1),
                    Arguments.of(route2, expectedDuration2),
                    Arguments.of(route3, expectedDuration3),
                    Arguments.of(customRoute1, expectedDuration4),
                    Arguments.of(customRoute2, expectedDuration5),
                    Arguments.of(customRoute3, expectedDuration6),
                    Arguments.of(directRoute, expectedDuration7),
                    Arguments.of(null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("getTwoStations")
    public void getShortestRoute(List<Station> expectedRoute, Station from, Station to) {
        if (expectedRoute == null || from == null || to== null) {
            assertThrows(NullPointerException.class, () -> {
                RouteCalculator.calculateDuration(expectedRoute);
            });
        } else {
            List<Station> actualRoute = routeCalculator.getShortestRoute(from,to);
            assertThat(actualRoute, is(expectedRoute));
            assertIterableEquals(expectedRoute,actualRoute);
        }
    }

    private static Stream<Arguments> getTwoStations() {

        List<Station> route1 = new ArrayList<>() {{
            add(stationIndex.getStation("Парнас"));
            add(stationIndex.getStation("Проспект Просвещения"));
            add(stationIndex.getStation("Озерки"));
        }};

        List<Station> route2 = new ArrayList<>() {{
            add(stationIndex.getStation("Обводный канал"));
            add(stationIndex.getStation("Звенигородская"));
            add(stationIndex.getStation("Пушкинская"));
            add(stationIndex.getStation("Владимирская"));
            add(stationIndex.getStation("Площадь восстания"));
            add(stationIndex.getStation("Маяковская"));
            add(stationIndex.getStation("Площадь Александра Невского"));
            add(stationIndex.getStation("Елизаровская"));
        }};

        List<Station> route3 = new ArrayList<>() {{
            add(stationIndex.getStation("Приморская"));
            add(stationIndex.getStation("Василеостровская"));
            add(stationIndex.getStation("Гостиный двор"));
            add(stationIndex.getStation("Невский проспект"));
            add(stationIndex.getStation("Горьковская"));
        }};

        List<Station> route4 = new ArrayList<>() {{
            add(stationIndex.getStation("Волковская"));
            add(stationIndex.getStation("Обводный канал"));
            add(stationIndex.getStation("Звенигородская"));
            add(stationIndex.getStation("Садовая"));
            add(stationIndex.getStation("Сенная площадь"));
            add(stationIndex.getStation("Технологический институт",2));
            add(stationIndex.getStation("Фрунзенская"));
        }};

        return Stream.of(
                Arguments.of(route1, stationIndex.getStation("Парнас"), stationIndex.getStation("Озерки")),
                Arguments.of(route2, stationIndex.getStation("Обводный канал"), stationIndex.getStation("Елизаровская")),
                Arguments.of(route3, stationIndex.getStation("Приморская"), stationIndex.getStation("Горьковская")),
                Arguments.of(route4, stationIndex.getStation("Волковская"), stationIndex.getStation("Фрунзенская")),
                Arguments.of(null,null,null)
        );

    }

}
