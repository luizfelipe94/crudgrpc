package client;

import com.proto.user.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class UserClient {

    public static void main(String[] args) {
        UserClient main = new UserClient();
        main.run();
    }

    private void run(){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        UserServiceGrpc.UserServiceBlockingStub userClient = UserServiceGrpc.newBlockingStub(channel);

        // CREATE USER
        User user = User.newBuilder()
                .setName("luiz")
                .setEmail("teste@teste.com")
                .build();
        CreateUserResponse createUserResponse = userClient.createUser(
                CreateUserRequest.newBuilder()
                        .setUser(user)
                        .build()
        );
        System.out.println(createUserResponse.toString());

        // DELETE USER
        String userId = createUserResponse.getUser().getId();
        DeleteUserResponse deleteUserResponse = userClient.deleteUser(
                DeleteUserRequest.newBuilder().setUserId(userId).build()
        );
        System.out.println(deleteUserResponse.toString());

        // LIST USERS
        userClient.listUser(ListUserRequest.newBuilder().build()).forEachRemaining(
                listUserResponse -> System.out.println(listUserResponse.getUser().toString())
        );




    }

}
