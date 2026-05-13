package compilador.sintatico;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import compilador.lexico.ClasseToken;
import compilador.lexico.Lexico;
import compilador.lexico.Token;

public class Sintatico {

    private Lexico lexico;
    private Token token;
    private String arquivoCodigo;
    private TabelaSimbolos tabela;
    private int offsetVariavel = 0;
    private List<String> sectionData = new ArrayList<>();
    private Registro registro;
    private BufferedWriter bw;
    private FileWriter fw;
    private String rotulo = "";
    private String rotuloElse = "";
    private int contRotulo = 1;
    private static final int TAMANHO_INTEIRO = 4;
    private List<String> variaveis = new ArrayList<>();

    public Sintatico(String arquivoCodigo) {
        this.arquivoCodigo = arquivoCodigo;
        this.tabela = new TabelaSimbolos();

        try {
            fw = new FileWriter("queronemver.asm");
            bw = new BufferedWriter(fw);
        } catch (IOException e) {
            System.err.println("Erro ao criar arquivo de saída");
        }
    }

    public void analisar() {
        lexico = new Lexico(arquivoCodigo);
        token = lexico.getNexToken();
        programa();
    }

    private void escreverCodigo(String instrucoes) {
        try {
            if (rotulo.isEmpty()) {
                bw.write(instrucoes + "\n");
            } else {
                bw.write(rotulo + ": " + instrucoes + "\n");
                rotulo = "";
            }
        } catch (IOException e) {
            System.err.println("Erro escrevendo no arquivo de saída");
        }
    }

    private String criarRotulo(String texto) {
        String retorno = "rotulo" + texto + contRotulo;
        contRotulo++;
        return retorno;
    }

