#!/usr/bin/env python3
"""
Genera el material gráfico de JAXIA a partir del logotipo original.

Uso:
    python3 tools/generar-assets.py ruta/a/JAXIA_logo_original.png

Produce, sin recomprimir ni deformar:
    play-assets/icono-512.png          icono de la ficha de Play (PNG 32 bits)
    play-assets/destacado-1024x500.png gráfico destacado
    app/src/main/res/drawable-nodpi/img_jaxia_logo.png   logo de la bienvenida
    app/src/main/res/drawable-nodpi/ic_jaxia_simbolo.png símbolo para el icono

Reglas que respeta:
  - El ICONO usa solo el símbolo (J + silueta + gancho); el texto del logotipo
    es ilegible por debajo de ~96 px.
  - El resto usa el logotipo COMPLETO.
  - Escalado siempre proporcional (LANCZOS). Nunca se estira.
  - Se parte del archivo original; no se re-guarda una versión ya comprimida.
"""
import sys, os
from PIL import Image

CREMA = (247, 244, 241)

def bbox_tinta(im, y0, y1, umbral=190):
    """Recuadro del contenido real, ignorando la sombra suave de la tarjeta."""
    px = im.load(); w, h = im.size
    minx, maxx, miny, maxy = w, 0, h, 0
    for y in range(y0, min(y1, h), 2):
        for x in range(0, w, 2):
            r, g, b = px[x, y][:3]
            if (r + g + b) / 3 < umbral:
                minx, maxx = min(minx, x), max(maxx, x)
                miny, maxy = min(miny, y), max(maxy, y)
    return minx, miny, maxx, maxy

def sobre_lienzo(recorte, lado, ocupacion):
    c = Image.new("RGB", (lado, lado), CREMA)
    w, h = recorte.size
    sc = (lado * ocupacion) / max(w, h)
    nw, nh = max(1, int(w * sc)), max(1, int(h * sc))
    c.paste(recorte.resize((nw, nh), Image.LANCZOS), ((lado - nw) // 2, (lado - nh) // 2))
    return c

def main(origen):
    im = Image.open(origen).convert("RGB")
    W, H = im.size
    print(f"origen: {origen}  {W}x{H}  modo={Image.open(origen).mode}")
    if min(W, H) < 1000:
        print("AVISO: el original mide menos de 1000 px; el icono de Play exige 512 nítidos.")

    # El símbolo ocupa aproximadamente el 11%-55% superior del logotipo.
    sx0, sy0, sx1, sy1 = bbox_tinta(im, int(H * 0.09), int(H * 0.57))
    simbolo = im.crop((sx0, sy0, sx1, sy1))
    print(f"símbolo detectado: {simbolo.size}")

    # Logotipo completo sin la sombra exterior de la tarjeta.
    m = int(min(W, H) * 0.07)
    completo = im.crop((m, m, W - m, H - m))

    os.makedirs("play-assets", exist_ok=True)
    dr = "app/src/main/res/drawable-nodpi"
    os.makedirs(dr, exist_ok=True)

    sobre_lienzo(simbolo, 512, 0.78).save("play-assets/icono-512.png", optimize=True)
    sobre_lienzo(simbolo, 432, 0.60).save(f"{dr}/ic_jaxia_simbolo.png", optimize=True)
    completo.save(f"{dr}/img_jaxia_logo.png", optimize=True)

    g = Image.new("RGB", (1024, 500), CREMA)
    lado = int(500 * 0.88)
    g.paste(completo.resize((lado, lado), Image.LANCZOS), (58, (500 - lado) // 2))
    g.save("play-assets/destacado-1024x500.png", optimize=True)

    print("\ngenerado:")
    for p in ("play-assets/icono-512.png", "play-assets/destacado-1024x500.png",
              f"{dr}/img_jaxia_logo.png", f"{dr}/ic_jaxia_simbolo.png"):
        print(f"  {p:56} {os.path.getsize(p)//1024:>5} KB")
    print("\nEl gráfico destacado queda sin el texto de la derecha: añádelo en el")
    print("editor que prefieras, o pídemelo y lo compongo.")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    main(sys.argv[1])
