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
                    corpo();
                    if (token.getClasse() == ClasseToken.Ponto) { // nao esta verificando o end, ate agora
                        token = lexico.getNexToken();
                        // {A45}
                    } else {
                        throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                                                    + "]. Erro Sintático => Faltou ponto final no programa (.)");
                    }
                } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                + "]. Erro Sintático => Faltou ponto e virgula depois do nome do programa (;)");
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

    // <sentencas> ::= <comando> <mais_sentencas>
    private void sentencas() {
        // comando();
        mais_sentencas();
    }

    /*  <comando> ::=
        read ( <var_read> ) |
        write ( <exp_write> ) |
        writeln ( <exp_write> ) {A61} |
        for id {A57} := <expressao> {A11} to <expressao> {A12} do begin <sentencas> end {A13} |
        repeat {A14} <sentencas> until ( <expressao_logica> ) {A15} |
        while {A16} ( <expressao_logica> ) {A17} do begin <sentencas> end {A18} |
        if ( <expressao_logica> ) {A19} then begin <sentencas> end {A20} <pfalsa> {A21} |
        id {A49} := <expressao> {A22} | 
    */

    private void comando() {
        // read ( <var_read> )
        if (ehPalavraReservada("read")) {
            // abreparenteses
            // var_read();
            // fechaparenteses
        } 
        // write ( <exp_write> )
        else if (ehPalavraReservada("write")){
            // abreparenteses
            // exp_write();
            // fechaparenteses
        } 
        // writeln ( <exp_write> ) {A61} 
        else if (ehPalavraReservada("writeln")){
            // abreparenteses
            // exp_write();
            // fechaparenteses
        } 
        // for id {A57} := <expressao> {A11} to <expressao> {A12} do begin <sentencas> end {A13} 
        else if (ehPalavraReservada("for")){
            token = lexico.getNexToken();
            // id();
            // analisar ponto e virgula
            // expressao();
            // to
            // expressao();
            // do
            // begin
            // sentencas();
            // end
        }
        // repeat {A14} <sentencas> until ( <expressao_logica> ) {A15}
        else if (ehPalavraReservada("repeat")){
            // sentencas();
            // until
            // abre parenteses
            // expressao_logica();
            // fecha parenteses
        }
        // while {A16} ( <expressao_logica> ) {A17} do begin <sentencas> end {A18}
        else if (ehPalavraReservada("while")){
            // abre parenteses
            // expressao_logica();
            // fecha parenteses
            // do
            // begin
            // sentencas();
            // end
        }
        // if ( <expressao_logica> ) {A19} then begin <sentencas> end {A20} <pfalsa> {A21}
        else if (ehPalavraReservada("if")){
            // abre parenteses
            // expressao_logica();
            // fecha parenteses
            // then
            // begin
            // sentencas();
            // end
            // pfalsa();
        }
        // id {A49} := <expressao> {A22}
        else if (ehPalavraReservada("id")){
            // id();
            // :=
            // expressao();
        }
    }

    // <mais_sentencas> ::= ; <cont_sentencas>
    private void mais_sentencas() {
        if (token.getClasse() == ClasseToken.PontoVirgula) {
            token = lexico.getNexToken();
            cont_sentencas();
        } else {
            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou ponto e vírgula (;) no final de uma sentença"); 
        }
    }

    // <cont_sentencas> ::= <sentencas> | ε
    private void cont_sentencas() {
        // verificar se é vazio primeiro
        if (ehPalavraReservada("read") || ehPalavraReservada("write") || 
            ehPalavraReservada("writeln") || ehPalavraReservada("for") ||
            ehPalavraReservada("repeat") || ehPalavraReservada("while") || 
            ehPalavraReservada("if") || token.getClasse() == ClasseToken.Identificador)  {
                sentencas();
            }
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

    // <variaveis> ::= id {A03} <mais_var>
    private void variaveis() {
        if (token.getClasse() == ClasseToken.Identificador) {
            token = lexico.getNexToken();
            // {A03}
            mais_var();
        } else {
            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou um identificador de variável" ); 
        }
    }

    // <mais_var> ::=  ,  <variaveis> | ε
    private void mais_var() {
        if (token.getClasse() == ClasseToken.Virgula) {
            token = lexico.getNexToken();
            variaveis();
        }
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