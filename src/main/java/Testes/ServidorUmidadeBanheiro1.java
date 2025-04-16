package Testes;

import com.proto.bully.BullyRequest;
import com.proto.bully.BullyResponse;
import com.proto.bully.BullyServiceGrpc;
import grpc.BullyServiceImpl;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import models.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class ServidorUmidadeBanheiro1 {
    public static void main(String[] args) throws IOException, InterruptedException {

        // Essa Thread é reponsável por criar uma conexão via Sockets, na porta 50051
        // Para realizar o envio do dado sensorial a cada 10 segundos para o Cliente
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 50051);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                Servidor server = new Servidor("Banheiro 1", TipoSensor.UMIDADE, 0);

                while (true) {
                    server.setClock(server.getClock() + 1);
                    double[] listaDados = {20, 40, 60, 80, 100};
                    Random random = new Random();
                    DadosSensoriais dados = new DadosSensoriais(UUID.randomUUID(), server.getClock(), server.getNomeLocalSensor(), server.getSensor(), listaDados[random.nextInt(listaDados.length)], LocalDateTime.now());

                    out.writeObject(dados);
                    out.flush();
                    System.out.println("\u001B[32m" + "Dados enviado: \n" + dados + "\u001B[0m");
                    Thread.sleep(10000);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        // Lista de paramêtros de eleição de outros servidores
        ArrayList<Integer> listaParametroServidores = new ArrayList<>();
        listaParametroServidores.add(100);
        listaParametroServidores.add(200);
        listaParametroServidores.add(300);

        Bully bullyServidor = new Bully(400, listaParametroServidores, 200, false);

        // Thread para enviar dados periodicamente para o servidor líder, via gRPC
        // Caso o líder não corresponda, é iniciada uma eleição para definir o novo líder
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000);
                    int port = 0;
                    if (bullyServidor.getParametroLiderAtual() == 100) {
                        port = 50061;
                    } else if (bullyServidor.getParametroLiderAtual() == 200) {
                        port = 50062;
                    } else if (bullyServidor.getParametroLiderAtual() == 300) {
                        port = 50063;
                    } else if (bullyServidor.getParametroLiderAtual() == 400) {
                        port = 50064;
                    }
                    if (!bullyServidor.isLider()) {
                        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", port)
                                .usePlaintext()
                                .build();

                        try {
                            BullyServiceGrpc.BullyServiceBlockingStub stub = BullyServiceGrpc.newBlockingStub(channel);
                            BullyRequest request = BullyRequest.newBuilder().setMensagem("Ativo?").build();
                            BullyResponse response = stub.verificarAtividade(request);

                            if (!response.getMensagem().equals("Sim")) {
                                System.out.println("\u001B[31m" + "Líder não correspondeu como esperado" + "\u001B[0m");
                                new EleicaoBully(bullyServidor).iniciarEleicao();
                            } else {
                                System.out.println("\u001B[31m" + "\nLIDER ATIVO\n" + "\u001B[0m");
                            }
                            channel.shutdown();
                        } catch (Exception e) {
                            System.out.println("\u001B[31m" + "\nLIDER INATIVO\n" + "\u001B[0m");
                            new EleicaoBully(bullyServidor).iniciarEleicao();
                        }
                    }

                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Thread para ficar recebendo mensagens gRPC
        // No caso, só vai receber mensagens se for o líder
        new Thread(() -> {
            try {
                BullyServiceImpl grpcService = new BullyServiceImpl();
                Server grpcServer = ServerBuilder.forPort(50064)
                        .addService(grpcService)
                        .build()
                        .start();

                System.out.println("\u001B[33m" + "Servico gRPC no ar" + "\u001B[0m");

                grpcServer.awaitTermination();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // Thread para receber o comando de iniciar a eleição, via Sockets
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(60074);

                while (true) {
                    Socket socket = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String comando = reader.readLine();
                    System.out.println("\u001B[33m" + "Servico Sockets no ar" + "\u001B[0m");

                    if ("iniciar-eleicao".equalsIgnoreCase(comando)) {
                        new EleicaoBully(bullyServidor).iniciarEleicao();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                InetAddress group = InetAddress.getByName("230.0.0.0");
                int port = 50055;

                MulticastSocket multicastSocket = new MulticastSocket(port);
                multicastSocket.joinGroup(group);

                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);

                    String mensagemRecebida = new String(packet.getData(), 0, packet.getLength());

                    System.out.println("\u001B[36m" + mensagemRecebida + "\u001B[0m");

                    // Esse bloco é para destrinchar a mensagem e pegar o paramêtro do novo líder
                    if (mensagemRecebida.contains("Parametro:")) {
                        String[] linhas = mensagemRecebida.split("\n");
                        for (String linha : linhas) {
                            if (linha.startsWith("Parametro:")) {
                                int novoParametroLider = Integer.parseInt(linha.split(":")[1].trim());
                                bullyServidor.setParametroLiderAtual(novoParametroLider);
                                if (novoParametroLider != 400) {
                                    bullyServidor.setLider(false);
                                }
                                System.out.println("\u001B[33m" + "Novo lider salvo localmente: " + novoParametroLider + "\u001B[0m");
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
