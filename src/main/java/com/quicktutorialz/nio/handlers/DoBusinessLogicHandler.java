package com.quicktutorialz.nio.handlers;

import com.quicktutorialz.nio.model.Person;
import com.quicktutorialz.nio.utils.JsonConverter;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.xnio.Pooled;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/*
https://stackoverflow.com/questions/42245193/undertow-completablefuture-rxjava
 */
public class DoBusinessLogicHandler  implements HttpHandler {

    JsonConverter json = JsonConverter.getInstance();

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        /* move the request to a worker thread */
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        String requestBody = getRequestBody(exchange);

        /* FIRST ALTERNATIVE:
        you can call the business logic directly because the whole body of handleRequest() is managed reactively
         */
        Person person = (Person) json.getObjectFromJson(requestBody, Person.class);
        Person p = transform(person);
        sendResponse(exchange, json.getJsonOf(p));


        /* SECOND ALTERNATIVE: not working
        you must wrap business logic within a reactive construction (RxJava, CompletableFuture, ecc.) in order to
        have all the stack reactive
         */
        /*CompletableFuture
                .supplyAsync(()-> (Person) json.getObjectFromJson(requestBody, Person.class))
                .thenApply(p -> transform(p))
                .thenAccept(p -> sendResponse(exchange, json.getJsonOf(p)));*/

    }

    /* it extract the body of the request */
    private String getRequestBody(HttpServerExchange exchange) throws IOException {
        Pooled<ByteBuffer> pooledByteBuffer = exchange.getConnection().getBufferPool().allocate();
        ByteBuffer byteBuffer = pooledByteBuffer.getResource();
        byteBuffer.clear();
        exchange.getRequestChannel().read(byteBuffer);
        int pos = byteBuffer.position();
        byteBuffer.rewind();
        byte[] bytes = new byte[pos];
        byteBuffer.get(bytes);
        byteBuffer.clear();
        pooledByteBuffer.free();

        return new String(bytes, Charset.forName("UTF-8") );
    }

    /* it could be also a database fetch or whatever */
    private Person transform(Person p){
        if(p!=null){
            p.setTitle(p.getTitle().toUpperCase());
            p.setName(p.getName().toUpperCase());
            p.setSurname(p.getSurname().toUpperCase());
        }
        return p;
    }

    private void sendResponse(HttpServerExchange exchange, String response){
        exchange.getResponseHeaders()
                .put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender()
                .send(response);
    }


}
