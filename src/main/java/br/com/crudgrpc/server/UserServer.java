package br.com.crudgrpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class UserServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Server start");
        Server server = ServerBuilder.forPort(50051)
                .addService(new UserServiceImpl())
                .build();
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
        }));
        server.awaitTermination();
    }
}
