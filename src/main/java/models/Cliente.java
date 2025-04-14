package models;

import java.util.ArrayList;

public class Cliente {
    private int clock = 0;
    private ArrayList<DadosSensoriais> dadosRecebidos = new ArrayList();

    public int getClock() {
        return clock;
    }

    public void setClock(int clock) {
        this.clock = clock;
    }

    public void addDadosSensoriais(DadosSensoriais dadosSensoriais) {
        this.dadosRecebidos.add(dadosSensoriais);
    }

    public ArrayList<DadosSensoriais> getDadosRecebidos() {
        return dadosRecebidos;
    }

    public void setDadosRecebidos(ArrayList<DadosSensoriais> dadosRecebidos) {
        this.dadosRecebidos = dadosRecebidos;
    }
}
