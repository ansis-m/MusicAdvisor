import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;

/*
Spotify username: 5t39ou35kdsvix1x84ayb3ca2
Spotify password: Eloraps1
 */

public class Main {


    private static final String port = "8000"; //when changing the port a new authorization has to be made at Spotify
    private final static String serverPath = "http://localhost:" + port;
    private final static String clientId = "e250b9f5fe2848f08f36f20b1274866a";
    private final static String clientSecret = "006e08a74dbd4d5caa1b5fdc8d247687";
    private final static String spotify = "https://accounts.spotify.com";
    private final static String defaultAPI = "https://api.spotify.com";
    private static String path;
    private static String APIpath;
    private static String url;
    private static int size = 5;

    private final static String clientIdSecret = clientId + ":" + clientSecret;
    private static String code = "";
    private static String accessToken = "";

    public static void main(String[] args){

        new Pages(size);
        getServerPath(args);
        HttpServer server = startServer();
        authorize(server);
        Scanner scanner = new Scanner(System.in);
        while(true) {
            printPrompt();
            String input = scanner.nextLine();
            switch (input) {
                case ("exit"):
                    handleExit(scanner);
                case ("new"):
                    handleNew(scanner);
                    break;
                case ("featured"):
                    handleFeatured(scanner);
                    break;
                case ("categories"):
                    handleCategories(scanner);
                    break;
                default:
                    if(input.startsWith("playlists")) {
                        handlePlaylists(input, scanner);
                    }
                    else {
                            System.out.println("\nBad input. Try again!\n\n");
                    }
                    break;
            }
        }
    }

