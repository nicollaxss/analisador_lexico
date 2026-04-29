package compilador;

import compilador.lexico.ClasseToken;
import compilador.lexico.Lexico;
import compilador.lexico.Token;
import compilador.sintatico.Sintatico;

public class App {
    public static void main(String[] args) {
        try {
            Sintatico sintatico = new Sintatico("teste.pas");
            sintatico.analisar();
            System.out.println("Análise sintática finalizada com sucesso!");

        } catch (RuntimeException e){
            System.err.println("\n==============================");
            System.err.println("ERRO NA COMPILAÇÃO");
            System.err.println("==============================");
            System.err.println(e.getMessage());
            System.err.println("==============================\n");
        }
        
//        Lexico lexico = new Lexico("teste.pas");
//        Token token = lexico.getNexToken();
//
//        while (token.getClasse() != ClasseToken.EOF) {
//            System.out.println(token);
//            token = lexico.getNexToken();
//        }
//        System.out.println(token);


    }
}
