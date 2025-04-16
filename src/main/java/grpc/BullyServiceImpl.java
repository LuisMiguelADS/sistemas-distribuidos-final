package grpc;

import com.proto.bully.BullyRequest;
import com.proto.bully.BullyResponse;
import com.proto.bully.BullyServiceGrpc;
import io.grpc.stub.StreamObserver;

public class BullyServiceImpl extends BullyServiceGrpc.BullyServiceImplBase {

    @Override
    public void verificarAtividade(BullyRequest request, StreamObserver<BullyResponse> responseObserver) {
        if ("Ativo?".equals(request.getMensagem())) {
            BullyResponse response = BullyResponse.newBuilder()
                    .setMensagem("Sim")
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }
}