    private static void handlePlaylists(String i, Scanner scanner) {
        try {
            if (getPlaylist(i)){
                Pages.displayNext();
                while (viewPages(scanner)) {
                    continue;
                }
            }
            else {
                System.out.println("Playlist not found. See 'Categories' for available playlists!\n");
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void handleCategories(Scanner scanner) {

        HttpResponse<String> response;
        JsonObject jo;

        try {
            response = getResponse("/v1/browse/categories");
            jo = JsonParser.parseString(response.body()).getAsJsonObject();
            Pages.clear();
            for (JsonElement j : jo.getAsJsonObject("categories").getAsJsonArray("items")) {
                Pages.addOutput(j.getAsJsonObject().get("name").getAsString());
            }
            Pages.displayNext();
            while (viewPages(scanner)) {
                continue;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleFeatured(Scanner scanner) {

        HttpResponse<String> response;
        JsonObject jo;

        try {
            response = getResponse("/v1/browse/featured-playlists");
            jo = JsonParser.parseString(response.body()).getAsJsonObject();
            Pages.clear();
            //System.out.println(jo);
            for (JsonElement j : jo.getAsJsonObject("playlists").getAsJsonArray("items")) {
                if(j.isJsonObject()){
                    Pages.addOutput(j.getAsJsonObject().get("name").getAsString() + "\n" + j.getAsJsonObject().getAsJsonObject("external_urls").get("spotify").getAsString() + "\n");
                }
            }
            Pages.displayNext();
            while (viewPages(scanner)) {
                continue;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleNew(Scanner scanner) {

        HttpResponse<String> response;
        JsonObject jo;
        try {
            response = getResponse("/v1/browse/new-releases");
            jo = JsonParser.parseString(response.body()).getAsJsonObject();
            Pages.clear();

            for (JsonElement j : jo.getAsJsonObject("albums").getAsJsonArray("items")) {
                StringBuilder builder = new StringBuilder();
                if(j.isJsonObject()){
                    builder.append(j.getAsJsonObject().get("name").getAsString()).append("\n[");
                    boolean first = true;
                    for(JsonElement k : j.getAsJsonObject().getAsJsonArray("artists")){
                        if(k.isJsonObject()) {
                            if(!first)
                                builder.append(", ");
                            builder.append(k.getAsJsonObject().get("name").getAsString());
                            first = false;
                        }
                    }
                    builder.append("]\n");
                    builder.append(j.getAsJsonObject().getAsJsonObject("external_urls").get("spotify").getAsString()).append("\n");
                    Pages.addOutput(builder.toString());
                }
            }
            Pages.displayNext();
            while (viewPages(scanner)) {
                continue;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleExit(Scanner scanner) {
        scanner.close();
        System.out.println("---GOODBYE!---");
        System.exit(0);
    }

    private static void printPrompt() {
        System.out.println("\nEnter:\t'exit' to exit");
        System.out.println("\t\t'new' to view new releases");
        System.out.println("\t\t'featured' to view featured playlists");
        System.out.println("\t\t'categories' to view categories of playlists");
        System.out.println("\t\t'playlists ' + the name of the category to view playlists in that category\n");
    }


    private static boolean viewPages(Scanner scanner){

        System.out.println("\nEnter:\t'exit' to return to main menu");
        System.out.println("\t\t'next' to view next page");
        System.out.println("\t\t'prev' to view previous page");
        String input = scanner.nextLine();
        if (input.equals("exit"))
            return false;
        else if (input.equals("next"))
            Pages.displayNext();
        else if (input.equals("prev"))
            Pages.displayPrev();
        return true;
    }

    private static boolean getPlaylist(String i) {

        String id = null;
        HttpResponse<String> response;

        try {
            String name = i.split(" ", 2)[1];
            response = getResponse("/v1/browse/categories");
            JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject();

            for (JsonElement j : jo.getAsJsonObject("categories").getAsJsonArray("items")) {

                if (j.getAsJsonObject().get("name").getAsString().equals(name)) {
                    id = j.getAsJsonObject().get("id").getAsString();
                    break;
                }
            }
            if(id == null) {
                System.out.println("\nUnknown category name.");
                return false;
            }
            else {
                response = getResponse("/v1/browse/categories/" + id + "/playlists");

                if(response.statusCode() == 200) {
                    jo = JsonParser.parseString(response.body()).getAsJsonObject();
                    if(jo.has("error")) {
                        System.out.println("ERROR: " + jo.get("error"));
                    }
                    else {
                        Pages.clear();
                        for (JsonElement j : jo.getAsJsonObject("playlists").getAsJsonArray("items")) {
                            if (j.isJsonObject()) {
                                Pages.addOutput(j.getAsJsonObject().get("name").getAsString() + "\n" + j.getAsJsonObject().getAsJsonObject("external_urls").get("spotify").getAsString() + "\n");
                            }
                        }
                        return true;
                    }
                }
                else {
                    System.out.println("bad status code");
                    System.out.println(response.headers());
                    System.out.println(response.body());
                }
            }
        }
        catch (Exception e) {
            System.out.println("Unknown playlist name.\nTry again!\n");
        }
        return false;
    }

    private static HttpResponse getResponse(String api) {

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .headers("Content-Type", "application/json", "Authorization", "Bearer " + accessToken)
                .uri(URI.create(APIpath + api))
                .GET()
                .build();
        try{
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception e) {
            return null;
        }
    }

    private static void getServerPath(String[] args) {

        boolean api = true;
        boolean pth = true;
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-access") && i < args.length - 1) {
                //System.out.println("args: " + args[i + 1]);
                path = args[i + 1] + "/api/token";
                url = args[i + 1] + "/authorize?client_id=" + clientId + "&redirect_uri=" + serverPath + "&response_type=code";
                pth = false;
            }
            if(args[i].equals("-resource") && i < args.length - 1) {
                APIpath = args[i + 1];
                api = false;
            }
            if(args[i].equals("-page") && i < args.length - 1) {
                try {
                    size = Integer.parseInt(args[i + 1]);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if(pth) {
            path = spotify + "/api/token";
            url = spotify + "/authorize?client_id=" + clientId + "&redirect_uri=" + serverPath + "&response_type=code";
        }
        if(api)
            APIpath = defaultAPI;
    }


    private static void authorize(HttpServer server) {

        try{
            System.out.println("\n\nUse this link to login into Spotify and authorize the server!");
            System.out.println(url);
            System.out.println("waiting for code...");
            while(code.equals("")) {
                Thread.sleep(100);
            }
            server.stop(0);
            getToken();
        }
        catch (Exception e) {
            System.out.println("Exception occurred while trying to authorize!");
            System.out.println("Try restarting the program!");
            System.exit(1);
        }
        server.stop(0);
        System.out.println("\n--- LOGIN SUCCESS ---\n");
    }

    private static void getToken() throws IOException, InterruptedException {

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .headers("Content-Type", "application/x-www-form-urlencoded", "Authorization", "Basic " + Base64.getEncoder().encodeToString(clientIdSecret.getBytes()))
                .uri(URI.create(path))
                .POST(HttpRequest.BodyPublishers.ofString("client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&grant_type=authorization_code" +
                        "&code=" + code.split("=")[1] +
                        "&redirect_uri=" + serverPath))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.readValue(response.body(), Map.class);
        accessToken = map.get("access_token");
//        System.out.println("response:");
//        System.out.println(response.body());
    }

    private static HttpServer startServer(){

        try{
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(Integer.parseInt(Main.port)), 0);
            server.createContext("/",
                    new HttpHandler() {
                        public void handle(HttpExchange exchange) throws IOException {
                            String response = exchange.getRequestURI().getQuery();
                            if(response != null && response.startsWith("code=")) {
                                code = response;
                                exchange.sendResponseHeaders(200, "Got the code. Return back to your program.".length());
                                exchange.getResponseBody().write("Got the code. Return back to your program.".getBytes());
                                exchange.getResponseBody().close();
                                //System.out.println("code received at controller: " + code);
                            }
                            else {
                                exchange.sendResponseHeaders(401, "Authorization code not found. Try again.".length());
                                exchange.getResponseBody().write("Authorization code not found. Try again.".getBytes());
                                exchange.getResponseBody().close();
                            }
                        }
                    }
            );
            server.start();
            return server;
        }
        catch (Exception e) {
            System.out.println("\n\n***Exception when starting the server***\n\n");
            e.printStackTrace();
            System.out.println("\n*******************************\n\n");
        }
        return null;
    }
}
