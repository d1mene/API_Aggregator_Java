package d1mene.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractAPIClient implements APIClient {

    protected final String baseUrl;
    protected final OkHttpClient httpClient;
    protected final Gson gson;

    protected AbstractAPIClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    protected JsonElement buildRequestAndExecute(Map<String, String> params) throws IOException {
        HttpUrl parsedUrl = HttpUrl.parse(baseUrl);
        if (parsedUrl == null) {
            throw new IOException("Невалидный URL: " + baseUrl);
        }

        HttpUrl.Builder urlBuilder = parsedUrl.newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }

        Request request = new Request.Builder().url(urlBuilder.build()).get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(getSourceName() + " ошибочка: HTTP " + response.code());
            }
            String body = response.body() != null ? response.body().string() : "";
            if (body.isEmpty()) {
                throw new IOException(getSourceName() + ": пустое тело ответа");
            }
            return gson.fromJson(body, JsonElement.class);
        }
    }
}