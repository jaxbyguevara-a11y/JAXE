#!/usr/bin/env bash
#
# Crea el keystore de subida de JAXIA y deja listos los 4 secretos de GitHub.
#
# Ejecútalo EN TU COMPUTADOR, no en un servidor ni en un contenedor temporal.
# Las contraseñas se generan en tu máquina y nunca se envían a ningún sitio.
#
#   bash tools/crear-keystore.sh
#
set -euo pipefail

SALIDA="${1:-$HOME/jaxia-claves}"
JKS="$SALIDA/jaxia-upload.jks"
ALIAS="upload"

echo "══════════════════════════════════════════════════════════════"
echo "  JAXIA · creación del keystore de subida"
echo "══════════════════════════════════════════════════════════════"
echo

command -v keytool >/dev/null || {
  echo "ERROR: falta 'keytool'. Instala un JDK (Temurin 21) y reintenta."
  exit 1
}
command -v openssl >/dev/null || {
  echo "ERROR: falta 'openssl'."
  exit 1
}

if [ -e "$JKS" ]; then
  echo "ERROR: ya existe $JKS"
  echo "No lo sobrescribo: si es el keystore de una app publicada, perderías"
  echo "la capacidad de actualizarla. Muévelo a mano si de verdad quieres uno nuevo."
  exit 1
fi

mkdir -p "$SALIDA"
chmod 700 "$SALIDA"

# Contraseñas aleatorias, distintas entre sí. Nunca se imprimen en pantalla.
STORE_PASS="$(openssl rand -base64 32)"
KEY_PASS="$(openssl rand -base64 32)"

echo "Datos del certificado. Puedes dejarlos vacíos salvo el nombre."
read -r -p "  Tu nombre o razón social: " CN
CN="${CN:-JAXIA}"
read -r -p "  Ciudad [Bogota]: " CIUDAD
CIUDAD="${CIUDAD:-Bogota}"
read -r -p "  País, 2 letras [CO]: " PAIS
PAIS="${PAIS:-CO}"
echo

echo "Generando keystore (RSA 4096, PKCS12, validez 10000 días)…"
keytool -genkeypair -v \
  -keystore "$JKS" \
  -storetype PKCS12 \
  -keyalg RSA -keysize 4096 \
  -validity 10000 \
  -alias "$ALIAS" \
  -storepass "$STORE_PASS" \
  -keypass "$KEY_PASS" \
  -dname "CN=$CN, L=$CIUDAD, C=$PAIS" >/dev/null

chmod 600 "$JKS"

# base64 sin saltos de línea; la opción cambia entre Linux y macOS.
if base64 --help 2>&1 | grep -q '\-w'; then
  base64 -w0 "$JKS" > "$SALIDA/KEYSTORE_BASE64.txt"
else
  base64 -i "$JKS" | tr -d '\n' > "$SALIDA/KEYSTORE_BASE64.txt"
fi

printf '%s' "$STORE_PASS" > "$SALIDA/STORE_PASSWORD.txt"
printf '%s' "$KEY_PASS"   > "$SALIDA/KEY_PASSWORD.txt"
printf '%s' "$ALIAS"      > "$SALIDA/KEY_ALIAS.txt"
chmod 600 "$SALIDA"/*.txt

HUELLA="$(keytool -list -v -keystore "$JKS" -storetype PKCS12 -storepass "$STORE_PASS" \
          | grep 'SHA256:' | head -1 | sed 's/.*SHA256: *//')"

cat <<EOF

══════════════════════════════════════════════════════════════
  LISTO
══════════════════════════════════════════════════════════════

Keystore:  $JKS
Huella SHA-256:
  $HUELLA

Anota esa huella: te permitirá confirmar en el futuro que un respaldo es
el keystore correcto.

──────────────────────────────────────────────────────────────
  PASO 1 · Crear los 4 secretos en GitHub
──────────────────────────────────────────────────────────────

Ve a:
  https://github.com/jaxbyguevara-a11y/JAXE/settings/secrets/actions

Pulsa "New repository secret" cuatro veces. Los nombres deben ser EXACTOS:

  Nombre             Contenido (pega el archivo completo)
  ─────────────────  ──────────────────────────────────────────
  KEYSTORE_BASE64    $SALIDA/KEYSTORE_BASE64.txt
  STORE_PASSWORD     $SALIDA/STORE_PASSWORD.txt
  KEY_ALIAS          $SALIDA/KEY_ALIAS.txt
  KEY_PASSWORD       $SALIDA/KEY_PASSWORD.txt

Para copiar cada uno al portapapeles:

  Linux:  xclip -sel clip < $SALIDA/STORE_PASSWORD.txt
  macOS:  pbcopy           < $SALIDA/STORE_PASSWORD.txt

──────────────────────────────────────────────────────────────
  PASO 2 · Respaldar
──────────────────────────────────────────────────────────────

Guarda $JKS y las dos contraseñas en DOS lugares distintos,
por ejemplo un gestor de contraseñas y un disco externo cifrado.

Si pierdes este archivo NO PODRÁS volver a actualizar la app publicada.

──────────────────────────────────────────────────────────────
  PASO 3 · Borrar los .txt
──────────────────────────────────────────────────────────────

Cuando los 4 secretos estén creados en GitHub y tengas el respaldo:

  shred -u $SALIDA/*.txt    # Linux
  rm -P    $SALIDA/*.txt    # macOS

El .jks NO se borra: es tu copia de trabajo.

EOF
