"""Gera HTML imprimível a partir do relatório Markdown (requer Python-Markdown)."""
from pathlib import Path
import markdown

origem = Path(__file__).with_name("RELATORIO.md")
destino = Path(__file__).with_name("RELATORIO.html")
corpo = markdown.markdown(
    origem.read_text(encoding="utf-8"),
    extensions=["tables", "fenced_code", "toc"],
)
estilo = """
<style>
@page { size: A4; margin: 2cm }
body { font: 11pt Arial, sans-serif; line-height: 1.45; color: #222;
       max-width: 900px; margin: auto }
h1 { font-size: 22pt } h2 { font-size: 16pt; border-bottom: 1px solid #888 }
h3 { font-size: 13pt } h2, h3 { break-after: avoid }
table { border-collapse: collapse; width: 100%; margin: 10px 0 }
th, td { border: 1px solid #777; padding: 5px }
th { background: #eee }
pre { white-space: pre-wrap; background: #f5f5f5; border: 1px solid #ccc;
      padding: 8px; font-size: 9pt }
code { font-family: Consolas, monospace }
blockquote { border-left: 4px solid #888; padding-left: 10px; color: #555 }
table, pre { break-inside: avoid }
</style>
"""
pagina = (
    '<!doctype html><html lang="pt-BR"><meta charset="utf-8">'
    "<title>Relatório LPA2v</title>" + estilo + "<body>" + corpo + "</body></html>"
)
destino.write_text(pagina, encoding="utf-8")
