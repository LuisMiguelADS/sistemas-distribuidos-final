package grpc;

import com.proto.conexaoDadosSensoriais.*;
import io.grpc.stub.StreamObserver;
import models.Cliente;
import models.DadosSensoriais;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    @Override
    public void snapshot(SnapshotRequest request, StreamObserver<SnapshotResponse> responseObserver) {
        Set<String> listaServidores = new HashSet<>();
        Map<String, DadosSensoriais> ultimosDados = new HashMap<>();

        for (DadosSensoriais dadoSensorial : clienteReceptor.getDadosRecebidos()) {
            ultimosDados.put(dadoSensorial.getNomeLocalSensor(), dadoSensorial);
            listaServidores.add(dadoSensorial.getNomeLocalSensor());
        }

        String mensagemResponse = "";
        for (String servidor : listaServidores) {
            DadosSensoriais dado = ultimosDados.get(servidor);
            mensagemResponse += "\nServidor: " + servidor + "" +
                    "\nClock: " + dado.getClock();
        }

        mensagemResponse += "\nClock do Cliente (Receptor de todos os dados sensoriais): " + clienteReceptor.getClock();

        SnapshotResponse response = SnapshotResponse.newBuilder()
                    .setMensagem(mensagemResponse)
                    .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
