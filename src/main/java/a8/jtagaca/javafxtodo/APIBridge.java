package a8.jtagaca.javafxtodo;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.json.JSONObject;

public class APIBridge {
    private static final HttpClient client = HttpClient.newBuilder().build();
    private static String baseURL = "https://javafxtodo-cfbb4-default-rtdb.firebaseio.com/";
    private static final String apikey = "hU9A7c9n9uds7UcSlkbftI5Bkn8LMjyljhevFkvB";

//we are basically sending in the desc and value using this
    //    static functions has no need to make an instance you can just call it directly like APIBridge.addItem
    public static void addItem(String selectedTab, Item item) {
        String path = URLEncoder.encode(item.getDesc(), StandardCharsets.UTF_8);
//         why is the description same as the added value is is because of the obj below?
        JSONObject obj = new JSONObject(item);
//        we content type we are going to send,
//        there is a lof of stuff to put in the header
//      why do we need to have a json here? in the uri
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + selectedTab + "/" + path + ".json?auth="+apikey)).
                header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(obj.toString())).build();


//        tying threds together is by passing in something that is from the UI thread like the observable list
        try {
            client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void getList(String selectedTab, ObservableList<Item> items) {
//         how do we know that this is running on a different thread?

//        this is running in the UI thread still but when we do async we call a new thread
        HttpRequest request = HttpRequest.newBuilder().
                uri(URI.create(baseURL + selectedTab + ".json?auth="+apikey)).header("Content-Type", "application/json").build();
//        this means that we are trying to a different thread
//        async uses a new thread
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    JSONObject obj = new JSONObject(response.body());
                    Iterator<String> keys = obj.keys();
                    while (keys.hasNext()) {
                        JSONObject jsonItem = obj.getJSONObject(keys.next());

//                       making the ui thread run this
                        Platform.runLater(() -> {
                            items.add(new Item(jsonItem.getString("desc")));
                        });
                    }
                    return response;
                });
    }

    public static void deleteItem(String selectedTab, Item selectedItem) {
        String path = URLEncoder.encode(selectedItem.getDesc(), StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + selectedTab + "/" + path + ".json?auth="+apikey)).header("Content-Type", "application/json").DELETE().build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Item deleted from database");
                    return response;
                });

    }
}
