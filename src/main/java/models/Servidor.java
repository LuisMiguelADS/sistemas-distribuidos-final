package models;

import java.util.UUID;

public class Servidor {
    private UUID id;
    private int clock;
    private String nomeLocalSensor;
    private TipoSensor sensor;
    private double dado_sensor;

    public Servidor(String nomeLocalSensor, TipoSensor tipoSensor, double dados) {
        this.nomeLocalSensor = nomeLocalSensor;
        this.sensor = tipoSensor;
        this.dado_sensor = dados;
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

    public TipoSensor getSensor() {
        return sensor;
    }

    public void setSensor(TipoSensor sensor) {
        this.sensor = sensor;
    }

    public double getDado_sensor() {
        return dado_sensor;
    }

    public void setDado_sensor(double dado_sensor) {
        this.dado_sensor = dado_sensor;
    }
}
