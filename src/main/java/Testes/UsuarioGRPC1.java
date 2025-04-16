package Testes;

import com.proto.conexaoDadosSensoriais.LocalRequest;
import com.proto.conexaoDadosSensoriais.SensorServiceGrpc;
import com.proto.conexaoDadosSensoriais.SnapshotRequest;
import com.proto.conexaoDadosSensoriais.SnapshotResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UsuarioGRPC1 {

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                InetAddress group = InetAddress.getByName("230.0.0.0");
                int port = 50053;

                MulticastSocket multicastSocket = new MulticastSocket(port);
                multicastSocket.joinGroup(group);

                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);

                    String mensagemRecebida = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("\u001B[32m" + "\nMensagem recebida: \n" + mensagemRecebida + "\u001B[0m");
                    System.out.println("\u001B[34m" + "\n--- Monitoraramento de sensores ---" +
                            "\nPara consultar dados de um cômodo, digite o nome." +
                            "\nPara sair, digite 'sair'." +
                            "\n-----------------------------------");
                    System.out.print("\nComando: " + "\u001B[0m");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        interacaoMenuTerminal();
    }

    private static void interacaoMenuTerminal() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        try {
            SensorServiceGrpc.SensorServiceBlockingStub stub = SensorServiceGrpc.newBlockingStub(channel);

            while (true) {
                System.out.println("\u001B[34m" + "\n--- Monitoraramento de sensores ---" +
                        "\nPara consultar dados de um cômodo, digite o nome." +
                        "\nPara tirar um snapshot do sistema digite 'snapshot'" +
                        "\nPara sair, digite 'sair'." +
                        "\n------------------------------------");
                System.out.print("\nComando: " + "\u001B[0m");
                String input = reader.readLine().trim();

                if (input.equalsIgnoreCase("sair")) {
                    break;
                } else if (input.equalsIgnoreCase("snapshot")) {
                    SnapshotRequest snapshotRequest = SnapshotRequest.newBuilder().setMensagem(input).build();
                    SnapshotResponse resposta = stub.snapshot(snapshotRequest);
                    System.out.println("\u001B[36m" + resposta.getMensagem() + "\u001B[0m");
                } else {
                    LocalRequest localRequest = LocalRequest.newBuilder().setNomeLocal(input).build();
                    stub.consultarDadosLocal(localRequest).forEachRemaining(
                            dadosSensoriais -> System.out.println("\u001B[36m" + "Dados Sensoriais do local: \n" + dadosSensoriais + "\u001B[0m")
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        } finally {
            channel.shutdown();
        }
    }
}
