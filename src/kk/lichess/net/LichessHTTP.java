package kk.lichess.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

public class LichessHTTP {
    private final ObjectMapper mapper = new JsonMapper();
    public static final String LICHESS = "https://lichess.org/";

    public class LichessResponse {
        private final int statusCode;
        private final String content;

        private LichessResponse(int statusCode, String content) {
            this.statusCode = statusCode;
            this.content = content;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String toString() {
            return content;
        }

        public JsonNode toJson() throws JsonProcessingException {
            return mapper.readTree(content);
        }

        public <T> T mapJsonTo(Class<T> dataType) throws JsonProcessingException {
            return mapper.readValue(content, dataType);
        }

    }
    private final String authToken;

    public LichessHTTP(String authToken) {
        this.authToken = authToken;
    }

    public LichessResponse get(String path) {
        HttpResponse<String> response = Unirest.get(LICHESS + path)
                .header("Authorization", authToken)
                .asString();
        return new LichessResponse(response.getStatus(), response.getBody());
    }

    public LichessResponse post(String path) {
        HttpResponse<String> response = Unirest.post(LICHESS + path)
                .header("Authorization", authToken)
                .asString();
        return new LichessResponse(response.getStatus(), response.getBody());
    }

    private InputStream openStream(String url) throws IOException {
        HttpURLConnection urlConnection =
                (HttpURLConnection) new URL(url).openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Authorization", authToken);
        return urlConnection.getInputStream();
    }


    public JsonStream stream(String path, Consumer<String> jsonConsumer) {
        return new JsonStream(() -> openStream(path), jsonConsumer);
    }
}
