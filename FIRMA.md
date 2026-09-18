# Firma del AAB — guía segura

Cómo generar el keystore de subida de JAXIA y configurar los cuatro secretos del
repositorio, **sin que ninguna credencial toque el código**.

> **Regla que no se rompe:** el `.jks` y sus contraseñas **nunca** se escriben en
> el repositorio, ni en `build.gradle.kts`, ni en `gradle.properties`, ni en el
> workflow, ni en un comentario, ni en un commit "temporal". Git guarda historia:
> un secreto commiteado y luego borrado **sigue siendo recuperable**.

---

## 0. Cómo está montado hoy

`app/build.gradle.kts` no contiene ninguna credencial. Lee cuatro valores, en
este orden de prioridad:

| Valor | Variable de entorno | Alternativa `-P` | Si falta |
|---|---|---|---|
| Ruta del keystore | `KEYSTORE_PATH` | `-PKEYSTORE_PATH` | build sin firmar |
| Contraseña del almacén | `STORE_PASSWORD` | `-PSTORE_PASSWORD` | build sin firmar |
| Alias de la clave | `KEY_ALIAS` | `-PKEY_ALIAS` | usa `upload` |
| Contraseña de la clave | `KEY_PASSWORD` | `-PKEY_PASSWORD` | build sin firmar |

Si alguno falta, `releaseSigningAvailable` es `false` y el release **compila sin
firmar** en lugar de romper el build. Eso es deliberado: permite que los forks y
las pull requests construyan sin tener acceso a tus credenciales.

---

## 1. Generar el keystore de subida

Una sola vez, en tu equipo. **No lo generes en el CI ni en un contenedor
efímero**: si lo pierdes, pierdes la capacidad de actualizar la app.

```bash
keytool -genkeypair -v \
  -keystore jaxia-upload.jks \
  -storetype PKCS12 \
  -keyalg RSA -keysize 4096 \
  -validity 10000 \
  -alias upload
```

Notas de cada opción:

- **`-storetype PKCS12`** — formato estándar actual. El antiguo JKS es propietario
  y `keytool` avisa de que está obsoleto.
- **`-keysize 4096`** — Google exige mínimo 2048; 4096 no cuesta nada y aguanta
  mejor el paso del tiempo.
- **`-validity 10000`** (≈27 años) — Google exige que la clave sea válida al menos
  hasta el 22 de octubre de 2033. Con 10000 días te sobra.
- **`-alias upload`** — coincide con el valor por defecto del proyecto. Si usas
  otro, tendrás que declarar el secreto `KEY_ALIAS`.

`keytool` te pedirá **dos contraseñas**: la del almacén (*store*) y la de la clave
(*key*). Pueden ser distintas; es preferible que lo sean.

### Generar contraseñas fuertes

No las inventes a mano. En Linux/macOS:

```bash
openssl rand -base64 32   # ejecútalo dos veces: una por contraseña
```

Guárdalas en un **gestor de contraseñas** (1Password, Bitwarden, KeePassXC).
No en notas del móvil, no en un `.txt`, no en WhatsApp, no en un correo a ti mismo.

---

## 2. Respaldar el keystore

**Esto es lo más importante de este documento.**

Si pierdes `jaxia-upload.jks` o sus contraseñas, **no puedes volver a publicar
actualizaciones** de la app. Google permite solicitar un restablecimiento de la
clave de subida, pero es un trámite lento y no siempre concedido.

Haz **al menos dos copias en sitios distintos**, por ejemplo:

1. Gestor de contraseñas que admita adjuntos (1Password, Bitwarden).
2. Disco externo cifrado, guardado físicamente aparte.

Verifica que el respaldo sirve antes de confiar en él:

```bash
keytool -list -v -keystore jaxia-upload.jks -storetype PKCS12
```

Debe listar el alias `upload` y mostrar la huella SHA-256. Anota esa huella: te
permitirá confirmar en el futuro que un respaldo es el keystore correcto.

---

## 3. Protección de clave de la app (Play App Signing)

Google Play usa **dos** claves:

| Clave | Quién la tiene | Para qué |
|---|---|---|
| **De subida** (la que acabas de crear) | Tú | Firmar el AAB que subes a Play Console |
| **De firma de la app** | Google | Firmar los APK que se instalan en los dispositivos |

Al crear la app en Play Console, acepta **Play App Signing** (es obligatorio para
apps nuevas). Google genera y custodia la clave de firma; tú solo manejas la de
subida. La ventaja práctica: si pierdes la clave de subida, el daño es
recuperable; si fuese la de firma, no lo sería.

---

## 4. Firmar en local

Nunca escribas las contraseñas en el historial del shell. Usa `read -s`, que no
las muestra ni las guarda:

