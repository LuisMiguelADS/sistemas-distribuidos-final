package models;

import java.io.*;
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
                String host = "";
                if (parametro == 100) {
                    port = 50061;
                    host = "cont-server-temp-quarto1";
                } else if (parametro == 200) {
                    port = 50062;
                    host = "cont-server-temp-quarto2";
                } else if (parametro == 300) {
                    port = 50063;
                    host = "cont-server-temp-quarto3";
                } else if (parametro == 400) {
                    port = 50064;
                    host = "cont-server-umi-banheiro1";
                }
                try {
                    // Conecta ao servidor com o parâmetro maior
                    try (Socket socket = new Socket(host, port)) {
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        // Envia a mensagem "Ativo?" para verificar se o servidor está vivo
                        writer.write("Ativo?\n");
                        writer.flush();

                        // Recebe a resposta do servidor
                        String resposta = reader.readLine();


                        if ("Sim".equalsIgnoreCase(resposta)) {
                            servidorParametroMaior = true;

                            // Define host/porta baseados no parâmetro
                            if (parametro == 100) {
                                port = 50071;
                                host = "cont-server-temp-quarto1";
                            } else if (parametro == 200) {
                                port = 50072;
                                host = "cont-server-temp-quarto2";
                            } else if (parametro == 300) {
                                port = 50073;
                                host = "cont-server-temp-quarto3";
                            } else if (parametro == 400) {
                                port = 50074;
                                host = "cont-server-umi-banheiro1";
                            }

                            // Envia a mensagem "iniciar-eleicao" para esse servidor
                            try (Socket socket2 = new Socket(host, port)) {
                                BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(socket2.getOutputStream()));
                                writer2.write("iniciar-eleicao\n");
                                writer2.flush();
                            } catch (IOException ex) {
                                System.out.println("\u001B[31mNao foi possivel entrar em contato com o servidor\u001B[0m");
                            }

                            break;
                        }

                    } catch (IOException e) {
                        System.out.println("\u001B[31mNao foi possível entrar em contato com o servidor (Ativo?)\u001B[0m");
                    }

                } catch (Exception e) {
                    System.out.println("\u001B[31mErro inesperado ao verificar o servidor\u001B[0m");
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