    // <programa> ::= program id {A01} ; <corpo> . {A45}
    public void programa() {
        if (ehPalavraReservada("program")) {
            token = lexico.getNexToken();
            if (token.getClasse() == ClasseToken.Identificador) {
                // {A01} -> Aqui viria a ação 01
                Registro registro = tabela.add(token.getValor().getTexto());
                offsetVariavel = 0;
                registro.setCategoria(Categoria.PROGRAMA_PRINCIPAL);
                escreverCodigo("global _main");
                escreverCodigo("extern _printf");
                escreverCodigo("extern _scanf\n");
                escreverCodigo("section .text");
                rotulo = "_main";
                escreverCodigo("\t; Entrada do programa");
                escreverCodigo("\tpush ebp");
                escreverCodigo("\tmov ebp, esp");

                token = lexico.getNexToken();
                if (token.getClasse() == ClasseToken.PontoVirgula) {
                    token = lexico.getNexToken();
                    corpo();
                    if (token.getClasse() == ClasseToken.Ponto) {
                        // {A45}
                        escreverCodigo("\tleave");
                        escreverCodigo("\tret");
                        if (!sectionData.isEmpty()) {
                            escreverCodigo("\nsection .data\n");
                            for (String mensagem : sectionData) {
                                escreverCodigo(mensagem);
                            }
                        }
                        try {
                            bw.close();
                            fw.close();
                        } catch (IOException e) {
                            System.err.println("Erro ao fechar arquivo de saída");
                        }

                        token = lexico.getNexToken();
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
                // {A46}

                token = lexico.getNexToken();
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

    /*
     * <comando> ::=
     * read ( <var_read> ) |
     * write ( <exp_write> ) |
     * writeln ( <exp_write> ) {A61} |
     * for id {A57} := <expressao> {A11} to <expressao> {A12} do begin <sentencas>
     * end {A13} |
     * repeat {A14} <sentencas> until ( <expressao_logica> ) {A15} |
     * while {A16} ( <expressao_logica> ) {A17} do begin <sentencas> end {A18} |
     * if ( <expressao_logica> ) {A19} then begin <sentencas> end {A20} <pfalsa>
     * {A21} |
     * id {A49} := <expressao> {A22} |
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
                    throw new RuntimeException("[linha=" + token.getLinha() + "]. Faltou ')' no read");
                }
            } else {
                throw new RuntimeException("[linha=" + token.getLinha() + "]. Faltou '(' no read");
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
        else if (ehPalavraReservada("writeln")) {
            token = lexico.getNexToken();
            if (token.getClasse() == ClasseToken.AbreParenteses) {
                token = lexico.getNexToken();
                exp_write();
                if (token.getClasse() == ClasseToken.FechaParenteses) {
                    // {A61}
                    String novaLinha = "rotuloStringLN: db '',10,0";
                    if (!sectionData.contains(novaLinha)) {
                        sectionData.add(novaLinha);
                    }
                    escreverCodigo("\tpush rotuloStringLN");
                    escreverCodigo("\tcall _printf");
                    escreverCodigo("\tadd esp, 4");

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
        // for id {A57} := <expressao> {A11} to <expressao> {A12} do begin <sentencas>
        // end {A13}
        else if (ehPalavraReservada("for")) {
            token = lexico.getNexToken();

            if (token.getClasse() == ClasseToken.Identificador) {
                // {A57}
                token = lexico.getNexToken();

                if (token.getClasse() == ClasseToken.Atribuicao) {
                    token = lexico.getNexToken();

                    expressao(false);
                    // {A11}
                    escreverCodigo("\tpop dword[ebp - " + registro.getOffset() + "]");
                    // NOVA VERSAO
                    // escreverCodigo("\tpop eax");
                    // escreverCodigo("\tmov dword[ebp - " + registro.getOffset() + "], eax");

                    String rotuloEntrada = criarRotulo("FOR");
                    String rotuloSaida = criarRotulo("FIMFOR");
                    rotulo = rotuloEntrada;

                    if (!ehPalavraReservada("to")) {
                        throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                                + "]. Erro Sintático => Faltou 'to'");
                    }

                    token = lexico.getNexToken();

                    expressao(false);
                    // {A12}
                    escreverCodigo("\tpush ecx\n"
                            + "\tmov ecx, dword[ebp - " + registro.getOffset() + "]\n"
                            + "\tcmp ecx, dword[esp+4]\n" // +4 por causa do ecx
                            + "\tjg " + rotuloSaida + "\n"
                            + "\tpop ecx");

                    if (ehPalavraReservada("do")) {
                        token = lexico.getNexToken();

                        if (ehPalavraReservada("begin")) {
                            token = lexico.getNexToken();

                            sentencas();

                            if (ehPalavraReservada("end")) {
                                // {A13}
                                escreverCodigo("\tadd dword[ebp - " + registro.getOffset() + "], 1");
                                escreverCodigo("\tjmp " + rotuloEntrada);
                                rotulo = rotuloSaida;

                                token = lexico.getNexToken();
                            } else {
                                throw new RuntimeException(
                                        "[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
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
                            + "]. Erro Sintático => Faltou ':=' no for");
                }
            } else {
                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                        + "]. Erro Sintático => Faltou identificador no for");
            }
        }
        // repeat {A14} <sentencas> until ( <expressao_logica> ) {A15}
        else if (ehPalavraReservada("repeat")) {
            // {A14}
            String rotRepeat = criarRotulo("Repeat");
            rotulo = rotRepeat;

            token = lexico.getNexToken();
            sentencas();
            if (ehPalavraReservada("until")) {
                token = lexico.getNexToken();
                if (token.getClasse() == ClasseToken.AbreParenteses) {
                    token = lexico.getNexToken();
                    expressao_logica();
                    if (token.getClasse() == ClasseToken.FechaParenteses) {
                        // {A15}
                        escreverCodigo("\tcmp dword[esp], 0");
                        escreverCodigo("\tje " + rotRepeat);
                        // NOVA VERSAO
                        // escreverCodigo("\tpop eax");
                        // escreverCodigo("\tcmp eax, 0");
                        // escreverCodigo("\tje " + rotRepeat);

                        token = lexico.getNexToken();
                    } else {
                        throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                                + "]. Erro Sintático => Faltou ')' no until");
                    }
                } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                            + "]. Erro Sintático => Faltou '(' no until");
                }
            } else {
                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                        + "]. Erro Sintático => Faltou 'until'");
            }
        }
        // while {A16} ( <expressao_logica> ) {A17} do begin <sentencas> end {A18}
        else if (ehPalavraReservada("while")) {
            // {A16}
            String rotuloWhile = criarRotulo("While");
            String rotuloFim = criarRotulo("FimWhile");
            rotulo = rotuloWhile;

            token = lexico.getNexToken();
            if (token.getClasse() == ClasseToken.AbreParenteses) {
                token = lexico.getNexToken();
                expressao_logica();
                if (token.getClasse() == ClasseToken.FechaParenteses) {
                    // {A17}
                    escreverCodigo("\tcmp dword[esp], 0");
                    escreverCodigo("\tje " + rotuloFim);
                    // NOVA VERSAO
                    // escreverCodigo("\tpop eax");
                    // escreverCodigo("\tcmp eax, 0");
                    // escreverCodigo("\tje " + rotuloFim);

                    token = lexico.getNexToken();
                    if (ehPalavraReservada("do")) {
                        token = lexico.getNexToken();
                        if (ehPalavraReservada("begin")) {
                            token = lexico.getNexToken();
                            sentencas();
                            if (ehPalavraReservada("end")) {
                                // {A18}
                                escreverCodigo("\tjmp " + rotuloWhile);
                                rotulo = rotuloFim;
                                token = lexico.getNexToken();
                            } else {
                                throw new RuntimeException(
                                        "[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
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
        // if ( <expressao_logica> ) {A19} then begin <sentencas> end {A20} <pfalsa>
        // {A21}
        else if (ehPalavraReservada("if")) {
            token = lexico.getNexToken();
            if (token.getClasse() == ClasseToken.AbreParenteses) {
                token = lexico.getNexToken();
                expressao_logica();
                if (token.getClasse() == ClasseToken.FechaParenteses) {
                    // {A19}
                    rotuloElse = criarRotulo("Else");
                    String rotuloFim = criarRotulo("FimIf");
                    escreverCodigo("\tcmp dword[esp], 0\n");
                    escreverCodigo("\tje " + rotuloElse);
                    // NOVA VERSAO
                    // escreverCodigo("\tpop eax");
                    // escreverCodigo("\tcmp eax, 0");
                    // escreverCodigo("\tje " + rotuloElse);

                    token = lexico.getNexToken();
                    if (ehPalavraReservada("then")) {
                        token = lexico.getNexToken();
                        if (ehPalavraReservada("begin")) {
                            token = lexico.getNexToken();
                            sentencas();
                            if (ehPalavraReservada("end")) {
                                // {A20}
                                escreverCodigo("\tjmp " + rotuloFim);
                                token = lexico.getNexToken();
                                pfalsa();
                                // {A21}
                                rotulo = rotuloFim;
                            } else {
                                throw new RuntimeException(
                                        "[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
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
        else if (token.getClasse() == ClasseToken.Identificador) {
            // {A49}
            String variavel = token.getValor().getTexto();
            if (!tabela.isPresent(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada");
                System.exit(-1);
            } else {
                registro = tabela.getRegistro(variavel);
                if (registro.getCategoria() != Categoria.VARIAVEL) {
                    System.err.println("O identificador " + variavel + "não é uma variável. A49");
                    System.exit(-1);
                }
            }

            token = lexico.getNexToken();
            if (token.getClasse() == ClasseToken.Atribuicao) {
                token = lexico.getNexToken();
                expressao();

                // {A22}
                registro = tabela.getRegistro(variavel);
                escreverCodigo("\tpop eax");
                escreverCodigo("\tmov dword[ebp - " + registro.getOffset() + "], eax");
            } else {
                throw new RuntimeException("[linha=" + token.getLinha() + "]. Esperado ':=' no identificador");
            }
        } else {
            throw new RuntimeException("[linha=" + token.getLinha() + "]. Esperado identificador");
        }
    }

    // <pfalsa> ::= {A25} else begin <sentencas> end | ε
    private void pfalsa() {
        if (ehPalavraReservada("else")) {
            // {A25}
            escreverCodigo(rotuloElse + ":");
            token = lexico.getNexToken();

            if (ehPalavraReservada("begin")) {
                token = lexico.getNexToken();

                sentencas();

                if (ehPalavraReservada("end")) {
                    token = lexico.getNexToken();
                } else {
                    throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                            + "]. Erro Sintático => Faltou 'end' no else");
                }
            } else {
                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                        + "]. Erro Sintático => Faltou 'begin' no else");
            }
        }
    }

    // <expressao_logica> ::= <termo_logico> <mais_expr_logica>
    private void expressao_logica() {
        termo_logico();
        mais_expr_logica();
    }

    // <mais_expr_logica> ::= or <termo_logico> {A26} <mais_expr_logica> | ε
    private void mais_expr_logica() {
        if (ehPalavraReservada("or")) {
            token = lexico.getNexToken();
            termo_logico();

            // {A26}
            String rotSaida = criarRotulo("SaidaMEL");
            String rotVerdade = criarRotulo("VerdadeMEL");
            escreverCodigo("\tcmp dword [ESP + 4], 1");
            escreverCodigo("\tje " + rotVerdade);
            escreverCodigo("\tcmp dword [ESP], 1");
            escreverCodigo("\tje " + rotVerdade);
            escreverCodigo("\tmov dword [ESP + 4], 0");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotVerdade;
            escreverCodigo("\tmov dword [ESP + 4], 1");
            rotulo = rotSaida;
            escreverCodigo("\tadd esp, 4");

            mais_expr_logica();
        }
    }

    // <termo_logico> ::= <fator_logico> <mais_termo_logico>
    private void termo_logico() {
        fator_logico();
        mais_termo_logico();
    }

    // <mais_termo_logico> ::= and <fator_logico> {A27} <mais_termo_logico> | ε
    private void mais_termo_logico() {
        if (ehPalavraReservada("and")) {
            token = lexico.getNexToken();
            fator_logico();

            // {A27}
            String rotSaida = criarRotulo("SaidaMTL");
            String rotFalso = criarRotulo("FalsoMTL");
            escreverCodigo("\tcmp dword [ESP + 4], 1");
            escreverCodigo("\tjne " + rotFalso);
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjne " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;

            mais_termo_logico();
        }
    }

    /*
     * <fator_logico> ::= <relacional> |
     * ( <expressao_logica> ) |
     * not <fator_logico> {A28} |
     * true {A29} |
     * false {A30}
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
            String rotFalso = criarRotulo("FalsoFL");
            String rotSaida = criarRotulo("SaidaFL");
            escreverCodigo("\tcmp dword [ESP], 1");
            escreverCodigo("\tjne " + rotFalso);
            escreverCodigo("\tmov dword [ESP], 0");
            escreverCodigo("\tjmp " + rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 1");
            rotulo = rotSaida;
        } else if (ehPalavraReservada("true")) {
            // {A29}
            escreverCodigo("\tpush 1");
            token = lexico.getNexToken();
        } else if (ehPalavraReservada("false")) {
            // {A30}
            escreverCodigo("\tpush 0");
            token = lexico.getNexToken();
        } else {
            relacional();
        }
    }

    /*
     * <relacional> ::= <expressao> = <expressao> {A31} |
     * <expressao> > <expressao> {A32} |
     * <expressao> >= <expressao> {A33} |
     * <expressao> < <expressao> {A34} |
     * <expressao> <= <expressao> {A35} |
     * <expressao> <> <expressao> {A36}
     */
    private void relacional() {
        expressao();

        ClasseToken op = token.getClasse();

        if (op == ClasseToken.Igualdade ||
                op == ClasseToken.Maior ||
                op == ClasseToken.MaiorIgual ||
                op == ClasseToken.Menor ||
                op == ClasseToken.MenorIgual ||
                op == ClasseToken.Diferente) {

            token = lexico.getNexToken();
            expressao();

            switch (op) {
                case Igualdade: {
                    String rotFalso = criarRotulo("FalsoREL");
                    String rotSaida = criarRotulo("SaidaREL");
                    escreverCodigo("\tpop eax");
                    escreverCodigo("\tcmp dword [ESP], eax");
                    escreverCodigo("\tjne " + rotFalso);
                    escreverCodigo("\tmov dword [ESP], 1");
                    escreverCodigo("\tjmp " + rotSaida);
                    rotulo = rotFalso;
                    escreverCodigo("\tmov dword [ESP], 0");
                    rotulo = rotSaida;
                    break;
                }
                case Maior: {
                    String rotFalso = criarRotulo("FalsoREL");
                    String rotSaida = criarRotulo("SaidaREL");
                    escreverCodigo("\tpop eax");
                    escreverCodigo("\tcmp dword [ESP], eax");
                    escreverCodigo("\tjle " + rotFalso);
                    escreverCodigo("\tmov dword [ESP], 1");
                    escreverCodigo("\tjmp " + rotSaida);
                    rotulo = rotFalso;
                    escreverCodigo("\tmov dword [ESP], 0");
                    rotulo = rotSaida;
                    break;
                }
                case MaiorIgual: {
                    String rotFalso = criarRotulo("FalsoREL");
                    String rotSaida = criarRotulo("SaidaREL");
                    escreverCodigo("\tpop eax");
                    escreverCodigo("\tcmp dword [ESP], eax");
                    escreverCodigo("\tjl " + rotFalso);
                    escreverCodigo("\tmov dword [ESP], 1");
                    escreverCodigo("\tjmp " + rotSaida);
                    rotulo = rotFalso;
                    escreverCodigo("\tmov dword [ESP], 0");
                    rotulo = rotSaida;
                    break;
                }
                case Menor: {
                    String rotFalso = criarRotulo("FalsoREL");
                    String rotSaida = criarRotulo("SaidaREL");
                    escreverCodigo("\tpop eax");
                    escreverCodigo("\tcmp dword [ESP], eax");
                    escreverCodigo("\tjge " + rotFalso);
                    escreverCodigo("\tmov dword [ESP], 1");
                    escreverCodigo("\tjmp " + rotSaida);
                    rotulo = rotFalso;
                    escreverCodigo("\tmov dword [ESP], 0");
                    rotulo = rotSaida;
                    break;
                }
                case MenorIgual: {
                    String rotFalso = criarRotulo("FalsoREL");
                    String rotSaida = criarRotulo("SaidaREL");
                    escreverCodigo("\tpop eax");
                    escreverCodigo("\tcmp dword [ESP], eax");
                    escreverCodigo("\tjg " + rotFalso);
                    escreverCodigo("\tmov dword [ESP], 1");
                    escreverCodigo("\tjmp " + rotSaida);
                    rotulo = rotFalso;
                    escreverCodigo("\tmov dword [ESP], 0");
                    rotulo = rotSaida;
                    break;
                }
                case Diferente: {
                    String rotFalso = criarRotulo("FalsoREL");
                    String rotSaida = criarRotulo("SaidaREL");
                    escreverCodigo("\tpop eax");
                    escreverCodigo("\tcmp dword [ESP], eax");
                    escreverCodigo("\tje " + rotFalso);
                    escreverCodigo("\tmov dword [ESP], 1");
                    escreverCodigo("\tjmp " + rotSaida);
                    rotulo = rotFalso;
                    escreverCodigo("\tmov dword [ESP], 0");
                    rotulo = rotSaida;
                    break;
                }
                default:
                    break;
            }

        } else {
            throw new RuntimeException("[linha=" + token.getLinha() +
                    "]. Operador relacional esperado");
        }
    }

    private void expressao() {
        expressao(true);
    }

    private void expressao(boolean validarOperador) {
        termo(validarOperador);

        if (validarOperador && iniciaFator()) {
            throw new RuntimeException(
                    "[linha=" + token.getLinha() + ", coluna=" + token.getColuna() +
                            "]. Erro Sintático => Operador esperado (+, -, *, /)");
        }

        mais_expressao(validarOperador);
    }

    // // <expressao> ::= <termo> <mais_expressao>
    // private void expressao() {
    // termo();

    // if (token.getClasse() == ClasseToken.Identificador ||
    // token.getClasse() == ClasseToken.Inteiro ||
    // token.getClasse() == ClasseToken.AbreParenteses) {

    // throw new RuntimeException(
    // "[linha=" + token.getLinha() + ", coluna=" + token.getColuna() +
    // "]. Erro Sintático => Operador esperado (+, -, *, /)"
    // );
    // }

    // mais_expressao();
    // }

    // // <termo> ::= <fator> <mais_termo>
    // private void termo() {
    // fator();

    // if (token.getClasse() == ClasseToken.Identificador ||
    // token.getClasse() == ClasseToken.Inteiro ||
    // token.getClasse() == ClasseToken.AbreParenteses) {

    // throw new RuntimeException(
    // "[linha=" + token.getLinha() + ", coluna=" + token.getColuna() +
    // "]. Erro Sintático => Operador esperado (*, /)"
    // );
    // }

    // mais_termo();
    // }

    private void termo(boolean validarOperador) {
        fator();

        if (validarOperador && iniciaFator()) {
            throw new RuntimeException(
                    "[linha=" + token.getLinha() + ", coluna=" + token.getColuna() +
                            "]. Erro Sintático => Operador esperado");
        }

        mais_termo(validarOperador);
    }

    /*
     * <mais_termo> ::= * <fator> {A39} <mais_termo> |
     * / <fator> {A40} <mais_termo> | ε
     */
    // private void mais_termo() {
    // if (token.getClasse() == ClasseToken.Multiplicacao ||
    // token.getClasse() == ClasseToken.Divisao) {
    // token = lexico.getNexToken();
    // fator();
    // mais_termo();
    // }
    // }
    private void mais_termo(boolean validarOperador) {
        if (token.getClasse() == ClasseToken.Multiplicacao) {
            token = lexico.getNexToken();
            fator();

            escreverCodigo("\tpop eax");
            escreverCodigo("\timul eax, dword [ESP]");
            escreverCodigo("\tmov dword [ESP], eax");

            mais_termo(validarOperador);
        } else if (token.getClasse() == ClasseToken.Divisao) {
            token = lexico.getNexToken();
            fator();

            escreverCodigo("\tpop ecx");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcdq"); // GEMINI

            escreverCodigo("\tidiv ecx");
            escreverCodigo("\tpush eax");

            mais_termo(validarOperador);
        }
    }

    private boolean iniciaFator() {
        return token.getClasse() == ClasseToken.Identificador ||
                token.getClasse() == ClasseToken.Inteiro ||
                token.getClasse() == ClasseToken.AbreParenteses;
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
        } else if (token.getClasse() == ClasseToken.Identificador) {
            // {A55}
            String variavel = token.getValor().getTexto();
            if (!tabela.isPresent(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada");
                System.exit(-1);
            } else {
                registro = tabela.getRegistro(variavel);
                if (registro.getCategoria() != Categoria.VARIAVEL) {
                    System.err.println("O identificador " + variavel + "não é uma variável. A55");
                    System.exit(-1);
                }
            }
            escreverCodigo("\tpush dword[ebp - " + registro.getOffset() + "]");

            token = lexico.getNexToken();

        } else if (token.getClasse() == ClasseToken.Inteiro) {
            // {A41}
            escreverCodigo("\tpush " + token.getValor().getInteiro());

            token = lexico.getNexToken();
        } else {
            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                    + "]. Erro Sintático => " + token.getClasse() + " não é um <fator>");
        }
    }

    /*
     * <mais_expressao> ::= + <termo> {A37} <mais_expressao> |
     * - <termo> {A38} <mais_expressao> | ε
     */
    // private void mais_expressao() {
    // if (token.getClasse() == ClasseToken.Mais ||
    // token.getClasse() == ClasseToken.Menos) {
    // token = lexico.getNexToken();
    // termo();
    // mais_expressao();
    // }
    // }
    private void mais_expressao(boolean validarOperador) {
        if (token.getClasse() == ClasseToken.Mais) {
            token = lexico.getNexToken();
            termo(validarOperador);

            escreverCodigo("\tpop eax");
            escreverCodigo("\tadd dword[ESP], eax");

            mais_expressao(validarOperador);
        } else if (token.getClasse() == ClasseToken.Menos) {
            token = lexico.getNexToken();
            termo(validarOperador);

            escreverCodigo("\tpop eax");
            escreverCodigo("\tsub dword[ESP], eax");

            mais_expressao(validarOperador);
        }
    }

    /*
     * <exp_write> ::= id {A09} <mais_exp_write> |
     * string {A59} <mais_exp_write> |
     * intnum {A43} <mais_exp_write>
     */
    private void exp_write() {
        // id {A09} <mais_exp_write> |
        if (token.getClasse() == ClasseToken.Identificador) {
            // {A09}
            String variavel = token.getValor().getTexto();
            if (!tabela.isPresent(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada");
                System.exit(-1);
            } else {
                Registro registro = tabela.getRegistro(variavel);
                if (registro.getCategoria() != Categoria.VARIAVEL) {
                    System.err.println("Identificador " + variavel + " não é uma variável");
                    System.exit(-1);
                } else {
                    escreverCodigo("\tpush dword[ebp - " + registro.getOffset() + "]");
                    escreverCodigo("\tpush @Integer");
                    escreverCodigo("\tcall _printf");
                    escreverCodigo("\tadd esp, 8");
                    if (!sectionData.contains("@Integer: db '%d',0")) {
                        sectionData.add("@Integer: db '%d',0");
                    }
                }
            }
            token = lexico.getNexToken();
            mais_exp_write();
        }
        // string {A59} <mais_exp_write> |
        else if (token.getClasse() == ClasseToken.String) {
            // {A59}
            String string = token.getValor().getTexto();
            String rotulo = criarRotulo("String");
            sectionData.add(rotulo + ": db '" + string + "',0");
            escreverCodigo("\tpush " + rotulo);
            escreverCodigo("\tcall _printf");
            escreverCodigo("\tadd esp, 4");
            token = lexico.getNexToken();
            mais_exp_write();
        }
        // intnum {A43} <mais_exp_write>
        else if (token.getClasse() == ClasseToken.Inteiro) {
            // {A43}
            escreverCodigo("\tpush " + token.getValor().getInteiro());
            escreverCodigo("\tpush @Integer");
            escreverCodigo("\tcall _printf");
            escreverCodigo("\tadd esp, 8");
            if (!sectionData.contains("@Integer: db '%d',0")) {
                sectionData.add("@Integer: db '%d',0");
            }
            token = lexico.getNexToken();
            mais_exp_write();
        } else {
            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                    + "]. Erro Sintático => Faltou o operando no Write");
        }
    }

    // <mais_exp_write> ::= , <exp_write> | ε
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
            String variavel = token.getValor().getTexto();
            if (!tabela.isPresent(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada");
                System.exit(-1);
            } else {
                Registro registro = tabela.getRegistro(variavel);
                if (registro.getCategoria() != Categoria.VARIAVEL) {
                    System.err.println("Identificador " + variavel + " não é uma variável");
                    System.exit(-1);
                } else {
                    escreverCodigo("\tmov edx, ebp");
                    escreverCodigo("\tlea eax, [edx - " + registro.getOffset() + "]");
                    escreverCodigo("\tpush eax");
                    escreverCodigo("\tpush @Integer");
                    escreverCodigo("\tcall _scanf");
                    escreverCodigo("\tadd esp, 8");
                    if (!sectionData.contains("@Integer: db '%d',0")) {
                        sectionData.add("@Integer: db '%d',0");
                    }
                }
            }
            token = lexico.getNexToken();
            mais_var_read();
        } else {
            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                    + "]. Erro Sintático => Faltou identificado no read");
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
                ehPalavraReservada("if") || token.getClasse() == ClasseToken.Identificador) {
            sentencas();
        }
    }

    // <dvar> ::= <variaveis> : <tipo_var> {A02}
    private void dvar() {
        variaveis();
        if (token.getClasse() == ClasseToken.DoisPontos) {
            token = lexico.getNexToken();
            tipo_var();
            // {A02}
            int tamanho = 0;
            for (String var : variaveis) {
                tabela.getRegistro(var).setTipo(Tipo.INTEGER);
                tamanho += TAMANHO_INTEIRO;
            }
            escreverCodigo("\tsub esp, " + tamanho);
            variaveis.clear();
        } else {
            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                    + "]. Erro Sintático => Faltou dois pontos (:) na declaracao de variáveis");
        }
    }

    // <variaveis> ::= id {A03} <mais_var>
    private void variaveis() {
        if (token.getClasse() == ClasseToken.Identificador) {
            // {A03}
            String variavel = token.getValor().getTexto();
            if (tabela.isPresent(variavel)) {
                System.err.println("Variável " + variavel + " já foi declarada anteriormente");
                System.exit(-1);
            } else {
                tabela.add(variavel);
                tabela.getRegistro(variavel).setCategoria(Categoria.VARIAVEL);
                tabela.getRegistro(variavel).setOffset(offsetVariavel);
                offsetVariavel += TAMANHO_INTEIRO;
                variaveis.add(variavel);
            }
            token = lexico.getNexToken();
            if (token.getClasse() == ClasseToken.Identificador) {
                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                        + "]. Erro Sintático => Faltou virgula na definição de mais variáveis");
            }
            mais_var();
        } else {
            throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna()
                    + "]. Erro Sintático => Faltou um identificador de variável");
        }
    }

    // <mais_var> ::= , <variaveis> | ε
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
                    + "]. Erro Sintático => Faltou o tipo (INTEGER) das variaveis");
        }
    }

    // <cont_dc> ::= <dvar> <mais_dc> | ε
    private void cont_dc() {
        // if (token.getClasse() == ClasseToken.Identificador &&
        // lexico.peekToken().getClasse() == ClasseToken.DoisPontos) -> implementar
        // peekToken depois

        if (token.getClasse() == ClasseToken.Identificador) {

            dvar();
            mais_dc();
        }
    }

    // <mais_dc> ::= ; <cont_dc>
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