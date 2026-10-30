# Projeto LPA2v — risco cardiológico por agentes multicast

Aplicação didática distribuída em Java 17. Quatro agentes calculam evidências de
tabagismo, sedentarismo, pressão arterial e IMC; um coordenador as consolida por
uma rede de três Nós de Análise Paraconsistente (LPA2v).

> As escalas são simplificações didáticas. Este software não é instrumento
> clínico e não deve ser usado para avaliar pessoas reais.

## Integrantes

Evandro, Joel, Lucas, Arhur Alves e Iago.

## Pré-requisitos e compilação

- JDK 17 ou superior (um JRE sozinho não contém o compilador `javac`);
- conectividade IPv4 multicast entre as máquinas;
- todos os processos com o mesmo grupo, porta e interface da rede do laboratório.

No NetBeans, selecione uma plataforma JDK 17+ e use **Clean and Build**. Pela
linha de comando, na raiz do projeto:

```powershell
New-Item -ItemType Directory -Force build/classes | Out-Null
javac -encoding UTF-8 -d build/classes (Get-ChildItem src -Recurse -Filter *.java).FullName
java -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -cp build/classes projetolpa2v.testes.TesteCalculosMain
```

Linux/macOS:

```bash
mkdir -p build/classes
javac -encoding UTF-8 -d build/classes $(find src -name '*.java')
java -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -cp build/classes projetolpa2v.testes.TesteCalculosMain
```

## Descobrir a interface de rede

Execute `java -cp build/classes projetolpa2v.rede.ListarInterfacesMain` para ver
os nomes reconhecidos pela JVM. No Windows, `Get-NetIPConfiguration`, e no
Linux, `ip -br address`, também mostram o IPv4. Use o nome interno, nome exibido
pela JVM ou o IPv4 local. A
interface é obrigatória; o programa não deixa o sistema operacional escolher.

## Execução dos cinco processos

O grupo de alunos é `G=4`, portanto os padrões são `239.0.0.4:5004`. Em cinco
terminais, substitua `192.168.1.20` pelo IPv4 da respectiva máquina:

```powershell
$cp = "build/classes"
$jvm = @("-Dfile.encoding=UTF-8", "-Djava.net.preferIPv4Stack=true")
$rede = @("--iface", "192.168.1.20", "--grupo-numero", "4")
java $jvm -cp $cp projetolpa2v.agentes.AgenteTabagismoMain $rede
java $jvm -cp $cp projetolpa2v.agentes.AgenteSedentarismoMain $rede
java $jvm -cp $cp projetolpa2v.agentes.AgentePressaoMain $rede
java $jvm -cp $cp projetolpa2v.agentes.AgenteImcMain $rede
java $jvm -cp $cp projetolpa2v.coordenador.CoordenadorMain $rede
```

Cada agente possui seu próprio `main` e deve ser iniciado em processo separado.
Na demonstração em três máquinas, distribua os quatro agentes por pelo menos
duas máquinas e coloque o coordenador na terceira.

Parâmetros aceitos por todos os processos:

| Parâmetro | Padrão | Finalidade |
|---|---:|---|
| `--iface NOME_OU_IP` | obrigatório | interface IPv4 multicast explícita |
| `--grupo-numero G` | `4` | deriva `239.0.0.G` e porta `5000+G` |
| `--grupo ENDERECO` | derivado | sobrescreve o endereço |
| `--porta PORTA` | derivada | sobrescreve a porta |
| `--timeout MS` | `3000` | espera máxima do coordenador |
| `--threads N` | `4` | tamanho do pool de cada agente |

Use ponto em números reais. Para encerrar corretamente, digite `SAIR` nos
agentes e escolha `0` no coordenador. Todos executam `leaveGroup` antes de
fechar o socket. `Ctrl+C` também aciona o gancho de encerramento.

## Operação e testes

No coordenador, a opção 3 executa os seis casos obrigatórios. Os cálculos puros
podem ser verificados sem rede por `TesteCalculosMain`.

Roteiro de laboratório:

1. Inicie os quatro agentes e execute os seis casos.
2. Encerre o IMC, execute o caso 3 e confira timeout/avaliação incompleta.
3. Reinicie apenas o IMC e repita o caso 3.
4. Inicie dois coordenadores e execute simultaneamente os casos 1 e 2.
5. Inicie um segundo Tabagismo e execute o caso 3; procure `resposta duplicada` no log.
6. Faça uma avaliação manual com altura `0`; confira `ERRO` e avaliação incompleta.

Os arquivos são gravados em `logs/`. Preserve esses logs e faça capturas dos
terminais para incluir como evidência real no relatório.

## Protocolo

Formato UTF-8: `TIPO;ID;ORIGEM;DESTINO;DADOS`.

```text
REQ;a3f9c2;COORD;TABAGISMO;4,2,2,0,0
RESP;a3f9c2;TABAGISMO;COORD;0.615
ERRO;a3f9c2;IMC;COORD;altura fora da faixa plausível (0.50 a 2.50 m)
```

Todo tráfego usa o mesmo grupo nos dois sentidos. Cada processo recebe e
registra todos os datagramas e descarta os que não lhe pertencem. O UUID evita
mistura entre avaliações e coordenadores. Uma resposta repetida é ignorada e
registrada. Erro ou ausência torna a avaliação incompleta: o coordenador nunca
inventa evidências. O agente envia precisão suficiente para preservar o
`double`; o coordenador usa `Locale.US` e arredonda para três casas somente na
apresentação ao usuário.

## Estrutura principal

- `projetolpa2v.config`: parâmetros de execução;
- `projetolpa2v.rede`: socket, protocolo e logs;
- `projetolpa2v.agentes`: quatro processos e escalas obrigatórias;
- `projetolpa2v.coordenador`: interface, timeout e correlação;
- `projetolpa2v.logica`: três NAPs e classificação LPA2v;
- `projetolpa2v.testes`: testes determinísticos dos seis casos.

O relatório-base está em `relatorio/RELATORIO.md`. Adicione
instituição/disciplina/professor e insira as capturas reais antes da entrega.
Os diagramas editáveis estão em `relatorio/diagramas/*.mmd` e são renderizados
como SVG pelo Mermaid CLI; o PDF incorpora esses vetores sem perda de qualidade.
