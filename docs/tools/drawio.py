"""Мини-библиотека для генерации схем draw.io (.drawio) и их HTML-превью.

Используется скриптом docs/tools/diagrams.py. Готовые .drawio открываются в draw.io
(app.diagrams.net или draw.io Desktop) и редактируются как обычные схемы.
"""
import html
import itertools

FONT = "Times New Roman"


class Diagram:
    def __init__(self, name, width, height):
        self.name = name
        self.width = width
        self.height = height
        self.cells = []
        self.ids = itertools.count(2)

    def _id(self):
        return "c%d" % next(self.ids)

    def vertex(self, x, y, w, h, value, style, parent="1"):
        cid = self._id()
        self.cells.append(dict(kind="v", id=cid, x=x, y=y, w=w, h=h, value=value, style=style, parent=parent))
        return cid

    def edge(self, source, target, style, value="", points=None):
        cid = self._id()
        self.cells.append(dict(kind="e", id=cid, source=source, target=target, style=style, value=value,
                               points=points or []))
        return cid

    # ------------------------------------------------------------------ вывод
    def to_drawio(self):
        out = ['<mxfile host="drawio" agent="russian-resale docs" version="24.0.0">',
               '  <diagram id="%s" name="%s">' % (self.name.replace(" ", "_"), html.escape(self.name)),
               '    <mxGraphModel dx="1000" dy="1000" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" '
               'arrows="1" fold="1" page="1" pageScale="1" pageWidth="%d" pageHeight="%d" math="0" shadow="0">'
               % (self.width, self.height),
               '      <root>', '        <mxCell id="0" />', '        <mxCell id="1" parent="0" />']
        for c in self.cells:
            value = html.escape(c["value"], quote=True)
            style = html.escape(c["style"], quote=True)
            if c["kind"] == "v":
                out.append('        <mxCell id="%s" value="%s" style="%s" vertex="1" parent="%s">'
                           % (c["id"], value, style, c["parent"]))
                out.append('          <mxGeometry x="%d" y="%d" width="%d" height="%d" as="geometry" />'
                           % (c["x"], c["y"], c["w"], c["h"]))
            else:
                out.append('        <mxCell id="%s" value="%s" style="%s" edge="1" parent="1" source="%s" target="%s">'
                           % (c["id"], value, style, c["source"], c["target"]))
                if c["points"]:
                    out.append('          <mxGeometry relative="1" as="geometry">')
                    out.append('            <Array as="points">')
                    for px, py in c["points"]:
                        out.append('              <mxPoint x="%d" y="%d" />' % (px, py))
                    out.append('            </Array>')
                    out.append('          </mxGeometry>')
                else:
                    out.append('          <mxGeometry relative="1" as="geometry" />')
            out.append('        </mxCell>')
        out += ['      </root>', '    </mxGraphModel>', '  </diagram>', '</mxfile>', '']
        return "\n".join(out)

    def to_preview_html(self):
        """Грубое превью: прямоугольники с подписями и прямые линии между центрами (для проверки раскладки)."""
        boxes = {c["id"]: c for c in self.cells if c["kind"] == "v"}
        parts = ['<html><head><meta charset="utf-8"><style>body{margin:0;font-family:"%s";}'
                 '.v{position:absolute;box-sizing:border-box;overflow:hidden;font-size:11px;}</style></head><body>'
                 % FONT,
                 '<div style="position:relative;width:%dpx;height:%dpx;background:#fff">' % (self.width, self.height)]
        svg = ['<svg style="position:absolute;left:0;top:0" width="%d" height="%d">' % (self.width, self.height)]
        for c in self.cells:
            if c["kind"] != "e":
                continue
            s, t = boxes.get(c["source"]), boxes.get(c["target"])
            if not s or not t:
                continue
            pts = [(s["x"] + s["w"] / 2, s["y"] + s["h"] / 2)] + list(c["points"]) + [(t["x"] + t["w"] / 2, t["y"] + t["h"] / 2)]
            svg.append('<polyline fill="none" stroke="#c33" stroke-width="1" points="%s"/>'
                       % " ".join("%d,%d" % p for p in pts))
        svg.append('</svg>')
        parts += svg
        for c in self.cells:
            if c["kind"] != "v":
                continue
            st = c["style"]
            border = "none" if "strokeColor=none" in st else "1px solid #000"
            radius = "50%" if ("ellipse" in st) else ("12px" if "rounded=1" in st else "0")
            bg = "#fff"
            if "fillColor=#000000" in st:
                bg = "#000"
            elif "fillColor=" in st:
                bg = st.split("fillColor=")[1].split(";")[0]
            if "rhombus" in st:
                radius = "0"
                border = "1px dashed #00f"
            parts.append('<div class="v" style="left:%dpx;top:%dpx;width:%dpx;height:%dpx;border:%s;'
                         'border-radius:%s;background:%s">%s</div>'
                         % (c["x"], c["y"], c["w"], c["h"], border, radius, bg if bg != "none" else "transparent",
                            c["value"]))
        parts.append('</div></body></html>')
        return "\n".join(parts)


