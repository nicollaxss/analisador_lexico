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
                    if (token.getClasse() == ClasseToken.Ponto) {
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
            // System.out.println("ASDYHGLSADLKJASHLKJ" + token);
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
        comando();
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
            token = lexico.getNexToken();
            if (token.getClasse() == ClasseToken.AbreParenteses) {
                token = lexico.getNexToken();
                var_read();
                if (token.getClasse() == ClasseToken.FechaParenteses) {
                    token = lexico.getNexToken();
                } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + "]. Faltou ')'");
                }
            } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + "]. Faltou '('");
                }
        } 
        // write ( <exp_write> )
        else if (ehPalavraReservada("write")) {
            token = lexico.getNexToken();
            if (token.getClasse() == ClasseToken.AbreParenteses) {
                token = lexico.getNexToken();
                exp_write();
                if (token.getClasse() == ClasseToken.FechaParenteses) {
                    token = lexico.getNexToken();
                } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou fechar parênteses ')'");
                }
            } else {
                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou abrir parênteses '('");
            }
        } 
        // writeln ( <exp_write> ) {A61} 
        // adicionar a questao de linha nova
        else if (ehPalavraReservada("writeln")){
            token = lexico.getNexToken();
            if (token.getClasse() == ClasseToken.AbreParenteses) {
                token = lexico.getNexToken();
                exp_write();   
                if (token.getClasse() ==  ClasseToken.FechaParenteses) {
                    // {A61}
                    token = lexico.getNexToken();
                } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou fechar parênteses ')'");
                }
            } else {
                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou abrir parênteses '('"); 
            }
        } 
        // for id {A57} := <expressao> {A11} to <expressao> {A12} do begin <sentencas> end {A13} 
        else if (ehPalavraReservada("for")){
            token = lexico.getNexToken();
            if (token.getClasse() == ClasseToken.Identificador) {
                // {A57}
                token = lexico.getNexToken();
                if (token.getClasse() == ClasseToken.Atribuicao) { 
                    token = lexico.getNexToken();
                    expressao();
                    // {A11}
                    if (ehPalavraReservada("to")) { 
                        token = lexico.getNexToken();
                        expressao();
                        // {A12}
                        if (ehPalavraReservada("do")) {
                            token = lexico.getNexToken();
                            if (ehPalavraReservada("begin")) {
                                token = lexico.getNexToken();
                                sentencas();
                                if (ehPalavraReservada("end")) {
                                    token = lexico.getNexToken();
                                } else {
                                    throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou 'end'"); 
                                }
                            } else {
                                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou 'begin'"); 
                            }  
                        } else {
                                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                    + "]. Erro Sintático => Faltou 'do'"); 
                        }  
                    } else {
                            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                            + "]. Erro Sintático => Faltou 'to'"); 
                    }
                }    
            }
        }
        // repeat {A14} <sentencas> until ( <expressao_logica> ) {A15}
        else if (ehPalavraReservada("repeat")){
            token = lexico.getNexToken();
            // {A14}
            sentencas();
            if (ehPalavraReservada("until")) {
                token = lexico.getNexToken();
                if (token.getClasse() == ClasseToken.AbreParenteses) {
                    token = lexico.getNexToken();
                    expressao_logica();
                    if (token.getClasse() == ClasseToken.FechaParenteses) {
                        token = lexico.getNexToken();
                        // {A15}
                    } else {
                        throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                            + "]. Erro Sintático => Faltou ')'"); 
                    }
                } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                            + "]. Erro Sintático => Faltou ')'"); 
                }
            } else {
                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                            + "]. Erro Sintático => Faltou 'until'"); 
            }
        }
        // while {A16} ( <expressao_logica> ) {A17} do begin <sentencas> end {A18}
        else if (ehPalavraReservada("while")){
            token = lexico.getNexToken();
            // {A16}
            if (token.getClasse() == ClasseToken.AbreParenteses) {
                token = lexico.getNexToken();
                expressao_logica();
                if (token.getClasse() == ClasseToken.FechaParenteses) {
                    token = lexico.getNexToken();
                    // {A17}
                    if (ehPalavraReservada("do")) {
                        token = lexico.getNexToken();
                        if (ehPalavraReservada("begin")) {
                            token = lexico.getNexToken();
                            sentencas();
                            if (ehPalavraReservada("end")) {
                                token = lexico.getNexToken();
                                // {A18}
                            } else {
                                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou 'end'");
                            }
                        } else {
                                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou 'begin'");
                            }
                    } else {
                                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou 'do'");
                            }
                } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                            + "]. Erro Sintático => Faltou fechar ')'");
                }
            } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                            + "]. Erro Sintático => Faltou abrir '('");
                }
        }
        // if ( <expressao_logica> ) {A19} then begin <sentencas> end {A20} <pfalsa> {A21}
        else if (ehPalavraReservada("if")){
            token = lexico.getNexToken();
            if (token.getClasse() == ClasseToken.AbreParenteses) {
                token = lexico.getNexToken();
                expressao_logica();
                if (token.getClasse() == ClasseToken.FechaParenteses) {
                    token = lexico.getNexToken();
                    // {A19}
                    if (ehPalavraReservada("then")) {
                        token = lexico.getNexToken();
                        if (ehPalavraReservada("begin")) {
                            token = lexico.getNexToken();
                            sentencas();
                            if (ehPalavraReservada("end")) {
                                token = lexico.getNexToken();
                                // {A20}
                                pfalsa();
                                // {A21}
                            } else {
                                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou 'end'");
                            }
                        } else {
                            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                    + "]. Erro Sintático => Faltou 'begin'");
                        }
                    } else {
                        throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                + "]. Erro Sintático => Faltou 'then'");
                    }
                } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                            + "]. Erro Sintático => Faltou fechar ')'");
                }
            } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                            + "]. Erro Sintático => Faltou abrir '('");
                }
        }
        // id {A49} := <expressao> {A22}
        else if (token.getClasse() == ClasseToken.Identificador){
            // {A49}
            token = lexico.getNexToken();
            if (token.getClasse() == ClasseToken.Atribuicao) {
                token = lexico.getNexToken();
                expressao();
                // {A22}
            } else {
                throw new RuntimeException("[linha=" + token.getLinha() + "]. Esperado ':='");
            }
        }
    }

    // <pfalsa> ::= {A25} else begin <sentencas> end | ε
    private void pfalsa() {
        if (ehPalavraReservada("else")) {
            // {A25}
            token = lexico.getNexToken();
            if (ehPalavraReservada("begin")) {
                token = lexico.getNexToken();
                sentencas();
                System.out.println("DASDAS" + token);
                if (ehPalavraReservada("end")) {
                    token = lexico.getNexToken();
                    if (token.getClasse() == ClasseToken.Ponto) {
                        throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                            + "]. Erro Sintático => Está faltando um end. Tem apenas o do end do final");
                    }
                } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                            + "]. Erro Sintático => Faltou 'end'");
                }
            } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                            + "]. Erro Sintático => Faltou 'begin'");
                }
        }
    }

    // <expressao_logica> ::= <termo_logico> <mais_expr_logica>
    private void expressao_logica() {
        termo_logico();
        mais_expr_logica();
    }

    // <mais_expr_logica> ::= or <termo_logico> {A26} <mais_expr_logica>  | ε
    private void mais_expr_logica() {
        if (ehPalavraReservada("or")) {
            token = lexico.getNexToken();
            termo_logico();
            // {A26}
            mais_expr_logica();
        }
    }

    // <termo_logico> ::= <fator_logico> <mais_termo_logico>
    private void termo_logico() {
        fator_logico();
        mais_termo_logico();
    }

    // <mais_termo_logico> ::= and <fator_logico> {A27} <mais_termo_logico>  | ε
    private void mais_termo_logico() {
        if (ehPalavraReservada("and")) {
            token = lexico.getNexToken();
            fator_logico();
            // {A27}
            mais_termo_logico();
        }
    }

    /*
    <fator_logico> ::= <relacional> |
                   ( <expressao_logica> ) |
                   not <fator_logico> {A28} |
                   true {A29} |
                   false {A30}
    */
    private void fator_logico() {
        if (token.getClasse() == ClasseToken.AbreParenteses) {
            token = lexico.getNexToken();
            expressao_logica();
            if (token.getClasse() == ClasseToken.FechaParenteses) {
                token = lexico.getNexToken();
            } else {
                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou fechar parênteses ')'");
            }
        } else if (ehPalavraReservada("not")) {
            token = lexico.getNexToken();
            fator_logico();
            // {A28}
        } else if (ehPalavraReservada("true")) {
            token = lexico.getNexToken();
            // {A29}
        } else if (ehPalavraReservada("false")) {
            token = lexico.getNexToken();
            // {A30}
        } else {
            relacional();
        }
    }

    /*
    <relacional> ::= <expressao> =  <expressao> {A31} |
                 <expressao> >  <expressao> {A32} |
                 <expressao> >= <expressao> {A33} |
                 <expressao> <  <expressao> {A34} |
                 <expressao> <= <expressao> {A35} |
                 <expressao> <> <expressao> {A36}
    */
    private void relacional() {
        expressao();

        ClasseToken op = token.getClasse();

        if (op == ClasseToken.Igualdade || op == ClasseToken.Maior || 
            op == ClasseToken.MaiorIgual || op == ClasseToken.Menor || 
            op == ClasseToken.MenorIgual || op == ClasseToken.Diferente) {

            token = lexico.getNexToken();
            expressao();
        } 
        else {
            throw new RuntimeException("[linha=" + token.getLinha() + 
            "]. Operador relacional esperado");
        }
    }
    
    // <expressao> ::= <termo> <mais_expressao>
    private void expressao() {
        termo();
        mais_expressao();
    }

    // <termo> ::= <fator> <mais_termo>
    private void termo() {
        fator();
        mais_termo();
    }

    /*
    <mais_termo> ::= * <fator> {A39} <mais_termo>  |
                     / <fator> {A40} <mais_termo>  | ε
    */
   private void mais_termo() {
    if (token.getClasse() == ClasseToken.Multiplicacao || 
        token.getClasse() == ClasseToken.Divisao) {
            token = lexico.getNexToken();
            fator();
            mais_termo();
        }
    }

    // <fator> ::= id {A55} | intnum {A41} | ( <expressao> ) 
    private void fator() {
        // verificar se é expressao
        if (token.getClasse() == ClasseToken.AbreParenteses) {
            token = lexico.getNexToken();
            expressao();
            if (token.getClasse() == ClasseToken.FechaParenteses) {
                token = lexico.getNexToken();
            } else {
                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                            + "]. Erro Sintático => Faltou fechar parenteses"); 
            }
        }
        else if (token.getClasse() == ClasseToken.Identificador || 
                token.getClasse() == ClasseToken.Inteiro) {
            token = lexico.getNexToken();
        } else {
            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                            + "]. Erro Sintático => Não é um <fator>"); 
        }
    }

    /*
    <mais_expressao> ::= + <termo> {A37} <mais_expressao>  |
                         - <termo> {A38} <mais_expressao>  | ε
    */
   private void mais_expressao() {
        if (token.getClasse() == ClasseToken.Mais || 
            token.getClasse() == ClasseToken.Menos) {
            token = lexico.getNexToken();
            termo();
            mais_expressao();
        }
    }


    /*
    <exp_write> ::= id {A09} <mais_exp_write> |
                string {A59} <mais_exp_write> |
                intnum {A43} <mais_exp_write>
    */
    private void exp_write() { 
        // id {A09} <mais_exp_write> |
        if (token.getClasse() == ClasseToken.Identificador) {
            // {A09}
            token = lexico.getNexToken();
            mais_exp_write(); 
        } 
        // string {A59} <mais_exp_write> |
        else if (token.getClasse() == ClasseToken.String) {
            // {A59}
            token = lexico.getNexToken();
            mais_exp_write(); 
        } 
        // intnum {A43} <mais_exp_write>
        else if (token.getClasse() == ClasseToken.Inteiro) {
            // {A43}
            token = lexico.getNexToken();
            mais_exp_write(); 
        }
        else {
            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Write"); 
        }
    }

    // <mais_exp_write> ::=  ,  <exp_write> | ε
    private void mais_exp_write() { 
        // Lê a vírgula ou o parênteses
        if (token.getClasse() == ClasseToken.Virgula) {
            token = lexico.getNexToken();
            exp_write();
        }
    }


    // <var_read> ::= id {A08} <mais_var_read>
    private void var_read() {
        if (token.getClasse() == ClasseToken.Identificador) {
            token = lexico.getNexToken();
            mais_var_read();
        } else {
            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou identificador");
        }
    }

    // <mais_var_read> ::= , <var_read> | ε
    private void mais_var_read() {
        if (token.getClasse() == ClasseToken.Virgula) {
            token = lexico.getNexToken();
            var_read();
        }
    }

    // <mais_sentencas> ::= ; <cont_sentencas>
    private void mais_sentencas() {
        if (token.getClasse() == ClasseToken.PontoVirgula) {
            token = lexico.getNexToken();
            cont_sentencas();
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
        // System.out.println("token atual" + token);
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

    // <cont_dc> ::= <dvar> <mais_dc> | ε
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
        }
    }

    private boolean ehPalavraReservada(String palavra) {
        return token.getClasse() == ClasseToken.PalavraReservada 
            && token.getValor().getTexto().equalsIgnoreCase(palavra);
    }

}