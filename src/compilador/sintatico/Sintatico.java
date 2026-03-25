package compilador.sintatico;

import compilador.lexico.ClasseToken;
import compilador.lexico.Lexico;
import compilador.lexico.Token;

public class Sintatico {

    private Lexico lexico;
    private Token token;
    private String arquivoCodigo;

    public Sintatico(String arquivoCodigo) {
        this.arquivoCodigo = arquivoCodigo;
    }

    public void analisar() {
        lexico = new Lexico(arquivoCodigo);
        token = lexico.getNexToken();
        programa();
    }

    // <programa> ::= program id {A01} ; <corpo> . {A45}
    public void programa() {
        if (ehPalavraReservada("program")) {
            token = lexico.getNexToken();
            if (token.getClasse() == ClasseToken.Identificador) {
                token = lexico.getNexToken();
                // {A01} -> Aqui viria a ação 01
                if (token.getClasse() == ClasseToken.PontoVirgula) {
                    token = lexico.getNexToken();
                    // corpo();
                    if (token.getClasse() == ClasseToken.Ponto) {
                        token = lexico.getNexToken();
                        // {A45}
                    } else {
                        throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                                                    + "]. Erro Sintático => Faltou ponto final no programa (.)");
                    }
                } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                + "]. Erro Sintático => Faltou ponto e virgula depois do nome do programa (.)");
                }
            } else {
                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou o nome do Programa"); 
            } 
        } else {
                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou começar o programa com Program");
            }
    }

    
    // <corpo> ::= <declara> <rotina> {A44} begin <sentencas> end {A46}
    public void corpo() {
        declara();
        // <rotina>
        // {A44}
        if (ehPalavraReservada("begin")) {
            token = lexico.getNexToken();
            sentencas();
            if (ehPalavraReservada("end")) {
                token = lexico.getNexToken();
                // {A46}
            } else {
                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou END no final do programa"); 
            }
        } else {
            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou o BEGIN no começo do Programa"); 
        }
    }

    // <declara> ::= var <dvar> <mais_dc> | ε
    private void declara() {
        if (ehPalavraReservada("var")) {
            token = lexico.getNexToken();
            dvar();
            mais_dc();
        }
    }

    private void sentencas() {

    }

    // <dvar> ::= <variaveis> : <tipo_var> {A02}
    private void dvar() {
        variaveis();
        if (token.getClasse() == ClasseToken.DoisPontos) {
            token = lexico.getNexToken();
            tipo_var();
        } else {
            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou dois pontos (:) na declaracao de variáveis"); 
        }
    }

    private void variaveis() {

    }

    // <tipo_var> ::= integer
    private void tipo_var() {
        if (ehPalavraReservada("integer")) {
            token = lexico.getNexToken();
        } else {
            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou o tipo (INTEGER) das variaveis" ); 
        }
    }

    private void cont_dc() {
        if (token.getClasse() == ClasseToken.Identificador) {
            dvar();
            mais_dc();
        }
    }

    // <mais_dc> ::=  ; <cont_dc>
    private void mais_dc() {
        if (token.getClasse() == ClasseToken.PontoVirgula) {
            token = lexico.getNexToken();
            cont_dc();
        } else {
            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou ponto e vírgula (;) na declaração de variáveis" ); 
        }
    }

    private boolean ehPalavraReservada(String palavra) {
        return token.getClasse() == ClasseToken.PalavraReservada 
            && token.getValor().getTexto().equalsIgnoreCase(palavra);
    }

}