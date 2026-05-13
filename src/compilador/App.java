package compilador;

import java.io.IOException;
import java.nio.file.Paths;
import compilador.sintatico.Sintatico;

public class App {
    public static void main(String[] args) throws Exception {

        String caminhoArquivo = Paths.get("queronemver.asm").toAbsolutePath().toString();
        String caminho = caminhoArquivo.substring(0, caminhoArquivo.indexOf("queronemver.asm"));
        ProcessBuilder processBuilder;
        Process process;
        int exitCode;

        Sintatico sintatico = new Sintatico("arquivo_pascal_teste.pas");
        sintatico.analisar();

        try {

            processBuilder = new ProcessBuilder(
                    "C:\\msys64\\mingw32\\bin\\nasm.exe",
                    "-f", "win32",
                    caminho + "queronemver.asm",
                    "-o",
                    caminho + "queronemver.o");

            processBuilder.inheritIO();

            process = processBuilder.start();
            exitCode = process.waitFor();

            System.out.println("NASM exit code: " + exitCode);

            processBuilder = new ProcessBuilder(
                    "C:\\msys64\\mingw32\\bin\\gcc.exe",
                    "-m32",
                    "-mconsole",
                    "-o",
                    caminho + "queronemver.exe",
                    caminho + "queronemver.o");

            processBuilder.inheritIO();

            process = processBuilder.start();
            exitCode = process.waitFor();

            System.out.println("GCC exit code: " + exitCode);
            if (exitCode != 0) {
                System.out.println("Falha no GCC.");
                return;
            }

            processBuilder = new ProcessBuilder(
                    caminho + "queronemver.exe");

            processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            process = processBuilder.start();
            exitCode = process.waitFor();

            System.out.println("Programa exit code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}