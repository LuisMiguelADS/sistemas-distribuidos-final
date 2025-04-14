package models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class DadosSensoriais implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private int clock;
    private String nomeLocalSensor;
    private TipoSensor tipoSensor;
    private double dadoSensor;
    private LocalDateTime dataHora;

    public DadosSensoriais(UUID id, int clock, String nomeLocalSensor, TipoSensor tipoSensor, double dadoSensor, LocalDateTime dataHora) {
        this.id = id;
        this.clock = clock;
        this.nomeLocalSensor = nomeLocalSensor;
        this.tipoSensor = tipoSensor;
        this.dadoSensor = dadoSensor;
        this.dataHora = dataHora;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getClock() {
        return clock;
    }

    public void setClock(int clock) {
        this.clock = clock;
    }

    public String getNomeLocalSensor() {
        return nomeLocalSensor;
    }

    public void setNomeLocalSensor(String nomeLocalSensor) {
        this.nomeLocalSensor = nomeLocalSensor;
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }

    public void setTipoSensor(TipoSensor tipoSensor) {
        this.tipoSensor = tipoSensor;
    }

    public double getDadoSensor() {
        return dadoSensor;
    }

    public void setDadoSensor(double dadoSensor) {
        this.dadoSensor = dadoSensor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    @Override
    public String toString() {
        return "Dados Sensoriais" +
                "\nID: " + id +
                "\nLocal: " + nomeLocalSensor +
                "\nTipo do sensor: " + tipoSensor +
                "\nDado: " + dadoSensor;
    }
}
