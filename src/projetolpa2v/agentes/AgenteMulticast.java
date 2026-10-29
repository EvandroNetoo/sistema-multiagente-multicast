package projetolpa2v.agentes;

import projetolpa2v.config.Configuracao;
import projetolpa2v.rede.CanalMulticast;
import projetolpa2v.rede.Mensagem;
import projetolpa2v.rede.Registro;
import projetolpa2v.rede.TipoMensagem;

import java.io.IOException;
import java.net.SocketException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Receptor concorrente comum aos quatro agentes. */
public final class AgenteMulticast implements AutoCloseable {
    private final TipoAgente tipo;
    private final CalculadorEvidencia calculador;
    private final Registro log;
    private final CanalMulticast canal;
    private final ExecutorService pool;
    private final Set<String> avaliacoesRecebidas = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean ativo = new AtomicBoolean(true);

    public AgenteMulticast(TipoAgente tipo, Configuracao config) throws IOException {
        this.tipo = tipo;
        calculador = Calculadores.para(tipo);
        log = new Registro(tipo.name());
        canal = new CanalMulticast(config, log);
        pool = Executors.newFixedThreadPool(config.threads());
    }

    public void executar() throws IOException {
        log.info("Agente pronto. Digite SAIR e Enter para encerrar.");
        while (ativo.get()) {
            try {
                String texto = canal.receber();
                receber(texto);
            } catch (SocketException e) {
                if (ativo.get()) throw e;
            }
        }
    }

    private void receber(String texto) {
        final Mensagem mensagem;
        try {
            mensagem = Mensagem.interpretar(texto);
        } catch (IllegalArgumentException e) {
            log.descartada("protocolo inválido: " + e.getMessage(), texto);
            return;
        }
        if (mensagem.origem().equals(tipo.name())) {
            log.descartada("mensagem da própria origem", texto); return;
        }
        if (!mensagem.destino().equals(tipo.name())) {
            log.descartada("destinada a " + mensagem.destino(), texto); return;
        }
        if (mensagem.tipo() != TipoMensagem.REQ || !mensagem.origem().equals("COORD")) {
            log.descartada("tipo ou origem incompatível", texto); return;
        }
        if (!avaliacoesRecebidas.add(mensagem.id())) {
            log.descartada("requisição duplicada", texto); return;
        }
        pool.submit(() -> processar(mensagem));
    }

    private void processar(Mensagem requisicao) {
        Mensagem resposta;
        try {
            double mu = calculador.calcular(requisicao.dados());
            // O fio preserva o double; três casas são aplicadas apenas no relatório do coordenador.
            String dados = String.format(Locale.US, "%.17g", mu);
            resposta = new Mensagem(TipoMensagem.RESP, requisicao.id(), tipo.name(), "COORD", dados);
        } catch (EntradaInvalidaException e) {
            String erro = e.getMessage().replace(';', ',');
            resposta = new Mensagem(TipoMensagem.ERRO, requisicao.id(), tipo.name(), "COORD", erro);
        }
        try {
            canal.enviar(resposta);
        } catch (IOException e) {
            log.erro("Falha no envio da resposta de " + requisicao.id() + ": " + e.getMessage());
        }
    }

    @Override public void close() {
        if (!ativo.compareAndSet(true, false)) return;
        try { canal.close(); } catch (IOException e) { log.erro("Falha ao sair do grupo: " + e.getMessage()); }
        pool.shutdown();
        try {
            if (!pool.awaitTermination(2, TimeUnit.SECONDS)) pool.shutdownNow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); pool.shutdownNow();
        }
        log.info("Agente encerrado.");
        try { log.close(); } catch (IOException ignored) { }
    }
}
