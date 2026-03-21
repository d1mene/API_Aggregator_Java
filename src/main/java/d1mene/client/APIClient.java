package d1mene.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

public interface APIClient {
    OkHttpClient httpClient = new OkHttpClient();
    Gson gson = new Gson();

    String getSourceName();
    JsonObject fetchData(Map<String, String> params) throws IOException;

    default JsonObject buildRequestAndResponse(String BASE_URL,
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
                throw new IOException("Ошибка соединения Open Exchange Rates: HTTP "
                        + response.code());
            }

            String body = response.body() != null ? response.body().string() : "";
            if (body.isEmpty()) {
                throw new IOException(getSourceName() + ": пустое тело ответа");
            }

            return gson.fromJson(body, JsonObject.class);
        }
    }
}