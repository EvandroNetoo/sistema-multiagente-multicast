package projetolpa2v.logica;

import java.util.Locale;

public final class ResultadoLpa2v {
    private final NoLpa2v nap1;
    private final NoLpa2v nap2;
    private final NoLpa2v nap3;
    private final EstadoLogico estado;
    private final String classificacao;

    public ResultadoLpa2v(NoLpa2v nap1, NoLpa2v nap2, NoLpa2v nap3,
                          EstadoLogico estado, String classificacao) {
        this.nap1 = nap1;
        this.nap2 = nap2;
        this.nap3 = nap3;
        this.estado = estado;
        this.classificacao = classificacao;
    }

    public NoLpa2v nap1() { return nap1; }
    public NoLpa2v nap2() { return nap2; }
    public NoLpa2v nap3() { return nap3; }
    public EstadoLogico estado() { return estado; }
    public String classificacao() { return classificacao; }

    public String relatorio() {
        StringBuilder s = new StringBuilder();
        s.append(String.format(Locale.US, "%-5s %8s %8s %8s %8s %8s%n", "Nó", "μ", "λ", "Gc", "Gct", "μE"));
        linha(s, nap1); linha(s, nap2); linha(s, nap3);
        s.append("Estado lógico: ").append(estado.descricao()).append(System.lineSeparator());
        s.append("Classificação: ").append(classificacao).append(System.lineSeparator());
        return s.toString();
    }

    private static void linha(StringBuilder s, NoLpa2v n) {
        s.append(String.format(Locale.US, "%-5s %8.3f %8.3f %8.3f %8.3f %8.3f%n",
                n.nome(), n.mu(), n.lambda(), n.grauCerteza(), n.grauContradicao(), n.evidenciaResultante()));
    }
}
