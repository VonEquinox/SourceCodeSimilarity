import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class ServerMain {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                port = DEFAULT_PORT;
            }
        }

        Path webRoot = Paths.get("Web").toAbsolutePath().normalize();
        if (!Files.isDirectory(webRoot)) {
            webRoot = Paths.get("FinalVer", "Web").toAbsolutePath().normalize();
        }
        if (!Files.isDirectory(webRoot)) {
            System.err.println("Web root not found: " + webRoot);
            return;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/similarity", new SimilarityHandler());
        server.createContext("/", new StaticFileHandler(webRoot));
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("Server running on http://localhost:" + port);
    }

    private static class SimilarityHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method Not Allowed");
                return;
            }

            String body = readBody(exchange.getRequestBody());
            String code1 = getValue(body, "code1");
            String code2 = getValue(body, "code2");

            if (code1 == null || code2 == null) {
                sendText(exchange, 400, "Invalid JSON payload");
                return;
            }

            double similarity = SimilarityCalculator.calculate(code1, code2);
            String json = String.format(Locale.US, "{\"similarity\":%.6f}", similarity);
            sendJson(exchange, 200, json);
        }
    }

    private static class StaticFileHandler implements HttpHandler {
        private final Path webRoot;

        private StaticFileHandler(Path webRoot) {
            this.webRoot = webRoot;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method Not Allowed");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if (path == null || path.isEmpty() || "/".equals(path)) {
                path = "/index.html";
            }

            Path file = webRoot.resolve(path.substring(1)).normalize();
            if (!file.startsWith(webRoot) || Files.isDirectory(file) || !Files.exists(file)) {
                sendText(exchange, 404, "Not Found");
                return;
            }

            byte[] data = Files.readAllBytes(file);
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", contentType(file));
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        }
    }

    private static String contentType(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".html")) return "text/html; charset=utf-8";
        if (name.endsWith(".js")) return "text/javascript; charset=utf-8";
        if (name.endsWith(".css")) return "text/css; charset=utf-8";
        if (name.endsWith(".json")) return "application/json; charset=utf-8";
        return "application/octet-stream";
    }

    private static String readBody(InputStream input) throws IOException {
        return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void sendText(HttpExchange exchange, int status, String text) throws IOException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private static String getValue(String json, String key) {
        if (json == null) return null;
        String pattern = "\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"";
        var matcher = Pattern.compile(pattern).matcher(json);
        if (!matcher.find()) return null;
        return unescapeJson(matcher.group(1));
    }

    private static String unescapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (i + 1 >= s.length()) {
                return null;
            }
            char esc = s.charAt(++i);
            switch (esc) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    if (i + 4 >= s.length()) return null;
                    int cp = 0;
                    for (int j = 1; j <= 4; j++) {
                        int v = Character.digit(s.charAt(i + j), 16);
                        if (v < 0) return null;
                        cp = (cp << 4) + v;
                    }
                    sb.append((char) cp);
                    i += 4;
                    break;
                default:
                    return null;
            }
        }
        return sb.toString();
    }
}
