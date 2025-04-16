import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import com.proto.bully.BullyRequest;
import com.proto.bully.BullyResponse;
import com.proto.bully.BullyServiceGrpc;

import grpc.BullyServiceImpl;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import models.Bully;
import models.DadosSensoriais;
import models.EleicaoBully;
import models.Servidor;
import models.TipoSensor;
import repository.Banco;
import repository.Dados;

public class ServidorTemperaturaQuarto3 {

    public static void main(String[] args) throws IOException, InterruptedException {
        Banco.inicializar();
        // Essa Thread é reponsável por criar uma conexão via Sockets
        // Para realizar o envio do dado sensorial a cada 10 segundos para o Cliente
        new Thread(() -> {
            try {
                Socket socket = new Socket("host.docker.internal", 50051);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                // Verifica se na tabela servidor tem algum servidor salvo, se não tiver é criado um servidor e adicionando no banco de dados
                if (Dados.estaVazio()) {
                    Servidor servidor = new Servidor("Quarto 3", TipoSensor.TEMPERATURA, 0);
                    Dados.salvarServidor(servidor);
                }

                while (true) {
                    // Pega a referência do servidor no banco de dados
                    Servidor servidor = Dados.buscarDadosServidor();
                    // Atualiza o clock do servidor no banco de dados
                    Dados.atualizarClockServidor(servidor.getId(), servidor.getClock() + 1);
                    // Pega a referência do servidor, após ter seu clock incrementado
                    Servidor servidorAtualizado = Dados.buscarDadosServidor();
                    // Lista dos possíveis dados que podem ser enviados para o Cliente, é selecionado de forma aleatoriamente
                    double[] listaDados = {20, 40, 60, 80, 100};
                    Random random = new Random();
                    DadosSensoriais dados = new DadosSensoriais(UUID.randomUUID(), servidorAtualizado.getClock(), servidorAtualizado.getNomeLocalSensor(), servidorAtualizado.getTipoSensor(), listaDados[random.nextInt(listaDados.length)], LocalDateTime.now());
                    // Salva os dados sensoriais no banco de dados
                    Dados.salvarDadosSensoriais(dados);
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

        // Lista de paramêtros de eleição de outros servidores, para o servidor atual ficar ciente
        ArrayList<Integer> listaParametroServidores = new ArrayList<>();
        listaParametroServidores.add(100);
        listaParametroServidores.add(200);
        listaParametroServidores.add(400);

        // Cria a classe bully, definindo seu paramêtro de eleição, tornando visível o paramêtro de outros servidores, define o paramêtro do lider atual e se é líder
        Bully bullyServidor = new Bully(300, listaParametroServidores, 400, false);

        // Thread para enviar dados periodicamente para o servidor líder, via gRPC
        // Caso o líder não corresponda, é iniciada uma eleição para definir o novo líder
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000);
                    // A partir do paramêtro do líder é possível saber a porta para enviar as mensagens periodicamente para o líder
                    int port = 0;
                    String host = "";
                    if (bullyServidor.getParametroLiderAtual() == 100) {
                        port = 50061;
                        host = "cont-server-temp-quarto1";
                    } else if (bullyServidor.getParametroLiderAtual() == 200) {
                        port = 50062;
                        host = "cont-server-temp-quarto2";
                    } else if (bullyServidor.getParametroLiderAtual() == 300) {
                        port = 50063;
                        host = "cont-server-temp-quarto3";
                    } else if (bullyServidor.getParametroLiderAtual() == 400) {
                        port = 50064;
                        host = "cont-server-umi-banheiro1";
                    }
                    // Se não for líder executa o bloco de código
                    if (!bullyServidor.isLider()) {
                        try (Socket socket = new Socket(host, port);
                             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                            out.println("Ativo?");
                            String resposta = in.readLine();

                            if (!"Sim".equalsIgnoreCase(resposta)) {
                                System.out.println("\u001B[31m" + "Líder não respondeu como esperado" + "\u001B[0m");
                                new EleicaoBully(bullyServidor).iniciarEleicao();
                            } else {
                                System.out.println("\u001B[32m" + "LÍDER ATIVO" + "\u001B[0m");
                            }

                        } catch (IOException e) {
                            System.out.println("\u001B[31m" + "LÍDER INATIVO" + "\u001B[0m");
                            new EleicaoBully(bullyServidor).iniciarEleicao();
                        }
                    } else {
                    for (double parametro : listaParametroServidores) {
                        String nomeServico = "";
                        if (parametro == 100) {
                            port = 50061;
                            host = "cont-server-temp-quarto1";
                            nomeServico = "Quarto 1";
                        } else if (parametro == 200) {
                            port = 50062;
                            host = "cont-server-temp-quarto2";
                            nomeServico = "Quarto 2";
                        } else if (parametro == 300) {
                            port = 50063;
                            host = "cont-server-temp-quarto3";
                            nomeServico = "Quarto 3";
                        } else if (parametro == 400) {
                            port = 50064;
                            host = "cont-server-umi-banheiro1";
                            nomeServico = "Banheiro 1";
                        }

                        try (Socket socket = new Socket(host, port);
                             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                            out.println("Ativo?");
                            String resposta = in.readLine();

                            if (!"Sim".equalsIgnoreCase(resposta)) {
                                System.out.println("\u001B[31m" + "Serviço não respondeu como esperado" + "\u001B[0m");
                            } else {
                                System.out.println("\u001B[32m\n" + nomeServico +  " ATIVO" + "\n\u001B[0m");
                            }

                        } catch (IOException e) {
                            System.out.println("\u001B[31m\n" + nomeServico + " INATIVO" + "\n\u001B[0m");
                            InetAddress group = InetAddress.getByName("230.0.0.0");
                            port = 50059;
                            MulticastSocket multicastSocket = new MulticastSocket();

                            String mensagem = "\nMensagem do LIDER \n" + nomeServico + " INATIVO\n";

                            byte[] buffer = mensagem.getBytes();

                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
                            multicastSocket.send(packet);
                        }
                    }
                }

                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Thread para ficar recebendo mensagens via Sockets
        // No caso, só vai receber mensagens se for o líder
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(50063)) {

                while (true) {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> {
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                            String mensagem = in.readLine();
                            System.out.println("Recebido: " + mensagem);

                            if ("Ativo?".equalsIgnoreCase(mensagem)) {
                                out.println("Sim");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Thread para receber o comando de iniciar a eleição, via Sockets
        // Quando alguém inicia uma eleição e envia mensagem para os servidores com paramêtro maior, a mensagem cai aqui
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(50073);

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

        // Thread para receber mensagens via multicast quando for eleito um novo líder, através da eleição
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
                                // Se o paramêtro do novo líder for diferente do paramêtro do servidor, torna falso
                                if (novoParametroLider != 300) {
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

        // Multicas para o líder enviar mensagem caso algum outro serviço feche
        new Thread(() -> {
            try {
                InetAddress group = InetAddress.getByName("230.0.0.0");
                int port = 50059;

                MulticastSocket multicastSocket = new MulticastSocket(port);
                multicastSocket.joinGroup(group);

                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);

                    String mensagemRecebida = new String(packet.getData(), 0, packet.getLength());

                    System.out.println("\u001B[36m" + mensagemRecebida + "\u001B[0m");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
