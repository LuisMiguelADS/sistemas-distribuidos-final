package repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.DadosSensoriais;
import models.TipoSensor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Dados {

    // Utilizado para pegar salvar os dados sensoriais quando é enviado para o cliente
    public static void salvarDadosSensoriais(DadosSensoriais dados) {
        String sql = """
                INSERT INTO dados_sensoriais (id, clock, nome_local_sensor, tipo_sensor, dado_sensor, data_hora)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = Banco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, dados.getId());
            stmt.setInt(2, dados.getClock());
            stmt.setString(3, dados.getNomeLocalSensor());
            stmt.setString(4, dados.getTipoSensor().name());
            stmt.setDouble(5, dados.getDadoSensor());
            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(dados.getDataHora()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Utilizado para pegar salvar os dados sensoriais quando é enviado para o cliente
    public static void salvarCheckpoint(String checkpoint) {
        String sql = """
            INSERT INTO checkpoints (checkpoint)
            VALUES (?)
        """;

        try (Connection conn = Banco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, checkpoint);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Busca todos os dados sensoriais presente no servidor
    public static List<DadosSensoriais> buscarTodos() {
        List<DadosSensoriais> lista = new ArrayList<>();
        String sql = "SELECT * FROM dados_sensoriais";

        try (Connection conn = Banco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                DadosSensoriais dado = new DadosSensoriais();
                dado.setId((UUID) rs.getObject("id", UUID.class));
                dado.setClock(rs.getInt("clock"));
                dado.setNomeLocalSensor(rs.getString("nome_local_sensor"));
                dado.setTipoSensor(TipoSensor.valueOf(rs.getString("tipo_sensor")));
                dado.setDadoSensor(rs.getDouble("dado_sensor"));
                dado.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());

                lista.add(dado);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    // Exclui todos os dados sensoriais salvos
    public static void excluirTodos() {
        String sql = "DELETE FROM dados_sensoriais";
        try (Connection conn = Banco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int quantidadeExcluida = stmt.executeUpdate();
            System.out.println("\u001B[31m" + "Dados excluidos.\n" + "\u001B[36m" +
                    "Total: " + quantidadeExcluida + "\u001B[0m");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void salvarCliente(int clock) {
        String sql = """
            INSERT INTO cliente (clock)
            VALUES (?)
        """;

        try (Connection conn = Banco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clock);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Integer buscarClockCliente() {
        String sql = "SELECT clock FROM cliente LIMIT 1";

        try (Connection conn = Banco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("clock");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void atualizarClockCliente(int novoClock) {
        String sql = "UPDATE cliente SET clock = ?";

        try (Connection conn = Banco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, novoClock);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
