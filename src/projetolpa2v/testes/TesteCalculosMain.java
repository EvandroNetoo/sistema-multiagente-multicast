package projetolpa2v.testes;

import projetolpa2v.agentes.Calculadores;
import projetolpa2v.agentes.EntradaInvalidaException;
import projetolpa2v.logica.EstadoLogico;
import projetolpa2v.logica.RedeLpa2v;
import projetolpa2v.logica.ResultadoLpa2v;
import projetolpa2v.rede.Mensagem;

/** Testes determinísticos sem biblioteca externa. */
public final class TesteCalculosMain {
    private static int executados;

    private TesteCalculosMain() { }

    public static void main(String[] args) throws Exception {
        caso("0,0,0,0,0", "0,0,0,0,0", "115,75", "65,1.70",
                0, 0, 0, 0, -1, 0, EstadoLogico.FALSO);
        caso("4,3,3,1,2", "3,2,1,2,1", "185,115", "125,1.70",
                1, 1, 1, 1, 1, 0, EstadoLogico.VERDADEIRO);
        caso("4,2,2,0,0", "2,2,1,2,0", "150,95", "92,1.75",
                .615, .778, .500, .336, .115, .279, EstadoLogico.NAO_EXTREMO);
        caso("4,3,3,1,2", "3,2,1,2,1", "115,75", "65,1.70",
                1, 1, 0, 0, 0, 1, EstadoLogico.INCONSISTENTE);
        caso("0,0,0,0,0", "0,0,0,0,0", "185,115", "125,1.70",
                0, 0, 1, 1, 0, -1, EstadoLogico.PARACOMPLETO);
        caso("2,1,1,0,0", "1,1,0,1,0", "135,85", "80,1.70",
                .308, .333, .250, .179, -.465, .106, EstadoLogico.NAO_EXTREMO);
        entradaInvalida();
        protocolo();
        System.out.println("OK: " + executados + " verificações passaram.");
    }

    private static void caso(String t, String s, String pa, String imc,
                             double et, double es, double epa, double eimc,
                             double gc, double gct, EstadoLogico estado) throws Exception {
        double mt = Calculadores.tabagismo(t);
        double ms = Calculadores.sedentarismo(s);
        double mp = Calculadores.pressao(pa);
        double mi = Calculadores.imc(imc);
        perto(mt, et); perto(ms, es); perto(mp, epa); perto(mi, eimc);
        ResultadoLpa2v r = RedeLpa2v.consolidar(mt, ms, mp, mi);
        perto(r.nap3().grauCerteza(), gc);
        perto(r.nap3().grauContradicao(), gct);
        igual(r.estado(), estado);
    }

    private static void entradaInvalida() throws Exception {
        boolean falhou = false;
        try { Calculadores.imc("80,0"); } catch (EntradaInvalidaException e) { falhou = true; }
        if (!falhou) throw new AssertionError("Altura zero deveria ser recusada");
        executados++;
        falhou = false;
        try { Calculadores.tabagismo("3,0,0,0,0"); } catch (EntradaInvalidaException e) { falhou = true; }
        if (!falhou) throw new AssertionError("Alternativa inexistente deveria ser recusada");
        executados++;
    }

    private static void protocolo() {
        String texto = "RESP;a3f9c2;PRESSAO;COORD;0.500";
        igual(Mensagem.interpretar(texto).serializar(), texto);
    }

    private static void perto(double obtido, double esperado) {
        if (Math.abs(obtido - esperado) > 0.0010001) {
            throw new AssertionError("Esperado " + esperado + ", obtido " + obtido);
        }
        executados++;
    }

    private static void igual(Object obtido, Object esperado) {
        if (!obtido.equals(esperado)) throw new AssertionError("Esperado " + esperado + ", obtido " + obtido);
        executados++;
    }
}
