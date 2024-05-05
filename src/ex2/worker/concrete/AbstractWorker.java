package ex2.worker.concrete;

import ex2.core.component.CounterSearch;
import ex2.core.component.DataEvent;
import ex2.core.component.FactorySearcher;
import ex2.core.component.concrete.CounterSearchImpl;
import ex2.core.component.searcher.Searcher;
import ex2.core.component.searcher.SearcherType;
import ex2.core.component.searcher.SearcherWorker;
import ex2.core.component.searcher.factory.SimpleFactory;
import ex2.core.listener.ModelListener;
import ex2.core.listener.ViewListener;
import ex2.worker.LogicWorker;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWorker extends AbstractVerticle implements LogicWorker, SearcherWorker {

    private static final int STATUS_CODE_MIN = 200;
    private static final int STATUS_CODE_MAX = 300;
    private final List<ViewListener> viewListeners;
    private final List<ModelListener> modelListeners;
    private final CounterSearch counterSearch;
    private final FactorySearcher searcherFactory;
    private final WebClient webClient;
    private SearcherType searcherType;

    public AbstractWorker() {
        this.init(Vertx.vertx(), null);
        this.viewListeners = new ArrayList<>();
        this.modelListeners = new ArrayList<>();
        this.counterSearch = new CounterSearchImpl();
        this.searcherFactory = new SimpleFactory();
        this.webClient = WebClient.create(this.vertx);
    }

    @Override
    public void startSearch(final SearcherType searcherType, final DataEvent dataEvent) {
        this.modelListeners.forEach(listener -> listener.onStart(dataEvent));
        this.searcherType = searcherType;
        this.counterSearch.reset();
        this.searchUrl(dataEvent);
    }

    @Override
    public void stop() {
        this.webClient.close();
        this.vertx.close();
    }

    @Override
    public void addViewListener(final ViewListener viewListener) {
        this.viewListeners.add(viewListener);
    }

    @Override
    public void addModelListener(final ModelListener modelListener) {
        this.modelListeners.add(modelListener);
    }

    @Override
    public abstract void addEventUrl(final DataEvent dataEvent);

    protected void searchUrl(final DataEvent dataEvent) {
        final long startTime = System.currentTimeMillis();
        this.webClient.getAbs(dataEvent.url())
                .send(handler -> {
                    if (handler.succeeded()) {
                        final long duration = System.currentTimeMillis() - startTime;
                        final int statusCode = handler.result().statusCode();
                        if (statusCode >= STATUS_CODE_MIN && statusCode < STATUS_CODE_MAX) {
                            System.out.println(handler.result().statusMessage() + " " + dataEvent.url());
                            final Searcher searcher = this.searcherFactory.create(this.searcherType, this, dataEvent, handler.result().bodyAsString(), duration);
                            this.viewListeners.forEach(listener -> listener.onResponse(searcher));
                            this.counterSearch.increaseSendIfMaxDepth(searcher);
                            searcher.addSearchFindUrls();
                        }
                        this.counterSearch.increaseConsumeIfMaxDepth(dataEvent);

                        if (this.counterSearch.isEnd()) {
                            this.modelListeners.forEach(ModelListener::onFinish);
                            this.viewListeners.forEach(ViewListener::onFinish);
                        }
                    } else {
                        System.out.println("ERROR -> " + handler.cause().getMessage());
                        this.viewListeners.forEach(viewListener -> viewListener.onError(handler.cause().getMessage()));
                    }
                });
    }
}