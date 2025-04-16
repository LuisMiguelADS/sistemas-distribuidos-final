package models;

import java.util.UUID;

public class Servidor {
    private UUID id;
    private int clock;
    private String nomeLocalSensor;
    private TipoSensor tipoSensor;


    public Servidor(String nomeLocalSensor, TipoSensor tipoSensor, int clock) {
        this.id = UUID.randomUUID();
        this.nomeLocalSensor = nomeLocalSensor;
        this.tipoSensor = tipoSensor;
        this.clock = clock;
    }

    public Servidor() {}

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

    public void setTipoSensor(TipoSensor sensor) {
        this.tipoSensor = sensor;
    }
}
