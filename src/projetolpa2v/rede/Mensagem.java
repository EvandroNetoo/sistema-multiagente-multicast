package projetolpa2v.rede;

import java.util.Objects;

/** Protocolo: TIPO;ID;ORIGEM;DESTINO;DADOS. */
public final class Mensagem {
    private final TipoMensagem tipo;
    private final String id;
    private final String origem;
    private final String destino;
    private final String dados;

    public Mensagem(TipoMensagem tipo, String id, String origem, String destino, String dados) {
        this.tipo = Objects.requireNonNull(tipo);
        this.id = campo(id, "id");
        this.origem = campo(origem, "origem");
        this.destino = campo(destino, "destino");
        this.dados = campo(dados, "dados");
    }

    private static String campo(String valor, String nome) {
        if (valor == null || valor.isBlank() || valor.contains(";") || valor.contains("\n") || valor.contains("\r")) {
            throw new IllegalArgumentException("Campo " + nome + " inválido");
        }
        return valor;
    }

    public static Mensagem interpretar(String texto) {
        String[] p = texto.split(";", 5);
        if (p.length != 5) throw new IllegalArgumentException("Mensagem deve possuir cinco campos");
        try {
            return new Mensagem(TipoMensagem.valueOf(p[0]), p[1], p[2], p[3], p[4]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Mensagem inválida: " + e.getMessage(), e);
        }
    }

    public String serializar() { return tipo + ";" + id + ";" + origem + ";" + destino + ";" + dados; }
    public TipoMensagem tipo() { return tipo; }
    public String id() { return id; }
    public String origem() { return origem; }
    public String destino() { return destino; }
    public String dados() { return dados; }

    @Override public String toString() { return serializar(); }
}
