package projetolpa2v.coordenador;

import projetolpa2v.config.Configuracao;

import java.util.Scanner;

/** Interface textual do processo coordenador. */
public final class CoordenadorMain {
    private CoordenadorMain() { }

    public static void main(String[] args) throws Exception {
        try {
            Configuracao config = Configuracao.ler(args);
            CoordenadorMulticast coordenador = new CoordenadorMulticast(config);
            Runtime.getRuntime().addShutdownHook(new Thread(coordenador::close, "encerramento-coord"));
            try (Scanner entrada = new Scanner(System.in)) {
                executarMenu(entrada, coordenador);
            } finally {
                coordenador.close();
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println("Uso: " + Configuracao.uso());
        }
    }

    private static void executarMenu(Scanner entrada, CoordenadorMulticast coordenador) throws Exception {
        while (true) {
            System.out.println("\nAVALIAÇÃO DIDÁTICA DE RISCO CARDIOLÓGICO — não é instrumento clínico");
            System.out.println("1 - Nova avaliação manual");
            System.out.println("2 - Executar um caso de teste (1 a 6)");
            System.out.println("3 - Executar os seis casos de teste");
            System.out.println("0 - Sair");
            System.out.print("Opção: ");
            String opcao = entrada.nextLine().trim();
            if (opcao.equals("0") || opcao.equalsIgnoreCase("sair")) return;
            if (opcao.equals("1")) {
                avaliar(coordenador, lerDados(entrada));
            } else if (opcao.equals("2")) {
                System.out.print("Número do caso: ");
                try { avaliar(coordenador, CasosTeste.obter(Integer.parseInt(entrada.nextLine().trim()))); }
                catch (IllegalArgumentException e) { System.out.println("Caso inválido: " + e.getMessage()); }
            } else if (opcao.equals("3")) {
                for (int i = 1; i <= 6; i++) {
                    System.out.println("\n######## CASO " + i + " ########");
                    avaliar(coordenador, CasosTeste.obter(i));
                }
            } else {
                System.out.println("Opção inválida.");
            }
        }
    }

    private static void avaliar(CoordenadorMulticast coordenador, DadosAvaliacao dados) throws Exception {
        ResultadoAvaliacao resultado = coordenador.avaliar(dados);
        System.out.println(resultado.relatorio());
    }

    private static DadosAvaliacao lerDados(Scanner e) {
        System.out.println("Informe os PONTOS de cada alternativa; a pontuação será calculada pelo agente.");
        System.out.println("T1: nunca=0; parou >5 anos=1; parou <5 anos=2; fumante=4");
        String t1 = ler(e, "T1");
        System.out.println("T2: nenhum=0; 1-10=1; 11-20=2; >20=3");
        String t2 = ler(e, "T2");
        System.out.println("T3: nenhum=0; <10 anos=1; 10-20=2; >20=3");
        String t3 = ler(e, "T3");
        System.out.println("T4: não=0; sim=1");
        String t4 = ler(e, "T4");
        System.out.println("T5: não=0; ocasionalmente=1; diariamente=2");
        String t5 = ler(e, "T5");

        System.out.println("S1: >=150 min=0; 75-149=1; 1-74=2; nenhum=3");
        String s1 = ler(e, "S1");
        System.out.println("S2: <4 h=0; 4-8=1; >8=2");
        String s2 = ler(e, "S2");
        System.out.println("S3: desloca-se a pé/bicicleta: sim=0; não=1");
        String s3 = ler(e, "S3");
        System.out.println("S4: força >=2 vezes=0; 1 vez=1; nenhuma=2");
        String s4 = ler(e, "S4");
        System.out.println("S5: tarefas vigorosas: sim=0; não=1");
        String s5 = ler(e, "S5");

        String pas = ler(e, "Pressão sistólica (mmHg)");
        String pad = ler(e, "Pressão diastólica (mmHg)");
        String peso = ler(e, "Peso (kg, ponto decimal)");
        String altura = ler(e, "Altura (m, ponto decimal)");
        return new DadosAvaliacao(String.join(",", t1, t2, t3, t4, t5),
                String.join(",", s1, s2, s3, s4, s5), pas + "," + pad, peso + "," + altura);
    }

    private static String ler(Scanner entrada, String rotulo) {
        System.out.print(rotulo + ": ");
        return entrada.nextLine().trim();
    }
}