```bash
export KEYSTORE_PATH="$HOME/claves/jaxia-upload.jks"
export KEY_ALIAS="upload"
read -rs -p "Store password: " STORE_PASSWORD && export STORE_PASSWORD && echo
read -rs -p "Key password: "   KEY_PASSWORD   && export KEY_PASSWORD   && echo

./gradlew bundleRelease
unset STORE_PASSWORD KEY_PASSWORD
```

Resultado: `app/build/outputs/bundle/release/app-release.aab`

### Comprobar que quedó firmado

```bash
# Ruta típica; ajusta la versión de build-tools
"$ANDROID_HOME/build-tools/36.0.0/apksigner" verify --print-certs --verbose \
  app/build/outputs/bundle/release/app-release.aab
```

La huella SHA-256 que imprima debe coincidir con la que anotaste en el paso 2.

> **Alternativa a las variables de entorno:** un archivo
> `keystore.properties` **fuera del repositorio** (por ejemplo en `~/.gradle/`)
> con `-PKEYSTORE_PATH=...`. Si lo pones dentro del proyecto, ya está en
> `.gitignore`, pero sigue siendo más fácil filtrarlo por error.

---

## 5. Configurar los cuatro secretos en GitHub

### 5.1 Codificar el keystore

GitHub Secrets solo guarda texto, así que el `.jks` (binario) va en base64:

```bash
base64 -w0 jaxia-upload.jks > jaxia-upload.jks.b64   # Linux
base64 -i  jaxia-upload.jks -o jaxia-upload.jks.b64  # macOS
```

`-w0` / `-i` evitan los saltos de línea, que romperían el decodificado.

### 5.2 Crear los secretos

En **Settings → Secrets and variables → Actions → New repository secret**:

| Nombre del secreto | Contenido |
|---|---|
| `KEYSTORE_BASE64` | Todo el contenido de `jaxia-upload.jks.b64` |
| `STORE_PASSWORD` | La contraseña del almacén |
| `KEY_ALIAS` | `upload` |
| `KEY_PASSWORD` | La contraseña de la clave |

Los nombres deben ser **exactos**: así los lee `.github/workflows/android.yml`.

### 5.3 Borrar el archivo intermedio

```bash
shred -u jaxia-upload.jks.b64   # Linux
rm -P  jaxia-upload.jks.b64     # macOS
```

---

## 6. Por qué esto es seguro

- **Cifrado en reposo.** GitHub cifra los secretos y solo los descifra dentro del
  runner que los necesita.
- **Enmascarado en los logs.** Si un secreto apareciera por accidente en la
  salida, GitHub lo sustituye por `***`. No confíes en ello como única defensa,
  pero es una red útil.
- **No llegan a las pull requests.** El workflow tiene
  `if: github.event_name != 'pull_request'` en el paso de decodificación. Una PR
  desde un fork **no** puede leer tus secretos, ni siquiera modificando el
  workflow en su rama: GitHub no los inyecta.
- **El keystore vive en `RUNNER_TEMP`**, que se destruye al terminar el job, y
  nunca en el árbol de trabajo (donde podría acabar en un artefacto).
- **Ningún artefacto lo contiene.** Se publican el AAB y el `mapping.txt`; el
  `.jks` no.

### Lo que sigue siendo tu responsabilidad

- **Quien tenga permiso de escritura en el repositorio puede usar los secretos**
  para firmar. No es un fallo de configuración: es cómo funcionan las Actions.
  Limita los colaboradores con acceso de escritura.
- **Un workflow malicioso mergeado a una rama puede exfiltrar secretos.** Revisa
  siempre los cambios en `.github/workflows/`.
- **Considera un Environment protegido** (Settings → Environments) con revisores
  obligatorios si quieres que la firma requiera aprobación humana.

---

## 7. Si sospechas que se filtró

1. **No borres el keystore.** Lo necesitas para subir la versión que lo sustituya.
2. En Play Console: **Configuración → Integridad de la app → solicitar
   restablecimiento de la clave de subida**.
3. Genera un keystore nuevo siguiendo el paso 1.
4. Sustituye los cuatro secretos.
5. Revoca los tokens y revisa el registro de accesos del repositorio.

La clave **de firma de la app** la custodia Google, así que una filtración de tu
clave de subida **no** permite a nadie publicar actualizaciones en tu ficha: solo
subir candidatos, que Google rechazará una vez restablecida.

---

## 8. Lista de verificación

- [ ] Keystore generado con PKCS12, RSA 4096, validez ≥ 10000 días
- [ ] Contraseñas generadas con `openssl rand`, guardadas en gestor de contraseñas
- [ ] Huella SHA-256 anotada
- [ ] Dos respaldos en ubicaciones distintas, y **verificados**
- [ ] Cuatro secretos creados en GitHub con los nombres exactos
- [ ] Archivo `.b64` intermedio destruido
- [ ] `git status` limpio: ningún `.jks` ni `.b64` aparece
- [ ] CI en verde y el AAB reporta "Keystore configurado: el AAB saldrá firmado."
- [ ] `apksigner verify` muestra la huella esperada
