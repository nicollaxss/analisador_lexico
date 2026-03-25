package compilador;

import compilador.sintatico.Sintatico;

public class App {
    public static void main(String[] args) {
        Sintatico sintatico = new Sintatico("teste.pas");
        sintatico.analisar();
    }
}
