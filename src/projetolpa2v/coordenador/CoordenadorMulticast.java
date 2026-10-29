package projetolpa2v.coordenador;

import projetolpa2v.agentes.TipoAgente;
import projetolpa2v.config.Configuracao;
import projetolpa2v.rede.CanalMulticast;
import projetolpa2v.rede.Mensagem;
import projetolpa2v.rede.Registro;
import projetolpa2v.rede.TipoMensagem;

import java.io.IOException;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CoordenadorMulticast implements AutoCloseable {
    private final Configuracao config;
    private final Registro log;
    private final CanalMulticast canal;
    private final ConcurrentMap<String, AvaliacaoEmAndamento> avaliacoes = new ConcurrentHashMap<>();
    private final AtomicBoolean ativo = new AtomicBoolean(true);
    private final Thread receptor;

    public CoordenadorMulticast(Configuracao config) throws IOException {
        this.config = config;
        log = new Registro("COORD");
        canal = new CanalMulticast(config, log);
        receptor = new Thread(this::receber, "receptor-coordenador");
        receptor.start();
    }

    public ResultadoAvaliacao avaliar(DadosAvaliacao dados) throws IOException, InterruptedException {
        String id = UUID.randomUUID().toString();
        AvaliacaoEmAndamento avaliacao = new AvaliacaoEmAndamento(id);
        avaliacoes.put(id, avaliacao);
        for (TipoAgente agente : TipoAgente.values()) {
            canal.enviar(new Mensagem(TipoMensagem.REQ, id, "COORD", agente.name(), dados.para(agente)));
        }
        ResultadoAvaliacao resultado = avaliacao.aguardar(config.timeoutMs());
        // O contexto fica no mapa para que respostas tardias/duplicadas também sejam reconhecidas no log.
        return resultado;
    }

    private void receber() {
        while (ativo.get()) {
            try { filtrar(canal.receber()); }
            catch (SocketException e) { if (ativo.get()) log.erro("Socket: " + e.getMessage()); }
            catch (IOException e) { if (ativo.get()) log.erro("Recepção: " + e.getMessage()); }
        }
    }

    private void filtrar(String texto) {
        Mensagem m;
        try { m = Mensagem.interpretar(texto); }
        catch (IllegalArgumentException e) { log.descartada("protocolo inválido: " + e.getMessage(), texto); return; }
        if (m.origem().equals("COORD")) { log.descartada("mensagem de coordenador", texto); return; }
        if (!m.destino().equals("COORD")) { log.descartada("destinada a " + m.destino(), texto); return; }
        AvaliacaoEmAndamento avaliacao = avaliacoes.get(m.id());
        if (avaliacao == null) { log.descartada("identificador desconhecido (possível outro coordenador)", texto); return; }
        avaliacao.registrar(m, log);
    }

    @Override public void close() {
        if (!ativo.compareAndSet(true, false)) return;
        try { canal.close(); } catch (IOException e) { log.erro("Falha ao sair do grupo: " + e.getMessage()); }
        try { receptor.join(2_000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        log.info("Coordenador encerrado.");
        try { log.close(); } catch (IOException ignored) { }
    }
}
