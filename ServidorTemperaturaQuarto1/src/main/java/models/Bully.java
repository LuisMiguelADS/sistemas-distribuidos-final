package models;

import java.util.List;

public class Bully {

    private int parametroEleicao;
    private int parametroLiderAtual;
    private List<Integer> listaParametroEleicao;
    private boolean lider;

    public Bully(int parametroEleicao, List<Integer> listaParametroEleicao, int parametroLiderAtual, boolean lider) {
        this.parametroEleicao = parametroEleicao;
        this.listaParametroEleicao = listaParametroEleicao;
        this.lider = lider;
        this.parametroLiderAtual = parametroLiderAtual;
    }

    public int getParametroEleicao() {
        return parametroEleicao;
    }

    public void setParametroEleicao(int parametroEleicao) {
        this.parametroEleicao = parametroEleicao;
    }

    public List<Integer> getListaParametroEleicao() {
        return listaParametroEleicao;
    }

    public void setListaParametroEleicao(List<Integer> listaParametroEleicao) {
        this.listaParametroEleicao = listaParametroEleicao;
    }

    public int getParametroLiderAtual() {
        return parametroLiderAtual;
    }

    public void setParametroLiderAtual(int parametroLiderAtual) {
        this.parametroLiderAtual = parametroLiderAtual;
    }

    public boolean isLider() {
        return lider;
    }

    public void setLider(boolean lider) {
        this.lider = lider;
    }
}
