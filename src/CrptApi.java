import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final Object lock = new Object();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
    }

    public void createDocument(DocumentObject document, String signature) {
        synchronized (lock) {
            if (requestCount.get() >= requestLimit) {
                try {
                    lock.wait(timeUnit.toMillis(1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                requestCount.set(0);
            }
            requestCount.incrementAndGet();
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .POST(HttpRequest.BodyPublishers.ofString(convertDocumentToJson(document)))
                .build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Код ответа: " + response.statusCode());
            System.out.println("Тело ответа: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String convertDocumentToJson(DocumentObject document) {
        return "";
    }

    public static class DocumentObject {
    }
}
