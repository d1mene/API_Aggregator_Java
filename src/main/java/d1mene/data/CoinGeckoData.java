package d1mene.data;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CoinGeckoData implements APIRecord {

    @SerializedName("id")
    private String id;

    @SerializedName("symbol")
    private String symbol;

    @SerializedName("name")
    private String name;

    @SerializedName("current_price")
    private double currentPrice;

    @SerializedName("market_cap")
    private double marketCap;

    @SerializedName("price_change_24h")
    private double priceChange24h;

    @SerializedName("price_change_percentage_24h")
    private double priceChangePercent24h;

    @SerializedName("last_updated")
    private String lastUpdated;

    @SerializedName("roi")
    private Roi roi;

    @Override
    public String getSourceName() {
        return "CoinGecko";
    }

    @Data
    @NoArgsConstructor
    public static class Roi {

        @SerializedName("times")
        private double times;

        @SerializedName("currency")
        private String currency;

        @SerializedName("percentage")
        private double percentage;
    }
}