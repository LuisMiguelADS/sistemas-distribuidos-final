package Testes;

import models.DadosSensoriais;
import models.Servidor;
import models.TipoSensor;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

public class ServidorUmidadeBanheiro1 {
    public static void main(String[] args) throws IOException, InterruptedException {
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
            System.out.println("Dados enviado: " + dados);
            Thread.sleep(10000);
        }
    }
}
