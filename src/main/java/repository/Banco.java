package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Banco {

    private static final String URL = "jdbc:h2:./dadosSensoresDB";

    // Realiza a conexão com o bando de dados, no nosso caso estamos utilizando o H2
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, "sa", "");
    }

    // Se não tiver criado as tabelas ainda, esse método garante a criação
    public static void inicializar() {
        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            String sqlCheckpoints = """
                CREATE TABLE IF NOT EXISTS dados_sensoriais (
                        id UUID PRIMARY KEY,
                        clock INT,
                        nome_local_sensor VARCHAR(20),
                        tipo_sensor VARCHAR(20),
                        dado_sensor DOUBLE,
                        data_hora TIMESTAMP
                    );
                
                CREATE TABLE IF NOT EXISTS checkpoints (
                        checkpoint CLOB
                    );
                
                CREATE TABLE IF NOT EXISTS cliente (
                    clock INT
                )
                """;
            stmt.execute(sqlCheckpoints);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
