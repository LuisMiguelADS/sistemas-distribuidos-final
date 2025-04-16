package repository;

import models.DadosSensoriais;
import models.Servidor;
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

    // Logo quando executa o servidor é salvo as informações no banco de dados, a partir deste método
    public static void salvarServidor(Servidor servidor) {
        String sql = """
                INSERT INTO servidor (id, clock, nome_local_sensor, tipo_sensor)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection conn = Banco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, servidor.getId());
                stmt.setInt(2, servidor.getClock());
                stmt.setString(3, servidor.getNomeLocalSensor());
                stmt.setString(4, servidor.getTipoSensor().name());
                stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Como está aplicado o algoritmo de Lamport, esse método permite atualizar o clock do servidor
    public static void atualizarClockServidor(UUID id, int novoClock) {
        String sql = "UPDATE servidor SET clock = ? WHERE id = ?";

        try (Connection conn = Banco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, novoClock);
            stmt.setObject(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
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

    // Pegar os dados do servidor
    public static Servidor buscarDadosServidor() {
        String sql = "SELECT * FROM servidor LIMIT 1";

        try (Connection conn = Banco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                Servidor servidor = new Servidor();
                servidor.setId((UUID) rs.getObject("id", UUID.class));
                servidor.setClock(rs.getInt("clock"));
                servidor.setNomeLocalSensor(rs.getString("nome_local_sensor"));
                servidor.setTipoSensor(TipoSensor.valueOf(rs.getString("tipo_sensor")));
                return servidor;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Verifica se na tabela servidor tem algum dado salvo
    public static boolean estaVazio() {
        String sql = "SELECT COUNT(*) FROM servidor";

        try (Connection conn = Banco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() && rs.getInt(1) == 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
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

    public static DadosSensoriais buscarUltimo() {
        DadosSensoriais dado = null;
        String sql = "SELECT * FROM dados_sensoriais ORDER BY data_hora DESC LIMIT 1";

        try (Connection conn = Banco.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                dado = new DadosSensoriais();
                dado.setId(rs.getObject("id", UUID.class));
                dado.setClock(rs.getInt("clock"));
                dado.setNomeLocalSensor(rs.getString("nome_local_sensor"));
                dado.setTipoSensor(TipoSensor.valueOf(rs.getString("tipo_sensor")));
                dado.setDadoSensor(rs.getDouble("dado_sensor"));
                dado.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dado;
    }
}
