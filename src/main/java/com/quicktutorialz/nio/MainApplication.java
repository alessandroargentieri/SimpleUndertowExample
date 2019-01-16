package com.quicktutorialz.nio;

import com.quicktutorialz.nio.handlers.CiaoHandler;
import com.quicktutorialz.nio.handlers.DoBusinessLogicHandler;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class MainApplication {

    /*
     * Creating HttpHandlers as a method and passing it as a method reference is pretty clean.
     * This also helps reduce accidentally adding state to handlers.
     */
    public static void helloWorldHandler(HttpServerExchange exchange) {
        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Hello World!" + exchange.getRequestPath());
    }

    public static void main(String[] args) {

        int port = 8686;
        /*
         *  "localhost" will ONLY listen on local host.
         *  If you want the server reachable from the outside you need to set "0.0.0.0"
         */
        String host = "0.0.0.0";

        //Default Handler
        Undertow server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(Handlers.path().addPrefixPath("/hello", MainApplication::helloWorldHandler)
                                           .addPrefixPath("/ciao" , new CiaoHandler())
                                           .addPrefixPath("/business", new DoBusinessLogicHandler())
                                           .addPrefixPath("/", exchange -> {
                                               exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                                               exchange.getResponseSender().send("400 - Invalid request");
                                           })
                )
                .build();
        server.start();
    }

}

