package com.example.app;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class App {
    private static final long POLL_INTERVAL = 5000; // 5 seconds
    private static final double BASE_PRICE = 35000.0; // Starting mock price
    private final List<PriceEntry> priceHistory = new ArrayList<>();
    private final Random random = new Random();
    private double currentPrice = BASE_PRICE;
    private final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        new App().start();
    }

    private void start() throws IOException {
        // Start HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/data", exchange -> {
            String response = gson.toJson(priceHistory);
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8000. Access /data for JSON feed");

        // Start price updates
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::generateMockPrice, 0, POLL_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void generateMockPrice() {
        // Generate small random price fluctuations
        double change = (random.nextDouble() - 0.5) * 100; // +/- $50
        currentPrice += change;
        
        // Ensure price stays reasonable
        currentPrice = Math.max(30000, Math.min(40000, currentPrice));
        
        LocalDateTime timestamp = LocalDateTime.now();
        priceHistory.add(new PriceEntry(currentPrice, timestamp));
        
        System.out.printf("Mock price: %.2f at %s%n", currentPrice, timestamp);
    }

    private record PriceEntry(double price, LocalDateTime timestamp) {}
}