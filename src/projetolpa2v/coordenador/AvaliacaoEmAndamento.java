package projetolpa2v.coordenador;

import projetolpa2v.agentes.TipoAgente;
import projetolpa2v.rede.Mensagem;
import projetolpa2v.rede.Registro;
import projetolpa2v.rede.TipoMensagem;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

final class AvaliacaoEmAndamento {
    private final String id;
    private final EnumMap<TipoAgente, Double> respostas = new EnumMap<>(TipoAgente.class);
    private final EnumMap<TipoAgente, String> erros = new EnumMap<>(TipoAgente.class);
    private final CountDownLatch pendentes = new CountDownLatch(TipoAgente.values().length);

    AvaliacaoEmAndamento(String id) { this.id = id; }

    synchronized void registrar(Mensagem mensagem, Registro log) {
        TipoAgente agente;
        try { agente = TipoAgente.valueOf(mensagem.origem()); }
        catch (IllegalArgumentException e) {
            log.descartada("origem não é agente conhecido", mensagem.serializar()); return;
        }
        if (respostas.containsKey(agente) || erros.containsKey(agente)) {
            log.descartada("resposta duplicada de " + agente + " para " + id, mensagem.serializar()); return;
        }
        if (mensagem.tipo() == TipoMensagem.ERRO) {
            erros.put(agente, mensagem.dados());
        } else if (mensagem.tipo() == TipoMensagem.RESP) {
            try {
                double mu = Double.parseDouble(mensagem.dados());
                if (!Double.isFinite(mu) || mu < 0 || mu > 1) throw new NumberFormatException();
                respostas.put(agente, mu);
            } catch (NumberFormatException e) {
                erros.put(agente, "grau de evidência inválido: " + mensagem.dados());
            }
        } else {
            log.descartada("tipo não é resposta ou erro", mensagem.serializar()); return;
        }
        pendentes.countDown();
    }

    ResultadoAvaliacao aguardar(int timeoutMs) throws InterruptedException {
        pendentes.await(timeoutMs, TimeUnit.MILLISECONDS);
        synchronized (this) {
            EnumSet<TipoAgente> ausentes = EnumSet.allOf(TipoAgente.class);
            ausentes.removeAll(respostas.keySet());
            ausentes.removeAll(erros.keySet());
            return new ResultadoAvaliacao(id, respostas, erros, ausentes);
        }
    }
}
