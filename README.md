# JAXIA

**Vístete con lo que tienes.** Aplicación Android para gestión consciente del
clóset: organiza tus prendas, arma looks con lo que ya posees, visualízalos en un
avatar, sigue el costo por uso y comparte lo que ya no usas.

Funciona **completamente sin conexión**. Todos los datos viven en el dispositivo.

---

## Estado del proyecto

Este repositorio contiene el proyecto tras una auditoría técnica integral.
Lee **[`AUDITORIA.md`](AUDITORIA.md)** para el detalle completo de hallazgos,
severidades y correcciones aplicadas.

> **Nota importante sobre la "IA":** pese al nombre, JAXIA **no utiliza
> inteligencia artificial**. El generador de looks es un motor de reglas
> determinista. Las dependencias de Firebase AI, Retrofit, OkHttp, Moshi y Coil
> que venían declaradas nunca se usaban en el código y fueron eliminadas.
> La decisión sobre incorporar IA real está planteada en `AUDITORIA.md` §7.

---

## Stack

| Componente | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material 3 |
| Persistencia | Room 2.7.0 (KSP) |
| Build | AGP 9.1.1 · Gradle 9.3.1 · JDK 17 |
| `minSdk` / `targetSdk` | 24 / 36 (Android 16) |
| Tests | JUnit 4 · Robolectric · Roborazzi |

---

## Compilar

**Requisitos:** JDK 21 y Android SDK con la plataforma 36.

> JDK 21 es necesario para los tests: Robolectric exige Java 21 para crear su
> sandbox de Android SDK 36. El bytecode sigue compilándose a nivel 17.

```bash
./gradlew assembleDebug        # APK de depuración
./gradlew testDebugUnitTest    # Tests unitarios
./gradlew bundleRelease        # AAB de release
```

No hace falta instalar Gradle: el wrapper lo descarga.

---

## Firmar el release

La firma se toma **exclusivamente de variables de entorno**; en el repositorio no
hay ninguna credencial. Si faltan, el release compila igualmente pero sin firmar.

```bash
export KEYSTORE_PATH=/ruta/absoluta/a/upload-key.jks
export STORE_PASSWORD='…'
export KEY_ALIAS=upload
export KEY_PASSWORD='…'

./gradlew bundleRelease
# → app/build/outputs/bundle/release/app-release.aab
```

Crear el keystore de subida (una sola vez):

```bash
keytool -genkeypair -v \
  -keystore upload-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias upload
```

> ⚠️ **Guarda copia del keystore y de sus contraseñas en un lugar seguro.**
> Si lo pierdes no podrás volver a actualizar la app publicada sin solicitar a
> Google un restablecimiento de la clave de subida.

El keystore está excluido por `.gitignore`. **Nunca lo subas al repositorio.**

---

## Integración continua

`.github/workflows/android.yml` compila, ejecuta los tests y genera el AAB en
cada push. Para que el artefacto salga firmado, define estos secretos del
repositorio:

| Secreto | Contenido |
|---|---|
| `KEYSTORE_BASE64` | El `.jks` codificado: `base64 -w0 upload-key.jks` |
| `STORE_PASSWORD` | Contraseña del keystore |
| `KEY_ALIAS` | Alias de la clave (por defecto `upload`) |
| `KEY_PASSWORD` | Contraseña de la clave |

---

## Antes de publicar en Google Play

Pendientes que requieren decisiones o activos tuyos — detalle en
`AUDITORIA.md` §8, Fase 4:

- [ ] Confirmar el `applicationId` definitivo (**es irreversible** tras la primera subida)
- [ ] Publicar [`PRIVACIDAD.md`](PRIVACIDAD.md) en una URL pública y completar sus campos
- [ ] Rellenar el formulario de Data Safety en Play Console
- [ ] Icono propio de 512×512 y gráfico destacado de 1024×500
- [ ] Mínimo 2 capturas de pantalla por tipo de dispositivo
- [ ] Definir público objetivo y clasificación de contenido

---

## Estructura

```
app/src/main/java/com/jaxia/app/
├── JaxiaApplication.kt         # Singleton de base de datos y repositorio
├── MainActivity.kt             # Navegación por pestañas
├── data/
│   ├── model/                  # Entidades Room + inspiración diaria
│   ├── dao/                    # Consultas Room
│   ├── database/               # AppDatabase + migraciones
│   ├── repository/             # Lógica de negocio y motor de looks
│   └── DefaultSeedData.kt      # Contenido de ejemplo del primer arranque
└── ui/
    ├── JaxiaViewModel.kt
    ├── format/                 # Formato de moneda es-CO
    ├── screens/                # Inicio, Vísteme, Probador, Clóset, Ahorro, Comunidad
    ├── components/             # Diálogos y avatar
    └── theme/                  # Paleta, tipografía, tema
```
