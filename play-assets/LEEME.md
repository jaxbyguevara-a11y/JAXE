# Material gráfico para Google Play

## Estado: PROVISIONAL

Los dos archivos `PROVISIONAL-*` se generaron desde el **WebP recomprimido del
chat**, no desde el logotipo original. Sirven para revisar encuadre y
composición; **no deben subirse a Play Console**.

## Para generar los definitivos

1. Coloca `JAXIA_logo_original.png` en la raíz del repositorio
2. Ejecuta:

```bash
python3 tools/generar-assets.py JAXIA_logo_original.png
```

Eso produce, escalando siempre de forma proporcional y sin deformar:

| Archivo | Uso |
|---|---|
| `play-assets/icono-512.png` | Icono de la ficha de Play |
| `play-assets/destacado-1024x500.png` | Gráfico destacado |
| `app/src/main/res/drawable-nodpi/img_jaxia_logo.png` | Pantalla de bienvenida |
| `app/src/main/res/drawable-nodpi/ic_jaxia_simbolo.png` | Icono del launcher |

3. Borra los `PROVISIONAL-*`

## Regla

**El icono usa solo el símbolo** (J + silueta + gancho). El logotipo completo, con
wordmark y lema, se reserva para la bienvenida y el material promocional: por
debajo de unos 96 px ese texto es ilegible.
