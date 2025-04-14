package Testes;

import com.proto.conexaoDadosSensoriais.LocalRequest;
import com.proto.conexaoDadosSensoriais.SensorServiceGrpc;
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

                byte[] buffer = new byte[1024];

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    multicastSocket.receive(packet);

                    String received = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("\nMensagem recebida: \n" + received);
                    System.out.println("\n--- Monitoraramento de sensores ---" +
                            "\nPara consultar dados de um cômodo, digite o nome." +
                            "\nPara sair, digite 'sair'." +
                            "\n-----------------------------------");
                    System.out.print("\nComando: ");
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
                System.out.println("\n--- Monitoraramento de sensores ---" +
                        "\nPara consultar dados de um cômodo, digite o nome." +
                        "\nPara sair, digite 'sair'." +
                        "\n------------------------------------");
                System.out.print("\nComando: ");
                String input = reader.readLine().trim();

                if (input.equalsIgnoreCase("sair")) {
                    break;
                } else {
                    LocalRequest localRequest = LocalRequest.newBuilder().setNomeLocal(input).build();
                    stub.consultarDadosLocal(localRequest).forEachRemaining(
                            dadosSensoriais -> System.out.println("Dados Sensoriais do local: \n" + dadosSensoriais)
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