# ---------------------------------------------------------------------- стили
def box_style(rounded=False, fill="#FFFFFF", bold=False, size=12, align="center", dashed=False):
    return ("rounded=%d;whiteSpace=wrap;html=1;fillColor=%s;strokeColor=#000000;fontColor=#000000;fontFamily=%s;fontSize=%d;"
            "fontStyle=%d;align=%s;verticalAlign=middle;arcSize=20;%s"
            % (1 if rounded else 0, fill, FONT, size, 1 if bold else 0, align, "dashed=1;" if dashed else ""))


def decision_style(size=11):
    return ("shape=rhombus;perimeter=rhombusPerimeter;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=#000000;fontColor=#000000;fontFamily=%s;fontSize=%d;"
            % (FONT, size))


def text_style(size=12, bold=False, align="center"):
    return ("text;html=1;strokeColor=none;fillColor=none;fontColor=#000000;align=%s;verticalAlign=middle;whiteSpace=wrap;"
            "fontFamily=%s;fontSize=%d;fontStyle=%d;" % (align, FONT, size, 1 if bold else 0))


def uml_style(size=11):
    return ("verticalAlign=top;align=left;overflow=fill;html=1;whiteSpace=wrap;fillColor=#FFFFFF;"
            "strokeColor=#000000;fontColor=#000000;fontFamily=%s;fontSize=%d;spacing=0;" % (FONT, size))


def edge_style(arrow="block", start="none", dashed=False, size=11, extra=""):
    return ("edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;endArrow=%s;"
            "endFill=%d;startArrow=%s;startFill=0;strokeColor=#000000;fontColor=#000000;fontFamily=%s;fontSize=%d;%s%s"
            % (arrow, 0 if arrow in ("open", "diamond", "none") else 1, start, FONT, size,
               "dashed=1;" if dashed else "", extra))


def uml_class(title, attrs, methods=(), stereotype=None):
    """HTML-подпись UML-класса: имя, атрибуты, методы."""
    head = ""
    if stereotype:
        head += "<i>&laquo;%s&raquo;</i><br>" % stereotype
    head += "<b>%s</b>" % title
    rows = '<p style="margin:0;margin-top:4px;text-align:center;">%s</p>' % head
    rows += '<hr size="1" style="border-style:solid;">'
    rows += '<p style="margin:0;margin-left:5px;">%s</p>' % "<br>".join(attrs)
    if methods:
        rows += '<hr size="1" style="border-style:solid;">'
        rows += '<p style="margin:0;margin-left:5px;">%s</p>' % "<br>".join(methods)
    return rows


def db_table(title, fields):
    """HTML-подпись таблицы БД: строки (ключ, имя, тип)."""
    rows = ('<table style="width:100%%;border-collapse:collapse;font-size:10px;">'
            '<tr><td colspan="3" style="background:#D9D9D9;text-align:center;border-bottom:1px solid #000;'
            'padding:2px;"><b>%s</b></td></tr>' % title)
    for key, name, typ in fields:
        rows += ('<tr><td style="width:24px;padding-left:3px;"><b>%s</b></td><td>%s</td>'
                 '<td style="text-align:right;padding-right:4px;color:#333;">%s</td></tr>' % (key, name, typ))
    rows += '</table>'
    return rows
