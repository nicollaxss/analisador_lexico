package compilador.lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Lexico {

    private String nomeArquivo;
    private BufferedReader br;
    private char caractere;
    private static final List<String> palavrasReservadas = Arrays.asList("const", "type", "var", "begin", "end", "while", "do", "for", "downto", "if", "then", "else", "case", "of", "array", "function", "procedure", "label", "record", "exit", "break", "continue", "and", "or", "not", "integer", "program", "write", "writeln", "read", "repeat", "until");
    private int linha;
    private int coluna;

    public Lexico(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
        linha = 1;
        coluna = 1;
        String caminhoArquivo = Paths.get(nomeArquivo).toAbsolutePath().toString();
        try {
            br = new BufferedReader(new FileReader(caminhoArquivo, StandardCharsets.UTF_8));
            caractere = (char) br.read();
        } catch (IOException ex) {
            System.out.println("Erro abrindo o arquivo " + nomeArquivo);
            System.out.println("Caminho do arquivo: " + caminhoArquivo);
        }
    }

    public Token getNexToken() {
        StringBuilder lexema;
        Token token;

        try {
            while (caractere != 65535) { // EOF
                lexema = new StringBuilder();
                token = new Token(linha, coluna);

                if (Character.isDigit(caractere)) {
                    while (Character.isDigit(caractere)) {
                        lexema.append(caractere);
                        caractere = (char) br.read();
                        coluna++;
                    }
                    token.setClasse(ClasseToken.Inteiro);
                    token.setValor(new ValorToken(Integer.parseInt(lexema.toString())));
                    return token;
                } else if (Character.isAlphabetic(caractere))  {
                    while (Character.isAlphabetic(caractere) || Character.isDigit(caractere)) {
                        lexema.append(caractere);
                        caractere = (char) br.read();
                        coluna++;
                    }
                    if (palavrasReservadas.contains(lexema.toString().toLowerCase())) {
                        token.setClasse(ClasseToken.PalavraReservada);

                        // se precisar verificar o tipo

                    } else {
                        token.setClasse(ClasseToken.Identificador);
                    }

                    token.setValor(new ValorToken(lexema.toString().toLowerCase()));
                    return token;
                } else if (caractere == ' ' || caractere == '\t') {
                    caractere = (char) br.read(); // ler o proximo caractere
                    coluna++;
                    
                } else if (caractere == '\r') {
                    caractere = (char) br.read(); // ler o proximo caractere
                    if (caractere == '\n') {
                        caractere = (char) br.read();
                        linha++;
                        coluna = 1;
                    }

                }

                // corrigido
                else if (caractere == '+') {
                    caractere = (char) br.read();
                    coluna++;
                    token.setClasse(ClasseToken.Mais);
                    return token;
                }

                // corrigido
                else if (caractere == '-') {
                    caractere = (char) br.read();
                    coluna++;
                    token.setClasse(ClasseToken.Menos);
                    return token;
                }

                // nao precisa corrigir contagem de coluna
                else if (caractere == '/') {
                    caractere = (char) br.read();

                    // reconhecer comentários e ignorar eles
                    if (caractere == '/') {
                        caractere = (char) br.read();
                        while (caractere != '\r') {
                            // System.out.println(caractere);
                            caractere = (char) br.read();
                            if (caractere == '\n') {
                                // System.out.println("Entao, como te conto...");
                                caractere = (char) br.read();
                                break;
                            } else if (caractere == 65535) {
                                break;
                            }
                        }
                    }
                    else {
                        coluna++;
                        token.setClasse(ClasseToken.Divisao);
                        return token;
                    }
                }
                // corrigido
                else if (caractere == '=') {
                    caractere = (char) br.read();
                    coluna++;
                    token.setClasse(ClasseToken.Igualdade);
                    return token;
                }
                // corrigido
                else if (caractere == ':') {
                    caractere = (char) br.read();
                    coluna++;
                    if (caractere == '=') {
                        caractere = (char) br.read();
                        coluna++;
                        token.setClasse(ClasseToken.Atribuicao);
                        return token;
                    } else {
                        token.setClasse(ClasseToken.DoisPontos);
                        return token;
                    }
                }
                
                // corrigido
                else if (caractere == '>') {
                    caractere = (char) br.read();
                    coluna++;
                    if (caractere == '=') {
                        caractere = (char) br.read();
                        coluna++;
                        token.setClasse(ClasseToken.MaiorIgual);
                        return token;
                    } else {
                        token.setClasse(ClasseToken.Maior);
                        return token;
                    }
                }

                // CLASSE PADRAO PARA ARRUMAR AS OUTRAS
                else if (caractere == '<') {
                    caractere = (char) br.read();
                    coluna++;
                    if (caractere == '=') {
                        caractere = (char) br.read();
                        coluna++;
                        token.setClasse(ClasseToken.MenorIgual);
                        return token;
                    } else if (caractere == '>') {
                        caractere = (char) br.read();
                        coluna++;
                        token.setClasse(ClasseToken.Diferente);
                        return token;
                    } else {
                        token.setClasse(ClasseToken.Menor);
                        return token;
                    }
                }

                // corrigido
                else if (caractere == ';') {
                    caractere = (char) br.read();
                    coluna++;
                    token.setClasse(ClasseToken.PontoVirgula);
                    return token;
                }
                // corrigido
                else if (caractere == ',') {
                    caractere = (char) br.read();
                    coluna++;
                    token.setClasse(ClasseToken.Virgula);
                    return token;
                }
                // corrigido
                else if (caractere == '(') {
                    caractere = (char) br.read();
                    coluna++;
                    token.setClasse(ClasseToken.AbreParenteses);
                    return token;
                }
                // corrigido
                else if (caractere == ')') {
                    caractere = (char) br.read();
                    coluna++;
                    token.setClasse(ClasseToken.FechaParenteses);
                    return token;
                }
                // corrigido
                else if (caractere == '.') {
                    caractere = (char) br.read();
                    coluna++;
                    token.setClasse(ClasseToken.Ponto);
                    return 
                    token;
                }
                // corrigido
                else if (caractere == '*') {
                    caractere = (char) br.read();
                    coluna++;
                    token.setClasse(ClasseToken.Multiplicacao);
                    return token;
                }
                
                // reconhecer bloco de comentários:
                else if (caractere == '{' ) {
                    caractere = (char) br.read();

                    while (caractere != '}') {
                        caractere = (char) br.read();

                        if (caractere == '\r') {
                            caractere = (char) br.read();
                            if (caractere == '\n') {
                                linha++;
                                coluna = 1;
                                caractere = (char) br.read();
                            }
                        }
                    
                        // fim do bloco de comentários
                        if (caractere == '}') {
                            // não preciso me preocupar com o /r/n, já que após o fim -> ele pega o /r/n e atualiza linha e col
                            caractere = (char) br.read();
                            break;
                        }
                        else if (caractere == 65535) {
                            // erro lexico, faltou fechar o bloco de comentário
                            System.out.println("Erro léxico faltou fechar comentário }\nLinha: " + linha + "\nColuna: " + coluna);
                            System.exit(1);
                        }
                    }
                }
                
                // reconhecer strings
                else if (caractere == '\'') {
                    caractere = (char) br.read();
                    coluna++;
                    while (caractere != '\'') {
                        lexema.append(caractere);
                        // System.out.println(lexema);
                        caractere = (char) br.read();
                        coluna++;

                        // ainda nao sei como mostrar que deu erro
                        if (caractere == 65535) {
                            System.out.println("Faltou fechar a string '\nLinha: " + linha + "\nColuna: " + coluna);
                            System.exit(1);
                        }
                        else if (caractere == '\r') {
                            caractere = (char) br.read();
                            if (caractere == '\n') {
                                System.out.println("Faltou fechar a string '\nLinha: " + linha + "\nColuna: " + coluna);
                                System.exit(1);
                            }
                        }
                        else if (caractere == '\''){
                            caractere = (char) br.read();
                            coluna++;
                            token.setClasse(ClasseToken.String);
                            token.setValor(new ValorToken(lexema.toString()));
                            return token;
                        }
                    }
                }

            }
            token = new Token(linha, coluna);
            token.setClasse(ClasseToken.EOF);
            return token;
        } catch (IOException e) {
            System.err.println("Não foi possível ler do arquivo: " + nomeArquivo);
        }
        return null;
    }
}