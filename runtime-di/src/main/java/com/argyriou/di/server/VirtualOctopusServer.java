package com.argyriou.di.server;

import com.argyriou.di.beans.Bean1;
import com.argyriou.di.context.Context;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class VirtualOctopusServer {
    private final HttpServer server;
    private final Context context;

    public VirtualOctopusServer(
            final int port,
            final String initPath) throws IOException {
        this.server = HttpServer.create(
                new InetSocketAddress(port), 0
        );
        this.context = new Context();

        server.createContext(initPath, BasePathHandler.getNew(context));
    }

    public void start() {
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
    }

    static class BasePathHandler
            implements HttpHandler {
        private final Context context;

        BasePathHandler(Context context) {
            this.context = context;
        }

        public static HttpHandler getNew(Context context) {
            return new BasePathHandler(context);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = context.getBeanBucket().get(Bean1.class).check();
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
