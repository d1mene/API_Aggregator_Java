package d1mene.data;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WeatherAPIData implements APIRecord {

    @SerializedName("location")
    private Location location;

    @SerializedName("current")
    private Current current;

    @Override
    public String getSourceName() {
        return "WeatherAPI";
    }

    @Data
    @NoArgsConstructor
    public static class Location {

        @SerializedName("name")
        private String name;

        @SerializedName("country")
        private String country;

        @SerializedName("localtime")
        private String localTime;
    }

    @Data
    @NoArgsConstructor
    public static class Current {

        @SerializedName("temp_c")
        private double tempC;

        @SerializedName("feelslike_c")
        private double feelsLikeC;

        @SerializedName("humidity")
        private int humidity;

        @SerializedName("wind_kph")
        private double windKph;

        @SerializedName("condition")
        private Condition condition;

        @SerializedName("air_quality")
        private AirQuality airQuality;
    }

    @Data
    @NoArgsConstructor
    public static class Condition {

        @SerializedName("text")
        private String text;
    }

    @Data
    @NoArgsConstructor
    public static class AirQuality {

        @SerializedName("co")
        private double co;

        @SerializedName("pm2_5")
        private double pm25;

        @SerializedName("pm10")
        private double pm10;
    }
}