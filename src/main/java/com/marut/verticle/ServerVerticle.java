package com.marut.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.rx.java.RxHelper;
import rx.Observer;

/**
 * Simple web server verticle to expose the results of the Spring service bean call (routed via a verticle - see
 * SpringDemoVerticle)
 */
//@DelhiveryVerticle
public class ServerVerticle extends AbstractVerticle {

    int port;
    public ServerVerticle(int port){
        super();
        this.port = port;
    }

    @Override
    public void start() throws Exception {
        super.start();
        HttpServer server = vertx.createHttpServer();
        server.requestHandler(req -> {
            if (req.method() == HttpMethod.GET) {
                req.response().setChunked(true);

                if (req.path().equals("/products")) {
                    Observer<Message<String>> observer = new Observer<Message<String>>() {
                        @Override
                        public void onNext(Message<String> o) {
                            req.response().setStatusCode(200).write(o.body()).end();
                        }

                        @Override
                        public void onError(Throwable var1) {
                            req.response().setStatusCode(500).write(var1.getMessage().toString()).end();
                        }
                        @Override
                        public void onCompleted() {
                        }
                    };

                    Handler<AsyncResult<Message<String>>> handler = RxHelper.toFuture(observer);
                    vertx.eventBus().<String>send(TaskProducerVerticle.TASK_PULL_EVENT, "",handler);
                } else {
                    req.response().setStatusCode(200).write("Hello from vert.x").end();
                }

            } else {
                // We only support GET for now
                req.response().setStatusCode(405).end();
            }
        });

        server.listen(port);
    }
}
