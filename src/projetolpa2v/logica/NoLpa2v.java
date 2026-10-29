package projetolpa2v.logica;

/** Resultado de um Nó de Análise Paraconsistente. */
public final class NoLpa2v {
    private final String nome;
    private final double mu;
    private final double lambda;
    private final double grauCerteza;
    private final double grauContradicao;
    private final double evidenciaResultante;

    private NoLpa2v(String nome, double mu, double lambda) {
        this.nome = nome;
        this.mu = limitar(mu);
        this.lambda = limitar(lambda);
        grauCerteza = this.mu - this.lambda;
        grauContradicao = this.mu + this.lambda - 1.0;
        evidenciaResultante = (grauCerteza + 1.0) / 2.0;
    }

    public static NoLpa2v calcular(String nome, double mu, double lambda) {
        if (!Double.isFinite(mu) || !Double.isFinite(lambda)) {
            throw new IllegalArgumentException("Evidências devem ser números finitos");
        }
        return new NoLpa2v(nome, mu, lambda);
    }

    public static double limitar(double valor) { return Math.max(0.0, Math.min(1.0, valor)); }
    public String nome() { return nome; }
    public double mu() { return mu; }
    public double lambda() { return lambda; }
    public double grauCerteza() { return grauCerteza; }
    public double grauContradicao() { return grauContradicao; }
    public double evidenciaResultante() { return evidenciaResultante; }
}
