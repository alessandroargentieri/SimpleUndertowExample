package com.quicktutorialz.nio.handlers;

import com.quicktutorialz.nio.model.Person;
import com.quicktutorialz.nio.utils.JsonConverter;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.xnio.Pooled;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


/*
https://stackoverflow.com/questions/22092146/undertow-how-to-do-non-blocking-io
 */
public class CiaoHandler implements HttpHandler {

    JsonConverter json = JsonConverter.getInstance();

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        ///////
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

        String requestBody = new String(bytes, Charset.forName("UTF-8") );
        Person person = (Person) json.getObjectFromJson(requestBody, Person.class);

        //////

        //exchange.getPathParameters();
        //exchange.getQueryParameters();


        exchange.getResponseHeaders()
                .put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender()
                .send(json.getJsonOf(person));

    }


}
