package d1mene.data;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class OpenExchangeRatesData implements APIRecord {

    @SerializedName("disclaimer")
    private String disclaimer;

    @SerializedName("license")
    private String license;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("base")
    private String base;

    @SerializedName("rates")
    private Map<String, Double> rates;

    @Override
    public String getSourceName() {
        return "OpenExchangeRates";
    }
}