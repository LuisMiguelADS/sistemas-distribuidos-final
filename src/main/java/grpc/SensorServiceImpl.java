package grpc;

import com.proto.conexaoDadosSensoriais.*;
import io.grpc.stub.StreamObserver;
import models.Cliente;
import models.DadosSensoriais;

public class SensorServiceImpl extends SensorServiceGrpc.SensorServiceImplBase {
    private Cliente clienteReceptor;

    public SensorServiceImpl(Cliente clienteReceptor) {
        this.clienteReceptor = clienteReceptor;
    }

    @Override
    public void consultarDadosLocal(LocalRequest request, StreamObserver<LocalResponse> responseObserver) {
        for (DadosSensoriais dadoSensorial : clienteReceptor.getDadosRecebidos()) {
            if (dadoSensorial.getNomeLocalSensor().equals(request.getNomeLocal())) {
                LocalResponse response = LocalResponse.newBuilder()
                        .setClock(dadoSensorial.getClock())
                        .setNomeLocal(dadoSensorial.getNomeLocalSensor())
                        .setTipoSensor(dadoSensorial.getTipoSensor().name())
                        .setDadoSensor(dadoSensorial.getDadoSensor())
                        .build();
                responseObserver.onNext(response);
            }
        }
        responseObserver.onCompleted();
    }

//    @Override
//    public void pegarClock(ClockRequest request, StreamObserver<ClockResponse> responseObserver) {
//        if (request.getMensagem().equals("Quero seu clock")) {
//            ClockResponse response = ClockResponse.newBuilder()
//                    .setClock(clienteReceptor.getClock())
//                    .build();
//            responseObserver.onNext(response);
//        }
//        responseObserver.onCompleted();
//    }
}
