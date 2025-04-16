import repository.Banco;
import repository.Dados;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RetornarDadosEnviados {
    public static void main(String[] args) throws IOException {
        // Inicia o banco de dados
        Banco.inicializar();
        while (true) {
            // Para interação com o terminal
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("\u001B[34m" + "----- Banco de dados ----- " +
                    "\n1 - Buscar todos" +
                    "\n2 - Excluir dados" );
            System.out.println("Sua escolha: " + "\u001B[0m");

            String input = reader.readLine().trim();
            // A partir do que for digitado, será efetuada algo
            switch (input) {
                case "1":
                    System.out.println(Dados.buscarTodos());
                    break;
                case "2":
                    Dados.excluirTodos();
            }
        }
    }
}
