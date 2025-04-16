package models;

import com.proto.bully.BullyRequest;
import com.proto.bully.BullyResponse;
import com.proto.bully.BullyServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

public class EleicaoBully {

    private Bully bully;

    public EleicaoBully(Bully bully) {
        this.bully = bully;
    }

    public void iniciarEleicao() throws IOException {
        System.out.println("Iniciando Eleicao");
        boolean servidorParametroMaior = false;

        // Navega a lista de paramêtros de eleição dos servidores
        // Caso tenha algum servidor com paramêtro maior do que o atual e ativo, solicita que ele inicie uma eleição tbm
        for (int parametro : bully.getListaParametroEleicao()) {
            if (parametro > bully.getParametroEleicao()) {
                int port = 0;
                // Como a conexão é via gRPC, cada servidor tem sua porta, a partir do paramêtro será possível saber a porta do servidor
                if (parametro == 100) {
                    port = 50061;
                } else if (parametro == 200) {
                    port = 50062;
                } else if (parametro == 300) {
                    port = 50063;
                } else if (parametro == 400) {
                    port = 50064;
                }
                // Canal de conexão do gRPC
                ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", port)
                        .usePlaintext()
                        .build();
                try {
                    BullyServiceGrpc.BullyServiceBlockingStub stub = BullyServiceGrpc.newBlockingStub(channel);
                    // Constrói a requisição gRPC
                    BullyRequest bullyRequest = BullyRequest.newBuilder().setMensagem("Ativo?").build();
                    // Envia a requisição e salva a resposta
                    BullyResponse response = stub.verificarAtividade(bullyRequest);
                    // Verifica a resposta, se estiver ativo executa o bloco
                    if (response.getMensagem().equals("Sim")) {
                        servidorParametroMaior = true;
                        // Como a conexão é via socket, cada servidor tem sua porta, a partir do paramêtro será possível saber a porta do servidor
                        if (parametro == 100) {
                            port = 50071;
                        } else if (parametro == 200) {
                            port = 50072;
                        } else if (parametro == 300) {
                            port = 50073;
                        } else if (parametro == 400) {
                            port = 50074;
                        }
                        // Inicia a conexão via socket com o servidor
                        try (Socket socket = new Socket("localhost", port)) {
                            // Envia a mensagem "iniciar-eleicao", para o servidor com paramêtro maior iniciar a eleição
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                            writer.write("iniciar-eleicao\n");
                            writer.flush();
                        } catch (IOException ex) {
                            System.out.println("\u001B[31m" + "Nao foi possivel entrar em contato com o servidor" + "\u001B[0m");
                        }
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("\u001B[31m" + "Nao foi entrar em contato com o servidor" + "\u001B[0m");
                } finally {
                    channel.shutdown();
                }
            }
        }

        // Caso não tenha outro servidor com paramêtro maior
        // Será enviada uma mensagem via multcast para os outros servidores
        // Para anunciar o novo lider e consequentemente os servidores salvarem essa informação
        if (!servidorParametroMaior) {
            InetAddress group = InetAddress.getByName("230.0.0.0");
            int port = 50055;
            MulticastSocket multicastSocket = new MulticastSocket();
            bully.setParametroLiderAtual(bully.getParametroEleicao());
            bully.setLider(true);

            String mensagem = "Novo lider eleito\n" +
                    "Parametro: " + bully.getParametroEleicao();

            byte[] buffer = mensagem.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
            multicastSocket.send(packet);
        }
    }
}
