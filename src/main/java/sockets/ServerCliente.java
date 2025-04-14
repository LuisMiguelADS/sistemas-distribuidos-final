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

        new Thread(() -> {
            try {
                SensorServiceImpl grpcService = new SensorServiceImpl(cliente);
                Server grpcServer = ServerBuilder.forPort(50052)
                        .addService(grpcService)
                        .build()
                        .start();

                System.out.println("Servidor gRPC no ar");

                grpcServer.awaitTermination();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

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
                        mensagem += "\nMédia do ultimo minuto" +
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

        ServerSocket serverSocket = new ServerSocket(50051);

        System.out.println("Servidor socket no ar");

        while (true) {
            Socket clienteSocket = serverSocket.accept();

            new Thread(() -> {
                try (ObjectInputStream in = new ObjectInputStream(clienteSocket.getInputStream())) {
                    while (true) {
                        DadosSensoriais dados = (DadosSensoriais) in.readObject();

                        int clockAtualizado = Math.max(dados.getClock(), cliente.getClock()) + 1;
                        cliente.setClock(clockAtualizado);
                        cliente.addDadosSensoriais(dados);

                        System.out.println("Clock Atualizado: " + clockAtualizado +
                                "\nDados recebidos: " + dados);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

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
