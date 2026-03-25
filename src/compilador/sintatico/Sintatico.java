package compilador.sintatico;

import compilador.lexico.ClasseToken;
import compilador.lexico.Lexico;
import compilador.lexico.Token;

public class Sintatico {

    private Lexico lexico;
    private Token token;

    public Sintatico(Lexico lexico) {
        this.lexico = lexico;
    }

    public void analisar() {
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
                                + "]. Erro Sintático => Faltou ponto e virgula depois do nome do programa (.)");
                }
            } else {
                throw new RuntimeException("[linha=" + token.getLinha() + ", coluna=" + token.getColuna() 
                                        + "]. Erro Sintático => Faltou começar o programa com Program (.)");
            }
        }
    }

    private boolean ehPalavraReservada(String palavra) {
        return token.getClasse() == ClasseToken.PalavraReservada 
            && token.getValor().getTexto().equalsIgnoreCase(palavra);
    }

}