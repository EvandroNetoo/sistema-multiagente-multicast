package projetolpa2v.agentes;

@FunctionalInterface
public interface CalculadorEvidencia {
    double calcular(String dados) throws EntradaInvalidaException;
}
