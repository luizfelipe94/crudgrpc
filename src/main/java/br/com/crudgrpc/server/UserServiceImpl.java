package br.com.crudgrpc.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.proto.user.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("grpc_db");
    private MongoCollection<Document> collection = database.getCollection("users");

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {
        System.out.println("Creating User");
        User user = request.getUser();
        Document doc = new Document("name", user.getName())
                .append("email", user.getEmail());
        collection.insertOne(doc);
        String userID = doc.getObjectId("_id").toString();
        System.out.println("Inserted user: " + userID);
        CreateUserResponse response = CreateUserResponse.newBuilder()
                .setUser(user.toBuilder().setId(userID).build())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        String userID = request.getUserId();
        DeleteResult result = null;
        try{
            result = collection.deleteOne(eq("_id", new ObjectId(userID)));
        }catch (Exception e){
            System.out.println("User not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                    .withDescription("User not found for id: " + userID)
                    .augmentDescription(e.getLocalizedMessage())
                    .asRuntimeException()
            );
        }
        if(result.getDeletedCount() == 0){
            System.out.println("User not found for id: " + userID);
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("User not found for id: " + userID)
                            .asRuntimeException()
            );
        }else{
            System.out.println("User was deleted");
            responseObserver.onNext(
                    DeleteUserResponse.newBuilder()
                    .setUserId(userID)
                    .build()
            );
            responseObserver.onCompleted();
        }
    }

    @Override
    public void listUser(ListUserRequest request, StreamObserver<ListUserResponse> responseObserver) {
        System.out.println("Streaming users");
        collection.find().iterator().forEachRemaining(document -> responseObserver.onNext(
                ListUserResponse.newBuilder()
                .setUser(this.documentToUser(document))
                .build()
        ));
        responseObserver.onCompleted();
    }

    private User documentToUser(Document document){
        return User.newBuilder()
                .setName(document.getString("name"))
                .setEmail(document.getString("email"))
                .build();
    }
}
