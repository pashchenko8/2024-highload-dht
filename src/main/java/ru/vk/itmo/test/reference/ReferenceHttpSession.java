package ru.vk.itmo.test.reference;

import one.nio.http.HttpServer;
import one.nio.http.HttpSession;
import one.nio.http.Response;
import one.nio.net.Socket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ReferenceHttpSession extends HttpSession {
    public ReferenceHttpSession(Socket socket, HttpServer server) {
        super(socket, server);
    }

    public void sendResponseOrClose(Response response) {
        try {
            sendResponse(response);
        } catch (IOException e) {
            log.error("Exception while sending close connection", e);
            scheduleClose();
        }
    }

    public void sendError(Throwable e) {
        log.error("Exception during handleRequest", e);
        try {
            sendResponse(new Response(Response.INTERNAL_ERROR, Response.EMPTY));
        } catch (IOException ex) {
            log.error("Exception while sending close connection", e);
            scheduleClose();
        }
    }

    int i = 0;
    public void stream(int count) throws IOException {
        write(new QueueItem() {
            @Override
            public int write(Socket socket) throws IOException {
                i++;
                byte[] value = (i + "\n").getBytes(StandardCharsets.UTF_8);
                return socket.write(value, 0, value.length);
            }

            @Override
            public int remaining() {
                return i < count ? 1 : 0;
            }
        });
    }


    int count = 0;
    public void stream2(int count) throws IOException {
        this.count =count;
        next();
        next();
        sendResponse();


    }


    private void next() throws IOException {
        i++;
        byte[] value = (i + "\n").getBytes(StandardCharsets.UTF_8);
        write(value, 0, value.length);
    }

    @Override
    protected void processWrite() throws Exception {
        super.processWrite();
        if (i < count) {
            next();
        }


    }
}
