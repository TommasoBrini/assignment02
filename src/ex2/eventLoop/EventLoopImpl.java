package ex2.eventLoop;

import ex2.gui.ViewListener;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

import java.util.ArrayList;
import java.util.List;

public class EventLoopImpl extends AbstractVerticle implements EventLoop {
    private final List<ViewListener> viewListeners;

    public EventLoopImpl() {
        this.init(Vertx.vertx(), null);
        this.viewListeners = new ArrayList<>();
    }

    @Override
    public void stop() {
        this.vertx.close();
    }

    @Override
    public void addEvent(final Runnable runnable) {
        this.vertx.eventBus().consumer("event", message -> {
            System.out.println("Received message: " + message.body());
        });
        // add event in bus
        this.vertx.eventBus().send("event", "/hello");
    }

    @Override
    public void createServer() {
        this.vertx.createHttpServer()
                .requestHandler(request -> {
                    // Gestione della richiesta GET
                    if ("/hello".equals(request.path())) {
                        // Invia una risposta con un messaggio di testo

                        System.out.println("Received request");
                        request.response()
                                .putHeader("content-type", "text/plain")
                                .end("Hello from Vert.x!");
                    } else {
                        // Risposta per altre richieste
                        request.response()
                                .setStatusCode(404)
                                .end("Not Found");
                    }
                })
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        System.out.println("Server started on port 8080");
                    } else {
                        System.err.println("Failed to start server");
                    }
                });
    }

    @Override
    public void addViewListener(final ViewListener viewListener) {
        this.viewListeners.add(viewListener);
    }

    @Override
    public void requestWebClient(final String url) {
        final WebClient webClient = WebClient.create(this.vertx);
        webClient.getAbs(url)
                .send(handler -> {
                    if (handler.succeeded()) {
                        // Gestione della risposta ricevuta
                        System.out.println("Response status code: " + handler.result().statusCode());
                        System.out.println("Response body:");
                        System.out.println(handler.result().body());
                        this.viewListeners.forEach(viewListener -> viewListener.onResponse(handler.result().bodyAsString()));
                    } else {
                        // Gestione degli errori
                        this.viewListeners.forEach(viewListener -> viewListener.onError(handler.cause().getMessage()));
                        System.err.println("Request failed: " + handler.cause().getMessage());
                    }
                });
    }

    public static void main(final String[] args) {
        final EventLoopImpl eventLoop = new EventLoopImpl();
//        eventLoop.createServer();

        try {
            Thread.sleep(1000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        eventLoop.requestWebClient("https://en.wikipedia.org/wiki/Ubaldo_Ricci");
//        eventLoop.addEvent(null);

    }
}
