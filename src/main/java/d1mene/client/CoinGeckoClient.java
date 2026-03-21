package d1mene.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

public class CoinGeckoClient implements APIClient {
    private static final String BASE_URL = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&ids=bitcoin,ethereum";

    @Override
    public String getSourceName() {
        return "CoinGecko";
    }

    @Override
    public JsonObject buildRequestAndResponse(String BASE_URL,
                                              Map<String, String> params) throws IOException {
        HttpUrl parsedUrl = HttpUrl.parse(BASE_URL);
        if (parsedUrl == null) {
            throw new IOException("Невалидный BASE_URL: " + BASE_URL);
        }

        HttpUrl.Builder query = parsedUrl.newBuilder();

        for (Map.Entry<String, String> entry: params.entrySet()) {
            query.addQueryParameter(entry.getKey(), entry.getValue());
        }

        Request request = new Request.Builder().url(query.build()).get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ошибка соединения CoinGecko: HTTP " + response.code());
            }

            String body = response.body() != null ? response.body().string() : "";
            if (body.isEmpty()) {
                throw new IOException(getSourceName() + ": пустое тело ответа");
            }

            JsonArray array = gson.fromJson(body, JsonArray.class);
            JsonObject wrapper = new JsonObject();
            wrapper.add("data", array);
            return wrapper;
        }
    }

    @Override
    public JsonObject fetchData(Map<String, String> params) throws IOException {
        return buildRequestAndResponse(BASE_URL, params);
    }
}
