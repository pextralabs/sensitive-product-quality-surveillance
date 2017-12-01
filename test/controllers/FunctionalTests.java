package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Application;
import play.Environment;
import play.Mode;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient;
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClientConfig;
import play.shaded.ahc.org.asynchttpclient.DefaultAsyncHttpClient;
import play.shaded.ahc.org.asynchttpclient.DefaultAsyncHttpClientConfig;
import play.shaded.ahc.org.asynchttpclient.ws.WebSocket;
import org.junit.Test;
import play.test.Helpers;
import play.test.TestServer;
import utils.WebSocketClient;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static junit.framework.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.*;
import static play.test.Helpers.*;

public class FunctionalTests {

    private JsonNode registerEntity(String name, String type) {
        ObjectNode registerEntity = Json.newObject();
        registerEntity.put("name", name);
        registerEntity.put("type", type);
        return registerEntity;
    }

    private JsonNode registerSensor(String label, Double value) {
        ObjectNode registerSensor = Json.newObject();
        registerSensor.put("label", label);
        registerSensor.put("initValue", value);
        return registerSensor;
    }

    private JsonNode updateSensor(Double value) {
        ObjectNode updateSensor = Json.newObject();
        updateSensor.put("value", value);
        return updateSensor;
    }
    /*@Test
    public void testRejectWebSocket() {
        TestServer server = testServer(37117);
        running(server, () -> {
            try {
                AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder().setMaxRequestRetry(0).build();
                AsyncHttpClient client = new DefaultAsyncHttpClient(config);
                WebSocketClient webSocketClient = new WebSocketClient(client);

                try {
                    String serverURL = "ws://localhost:37117/ws";
                    WebSocketClient.LoggingListener listener = new WebSocketClient.LoggingListener(message -> {});
                    CompletableFuture<WebSocket> completionStage = webSocketClient.call(serverURL, listener);
                    await().until(completionStage::isDone);
                    assertThat(completionStage)
                            .hasFailedWithThrowableThat()
                            .hasMessageContaining("Invalid Status Code 403");
                } finally {
                    client.close();
                }
            } catch (Exception e) {
                fail("Unexpected exception", e);
            }
        });
    }*/

    @Test
    public void testAcceptWebSocket() {
        Map<String, ? extends Object> conf = inMemoryDatabase("default");
        Application app = fakeApplication(inMemoryDatabase());
        //Map<String, Object> conf = (Map<String, Object>) inMemoryDatabase();
        //new GuiceApplicationBuilder().in(new Environment(Mode.DEV)).configure(   ).build();
        TestServer server = testServer(19001, app );
        running(server, () -> {
            try {
                AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder().setMaxRequestRetry(0).build();
                AsyncHttpClient client = new DefaultAsyncHttpClient(config);
                WebSocketClient webSocketClient = new WebSocketClient(client);
                try {
                    String serverURL = "ws://localhost:19001/subscribe";
                    ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
                    WebSocketClient.LoggingListener listener = new WebSocketClient.LoggingListener((message) -> {
                        try {
                            queue.put(message);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    CompletableFuture<WebSocket> completionStage = webSocketClient.call(serverURL, listener);

                    WSClient ws = play.test.WSTestClient.newClient(server.port());

                    ws.url("http://localhost:19001/entities").post(registerEntity("Isaac", "Person"))
                            .thenCompose(resp -> ws.url("http://localhost:19001/entities/" + resp.asJson().path("id")).post(registerSensor("temperature", 36.5)))
                            .thenCompose(resp -> {
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                JsonNode sensorNode = resp.asJson();
                                return ws.url("http://localhost:19001/entities/" + sensorNode.path("bearer").path("id").asInt() + "/sensors/" + sensorNode.path("id").asInt() + "/update")
                                        .post(updateSensor(37.6));
                            })
                            .thenCompose(resp -> {
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                JsonNode sensorNode = resp.asJson();
                                return ws.url("http://localhost:19001/entities/" + sensorNode.path("bearer").path("id").asInt() + "/sensors/" + sensorNode.path("id").asInt() + "/update")
                                        .post(updateSensor(36.6));
                            });

                    await().until(completionStage::isDone);
                    WebSocket websocket = completionStage.get();
                    await().until(() -> websocket.isOpen() && queue.size() == 2 );
                    String input = queue.take();

                    JsonNode json = Json.parse(input);
                    //assertThat(Collections.singletonList(symbol)).isSubsetOf("AAPL", "GOOG", "ORCL");
                } finally {
                    client.close();
                }
            } catch (Exception e) {
                fail("Unexpected exception", e);
            }
        });
    }
}