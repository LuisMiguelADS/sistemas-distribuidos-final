package sockets;

import grpc.SensorServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import models.Cliente;
import models.DadosSensoriais;
import models.TipoSensor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ServerCliente {

    public static void main(String[] args) throws IOException {
        Cliente cliente = new Cliente();

        // Essa Thread realiza uma conexão via gRPC na porta 50052
        // O Usuário pode realizar solicitações para o Cliente em busca de dados personalizados
        new Thread(() -> {
            try {
                SensorServiceImpl grpcService = new SensorServiceImpl(cliente);
                Server grpcServer = ServerBuilder.forPort(50052)
                        .addService(grpcService)
                        .build()
                        .start();

                System.out.println("\u001B[33m" + "Servico gRPC para requisições do usuário no ar" + "\u001B[0m");

                grpcServer.awaitTermination();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // Essa Thread realiza uma conexão via gRPC na porta 50057
        // Os servidores vão pegar o clock do cliente para sincronização
//        new Thread(() -> {
//            try {
//                SensorServiceImpl grpcService = new SensorServiceImpl(cliente);
//                Server grpcServer = ServerBuilder.forPort(50057)
//                        .addService(grpcService)
//                        .build()
//                        .start();
//
//                System.out.println("\u001B[33m" + "Servico gRPC para o clock no ar" + "\u001B[0m");
//
//                grpcServer.awaitTermination();
//            } catch (IOException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();

        // Essa Thread realiza uma conexão Multicast na porta 50053 e no endereço 230.0.0.0
        // Periodicamente a cada minuto o Cliente envia uma visão geral dos dados do último minuto para os Usuários
        new Thread(() -> {
            try {
                InetAddress group = InetAddress.getByName("230.0.0.0");
                int port = 50053;

                MulticastSocket multicastSocket = new MulticastSocket(port);

                while (true) {
                    Set<String> servidores = new HashSet<>();
                    for (DadosSensoriais dados : cliente.getDadosRecebidos()) {
                        servidores.add(dados.getNomeLocalSensor());
                    }

                    String mensagem = "";

                    for (String servidor : servidores) {
                        TipoSensor tipoSensor = null;
                        double media = mediaUltimoMinuto(cliente.getDadosRecebidos(), servidor);
                        for (DadosSensoriais dados : cliente.getDadosRecebidos()) {
                            if (dados.getNomeLocalSensor().equals(servidor)) {
                                tipoSensor = dados.getTipoSensor();
                            }
                        }
                        mensagem += "\nMedia do ultimo minuto" +
                                "\nLOCAL: " + servidor +
                                "\n" + tipoSensor + ": " + media;
                    }

                    byte[] buffer = mensagem.getBytes();

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
                    multicastSocket.send(packet);
                    Thread.sleep(60000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Cria uma conexão via Socket na porta 50051
        // Para receber os dados enviados pelos servidores
        ServerSocket serverSocket = new ServerSocket(50051);
        System.out.println("\u001B[33m" + "Servico Socket no ar" + "\u001B[0m");
        while (true) {
            Socket clienteSocket = serverSocket.accept();

            new Thread(() -> {
                try (ObjectInputStream in = new ObjectInputStream(clienteSocket.getInputStream())) {
                    while (true) {
                        DadosSensoriais dados = (DadosSensoriais) in.readObject();

                        int clockAtualizado = Math.max(dados.getClock(), cliente.getClock()) + 1;
                        cliente.setClock(clockAtualizado);
                        cliente.addDadosSensoriais(dados);

                        System.out.println("\u001B[33m" + "Clock Atualizado: " + clockAtualizado + "\u001B[0m" +
                                "\u001B[32m" + "\nDados recebidos: \n" + dados + "\u001B[0m");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    // Função para calcular as média dos dados enviados no último minuto, particular de cada servidor
    public static double mediaUltimoMinuto(ArrayList<DadosSensoriais> dadosSensoriais, String nomeLocalSensor) {
        LocalDateTime umMinutosAtras = LocalDateTime.now().minusMinutes(1);
        ArrayList<DadosSensoriais> dadosUltimaHora = new ArrayList<>();
        double media = 0.0;

        for (DadosSensoriais dados : dadosSensoriais) {
            if (dados.getDataHora().isAfter(umMinutosAtras) && dados.getNomeLocalSensor().equals(nomeLocalSensor)) {
                dadosUltimaHora.add(dados);
            }
        }

        for (DadosSensoriais dados : dadosUltimaHora) {
            media += dados.getDadoSensor();
        }

        media = media / dadosUltimaHora.size();

        return media;
    }
}
