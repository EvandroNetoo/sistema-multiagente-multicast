package projetolpa2v.rede;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Log simultâneo em tela e em logs/, com data e hora. */
public final class Registro implements AutoCloseable {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final String processo;
    private final BufferedWriter arquivo;

    public Registro(String processo) throws IOException {
        this.processo = processo;
        Path pasta = Path.of("logs");
        Files.createDirectories(pasta);
        String sufixo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"))
                + "-p" + ProcessHandle.current().pid();
        Path caminho = pasta.resolve(processo.toLowerCase() + "-" + sufixo + ".log");
        arquivo = Files.newBufferedWriter(caminho, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        registrar("INFO", "Log iniciado em " + caminho.toAbsolutePath());
    }

    public synchronized void enviada(Mensagem m) { registrar("ENVIADA", m.serializar()); }
    public synchronized void recebida(String texto) { registrar("RECEBIDA", texto); }
    public synchronized void descartada(String motivo, String texto) { registrar("DESCARTADA", motivo + " | " + texto); }
    public synchronized void info(String texto) { registrar("INFO", texto); }
    public synchronized void erro(String texto) { registrar("ERRO", texto); }

    private void registrar(String evento, String texto) {
        String linha = String.format("%s [%s] [%s] %s", LocalDateTime.now().format(DATA), processo, evento, texto);
        System.out.println(linha);
        try {
            arquivo.write(linha);
            arquivo.newLine();
            arquivo.flush();
        } catch (IOException e) {
            System.err.println("Falha ao gravar log: " + e.getMessage());
        }
    }

    @Override public synchronized void close() throws IOException { arquivo.close(); }
}
