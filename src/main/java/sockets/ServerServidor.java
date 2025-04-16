package sockets;

import com.proto.bully.BullyRequest;
import com.proto.bully.BullyResponse;
import com.proto.bully.BullyServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import models.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class ServerServidor {

    public static void main(String[] args) throws IOException, InterruptedException {
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 50051);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                Servidor server = new Servidor("Quarto 1", TipoSensor.TEMPERATURA, 0);

                while (true) {
                    server.setClock(server.getClock() + 1);
                    double[] listaDados = {24.1, 16.3, 18.8, 20.3, 22.50};
                    Random random = new Random();
                    DadosSensoriais dados = new DadosSensoriais(UUID.randomUUID(), server.getClock(), server.getNomeLocalSensor(), server.getSensor(), listaDados[random.nextInt(listaDados.length)], LocalDateTime.now());

                    out.writeObject(dados);
                    out.flush();
                    System.out.println("Dados enviado: " + dados);
                    Thread.sleep(10000);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        ArrayList<Integer> listaParametroServidores = new ArrayList<>();
        listaParametroServidores.add(200);
        listaParametroServidores.add(300);
        listaParametroServidores.add(400);

        Bully bullyServidor = new Bully(100, listaParametroServidores, 200, false);

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000);

                    if (!bullyServidor.isLider()) {
                        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50054)
                                .usePlaintext()
                                .build();

                        try {
                            BullyServiceGrpc.BullyServiceBlockingStub stub = BullyServiceGrpc.newBlockingStub(channel);
                            BullyRequest request = BullyRequest.newBuilder().setMensagem("Ativo?").build();
                            BullyResponse response = stub.verificarAtividade(request);

                            if (!response.getMensagem().equals("Sim")) {
                                System.out.println("Líder não correspondeu como esperado");
                                new EleicaoBully(bullyServidor).iniciarEleicao();
                            }

                        } catch (Exception e) {
                            System.out.println("Líder inativo");
                            new EleicaoBully(bullyServidor).iniciarEleicao();
                        } finally {
                            channel.shutdown();
                        }
                    }

                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(60061); // porta arbitrária só pra comandos

                while (true) {
                    Socket socket = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String comando = reader.readLine();

                    if ("iniciar-eleicao".equalsIgnoreCase(comando)) {
                        new EleicaoBully(bullyServidor).iniciarEleicao();
                    }

                    socket.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
