# Auditoría Técnica Integral — JAXIA

**Aplicación:** JAXIA — "Vístete con lo que tienes"
**Nombre auditado:** el proyecto se entregó como *LookIA* y se renombró a **JAXIA**
durante este trabajo. Los identificadores `LookIA*` que aparecen citados abajo son
el código **original**, tal como estaba al auditarlo.
**Repositorio:** `jaxbyguevara-a11y/JAXE`
**Fecha:** 18 de septiembre de 2026
**Alcance:** 62 archivos · 27 fuentes Kotlin · 8.249 líneas
**Origen:** Exportación de Google AI Studio (`ai.studio/apps/137c0a35-…`)
**Estado auditado:** commit `c63e446` (snapshot sin modificar)

---

## 1. Resumen ejecutivo

JAXIA es una app Android nativa (Kotlin + Jetpack Compose + Room) para gestión
de clóset consciente, con avatar virtual, comunidad de intercambio e inspiración
espiritual diaria. La base de UI es **sólida y bien estructurada**: la separación
en capas (`data/model` → `dao` → `repository` → `ui`) es correcta, el uso de
`Flow` + `StateFlow` es idiomático, y el diseño visual es coherente y cuidado.

Sin embargo, **la aplicación no es publicable en su estado actual**. Se
identificaron **7 bloqueantes**, de los cuales dos son de naturaleza legal y de
política de tienda, no técnica:

1. **El "escaneo corporal" es una simulación**, pero la interfaz afirma al
   usuario que se capturan tres fotografías, que existe *"cifrado local de
   medidas"* y que *"nunca son compartidas con terceros"*. Ninguna de esas
   afirmaciones tiene implementación detrás. Esto encaja en
   **Misrepresentation / Deceptive Behavior** de Google Play y es el riesgo más
   grave del proyecto.
2. **El contador de ahorro presenta cifras fabricadas como datos reales.** La
   fórmula multiplica por constantes inventadas y garantiza un mínimo, de modo
   que un usuario recién instalado ve *"Has ahorrado $420.000 COP"* sin haber
   usado la app.

Adicionalmente, el proyecto **no compila de forma reproducible** (falta el Gradle
Wrapper) y **el build de release falla siempre** (firma mal configurada).

### Hallazgo estructural: la "IA" no existe

El nombre comercial es **JAXIA**, el `metadata.json` declara la capacidad
`MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API`, y el `build.gradle.kts` incluye
`firebase-ai`, `retrofit`, `okhttp`, `moshi` y `coil`.

**Ninguna de esas librerías se importa en una sola línea de código.** La búsqueda
sobre las 27 fuentes Kotlin devuelve cero referencias a Firebase, Gemini,
`GenerativeModel`, Retrofit, OkHttp o Coil. No hay una sola llamada de red en
toda la aplicación.

El "generador de outfits" (`LookIARepository.generateOutfitsForOccasion`) es un
conjunto de heurísticas fijas basadas en `firstOrNull()`, `lastOrNull()` y
`contains("Blazer")`. Produce **siempre las mismas tres propuestas** con títulos
constantes, ignorando por completo los parámetros `occasion` y `mood` que recibe.

Esto no es un defecto en sí mismo —un motor de reglas puede ser una decisión de
producto perfectamente válida y más barata que un LLM—, pero **sí lo es
presentarlo como inteligencia artificial**. Es una decisión de producto que debes
tomar conscientemente, y la desarrollo en §7.

---

## 2. Bloqueantes

Impiden compilar, publicar, o exponen a suspensión de la cuenta de desarrollador.

### B-01 · No compila de forma reproducible — falta el Gradle Wrapper

`gradle/wrapper/gradle-wrapper.properties` existe y apunta a Gradle 9.3.1, pero
**faltan `gradlew`, `gradlew.bat` y `gradle-wrapper.jar`**.

Sin el wrapper no hay build reproducible: cada máquina usa la versión de Gradle
que tenga instalada. AGP 9.1.1 exige Gradle 9.x; una máquina con Gradle 8.x falla
con un error de compatibilidad poco descriptivo. Ningún sistema de CI puede
construir este proyecto.

---

### B-02 · `namespace = "com.example"`

```kotlin
android {
  namespace = "com.example"          // ← paquete de plantilla sin personalizar
  applicationId = "com.aistudio.lookia.vstclr"
}
```

Todo el código vive bajo `com.example.*`. El `applicationId` sí es propio, así
que Play no lo rechazará por este motivo, pero:

- `R` y `BuildConfig` se generan en `com.example`, contaminando el espacio de
  nombres y rompiendo si alguna dependencia futura usa el mismo paquete.
- Es la señal más visible de plantilla sin personalizar, y los revisores de Play
  la usan como indicador de baja calidad.
- El sufijo `vstclr` del `applicationId` es un identificador autogenerado, no una
  marca. **El `applicationId` es inmutable tras la primera publicación**: una vez
  subido, no se puede cambiar nunca sin crear una ficha nueva desde cero.

---

### B-03 · El build de release falla siempre — firma mal configurada

```kotlin
create("release") {
  val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
  storeFile = file(keystorePath)                  // ← este archivo no existe
  storePassword = System.getenv("STORE_PASSWORD") // ← null si no está definida
  keyAlias = "upload"
  keyPassword = System.getenv("KEY_PASSWORD")     // ← null si no está definida
}
```

`signingConfigs` se evalúa **en fase de configuración**, no de ejecución. Como
`my-upload-key.jks` no existe y las variables de entorno no están definidas,
cualquier invocación de Gradle —incluso `./gradlew tasks`— arrastra una
configuración de firma inválida, y `bundleRelease` falla con un error de
keystore. No hay ruta posible hacia un AAB firmado con esta configuración.

Además el `README.md` instruye al usuario a *"eliminar la línea
`signingConfig = signingConfigs.getByName("debugConfig")`"*, lo que deja el build
de debug sin firma en lugar de resolver el problema real.

---

### B-04 · Release sin R8 — `isMinifyEnabled = false`

```kotlin
release {
  isCrunchPngs = false
  isMinifyEnabled = false   // ← sin ofuscación, sin shrinking
  proguardFiles(...)        // se declaran reglas que nunca se aplican
}
```

Consecuencias: el bundle incluye todo el código muerto (incluidas las ~9
dependencias jamás usadas de §A-01), el código Kotlin es trivialmente
descompilable, y no se eliminan recursos sin usar. `isCrunchPngs = false`
además desactiva la compresión de PNG.

---

### B-05 · 🔴 Afirmaciones de privacidad sin implementación — riesgo de suspensión

**Este es el hallazgo más grave de la auditoría.**

En `ProbadorAvatarScreen.kt:762-919`, la pantalla "Opción A: Escanear cuerpo"
muestra al usuario, textualmente:

> • Tus fotografías nunca son visibles públicamente ni compartidas con terceros.
> • Se utilizan exclusivamente en tu dispositivo para deducir proporciones del avatar.
> • **Cifrado local de medidas.**
> • Prohibido el uso para publicidad.
> • Puedes eliminar tus datos definitivamente en cualquier momento.

Y presenta tres tarjetas —"Foto 1: Frente", "Foto 2: Perfil lateral", "Foto 3:
Espalda"— que cambian a estado **"Listo"** con un check verde.

**Nada de eso ocurre.** El botón "Iniciar escaneo guiado" ejecuta:

```kotlin
onClick = {
    scanSimulated = true
    onSaveProfile(profile.copy(creationMethod = "scan", hasScanConsent = true))
}
```

Es decir: activa un booleano. No hay CameraX (las dependencias están
comentadas), no se declara el permiso `CAMERA` en el manifiesto, no existe
captura de imagen, no existe cifrado, y los campos `scanFrontPhoto`,
`scanProfilePhoto` y `scanBackPhoto` de la entidad `AvatarProfile` **siempre
permanecen vacíos**. La app luego etiqueta ese avatar como *"Escaneo 3D"*
(`AvatarVisualizer.kt:543`).

Por qué es crítico:

| Problema | Consecuencia |
|---|---|
| Se afirma "cifrado local" inexistente | Declaración falsa sobre seguridad de datos |
| Se afirma capturar fotos del cuerpo | Contradice el formulario de Data Safety que firmarás |
| El consentimiento se auto-activa | `onClick` fuerza `hasScanConsent = true` ignorando el switch |
| `hasScanConsent = true` por defecto | Consentimiento pre-marcado — prohibido bajo GDPR Art. 7 |

Google Play verifica la coherencia entre lo que la app dice hacer con los datos y
lo que el formulario de Data Safety declara. Una discrepancia sobre **datos
biométricos/fotografías corporales** es motivo de rechazo inmediato y, si se
detecta tras la publicación, de **suspensión de la cuenta de desarrollador**.

---

### B-06 · `fallbackToDestructiveMigration()` — pérdida total de datos del usuario

```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, "jaxia_database")
    .fallbackToDestructiveMigration()   // ← borra la BD ante cualquier cambio
    .build()
```

Con `version = 2` y `exportSchema = false`, **cualquier** modificación futura del
esquema —añadir un campo a `Garment`, por ejemplo— **destruye el clóset completo,
el avatar, los outfits guardados y el historial** de todos los usuarios ya
instalados, sin aviso.

Para una app cuyo valor entero reside en los datos que el usuario introduce
manualmente prenda a prenda, esto es catastrófico. Y `exportSchema = false`
impide siquiera escribir migraciones correctas, porque no queda registro del
esquema anterior.

---

### B-07 · `allowBackup=true` con reglas vacías — medidas corporales a la nube

```xml
<application android:allowBackup="true"
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:fullBackupContent="@xml/backup_rules">
```

Ambos ficheros XML son **las plantillas por defecto, completamente comentadas**:
no incluyen ni excluyen nada. Con `allowBackup="true"` y sin reglas, Android
respalda automáticamente toda la base de datos a Google Drive.

Eso significa que **altura, tipo de cuerpo, tallas, tono de piel y preferencias**
salen del dispositivo hacia la nube — contradiciendo directamente el texto
*"Se utilizan exclusivamente en tu dispositivo"* de B-05, y obligando a
declararlo en Data Safety como transferencia a terceros.

---

## 3. Severidad alta

### A-01 · Nueve dependencias declaradas y jamás utilizadas

| Dependencia | Uso en código |
|---|---|
| `firebase-ai` | **0 referencias** |
| `firebase-appcheck-recaptcha` | **0 referencias** |
| `firebase-appcheck-debug` | **0 referencias** |
| `retrofit` | **0 referencias** |
| `converter-moshi` | **0 referencias** |
| `okhttp` | **0 referencias** |
| `logging-interceptor` | **0 referencias** |
| `moshi-kotlin` (+ KSP codegen) | **0 referencias** |
| `coil-compose` | **0 referencias** |

Además se aplica el plugin `com.google.gms.google-services` **sin que exista
`google-services.json`**, neutralizado con `missingGoogleServicesStrategy = WARN`
y `googleServices.missing.passthrough=true`.

Impacto: tamaño de descarga inflado, superficie de ataque ampliada, tiempo de
build desperdiciado, y —lo más costoso— **obligación de declarar los SDK de
Firebase en el formulario de Data Safety y en la sección "SDKs" de Play Console**
por librerías que no ejecutan una sola instrucción.

`logging-interceptor` en particular, de haberse llegado a usar, registra cuerpos
HTTP completos en logcat.

---

### A-02 · Permiso `INTERNET` sin un solo uso de red

Es el único permiso declarado. No hay ninguna llamada de red en la aplicación.
Debe eliminarse: cada permiso innecesario es una pregunta más que responder en la
ficha de Play y una objeción más en revisión.

---

### A-03 · Fuga de scope acumulativa en cada rotación de pantalla

Dos defectos que se componen:

```kotlin
// MainActivity.onCreate() — se ejecuta en CADA cambio de configuración
val repository = LookIARepository(...)   // ← nueva instancia cada vez
```

```kotlin
// LookIARepository.init
init {
    CoroutineScope(Dispatchers.IO).launch {   // ← scope sin ciclo de vida
        seedInitialDataIfNeeded()
    }
}
```

`CoroutineScope(Dispatchers.IO)` crea un scope que **nunca se cancela** (no está
atado a ningún ciclo de vida). Cada rotación de pantalla construye un
`LookIARepository` nuevo y, con él, un scope huérfano más.

El `ViewModel` sobrevive al cambio de configuración y conserva el repositorio
*original*, de modo que las instancias nuevas quedan inaccesibles pero vivas.
Diez rotaciones = diez scopes filtrados ejecutando consultas a Room.

---

### A-04 · Condición de carrera en el `combine` del ViewModel

```kotlin
combine(garments, avatar, outfits, posts, secondChance) { ... ->
    val currentWearing = _uiState.value.selectedWearingGarments.ifEmpty { ... }
    val proposals = if (_uiState.value.generatedProposals.isEmpty()) { ... }
    _uiState.value.copy(...)     // ← lee y escribe el mismo estado que muta la UI
}.collect { _uiState.value = it }
```

El transform **lee `_uiState.value`** —que los callbacks de la UI
(`selectTab`, `toggleGarmentWearing`, `requestOutfits`…) modifican
concurrentemente— y luego lo sobrescribe entero.

Secuencia que pierde datos: el usuario pulsa "Generar outfits" →
`requestOutfits` escribe `generatedProposals`; simultáneamente Room emite un
cambio → el transform, que ya había leído el valor anterior, lo sobrescribe y las
propuestas recién generadas desaparecen.

La lectura de estado mutable dentro de un operador de `Flow` rompe además la
propiedad de que el transform sea puro: el mismo input produce outputs distintos.

---

### A-05 · Estado de navegación no sobrevive a rotación ni a muerte de proceso

```kotlin
var activeTab by remember { mutableStateOf(0) }
var showAddGarmentDialog by remember { mutableStateOf(false) }
var feedbackTargetOutfit by remember { mutableStateOf<Outfit?>(null) }
```

Ninguno usa `rememberSaveable`. Al rotar el dispositivo o al volver tras que el
sistema mate el proceso en segundo plano, el usuario es devuelto a "Inicio" y
pierde el diálogo abierto. **Cero `rememberSaveable` en todo el proyecto** (67
llamadas a `remember`).

---

### A-06 · Estado muerto: `LookIAUiState.activeTab`

`LookIAUiState` declara `activeTab`, y el ViewModel expone `selectTab(tab)`. **Ni
uno ni otro se usan jamás**: `MainActivity` mantiene su propia variable local
`activeTab`. Dos fuentes de verdad para la navegación, una de ellas permanentemente
desincronizada y muerta.

---

### A-07 · Modo oscuro roto — texto claro sobre fondo blanco

`Theme.kt` define un `DarkColorScheme` completo y `LookIATheme` respeta
`isSystemInDarkTheme()`. Pero las pantallas **ignoran el tema por completo**:

| Métrica | Conteo |
|---|---|
| `Color.White` hardcodeado | **67** |
| Literales `Color(0xFF…)` | **107** |
| Usos de `MaterialTheme.colorScheme` | **3** |

Con el dispositivo en modo oscuro, `DarkColorScheme` fija `onSurface =
Color(0xFFEDE5E0)` (texto casi blanco) mientras las `Card` siguen pintándose con
`containerColor = Color.White` hardcodeado. Resultado: **texto blanco sobre
tarjeta blanca — ilegible**.

Dado que ~40 % de usuarios Android tiene el modo oscuro activo, esto afecta a una
fracción enorme de la base instalada y es causa habitual de reseñas de 1 estrella.

---

### A-08 · Ninguna lista Lazy usa `key`

19 llamadas a `items(...)`, **0 con parámetro `key`**:

```kotlin
items(filteredList) { garment -> ... }        // ClosetScreen.kt:259
items(uiState.communityPosts) { post -> ... } // CommunityScreen.kt:441
```

Sin `key`, Compose identifica los elementos por posición. Al borrar una prenda
del centro de la lista, el estado interno de las tarjetas (animaciones,
expansión, foco) se reasigna al elemento equivocado, y se recomponen todos los
elementos posteriores en lugar de solo los afectados.

---

### A-09 · Contenido de comunidad falso presentado como real

`DefaultSeedData` siembra publicaciones con autoras inventadas y métricas de
interacción prefabricadas:

```kotlin
votesA = 18, votesB = 24, likesCount = 32, commentsCount = 9
```

La pantalla Comunidad las renderiza como publicaciones auténticas de otras
usuarias. El usuario puede votar y dar "me gusta" sobre ellas, y los contadores
suben — sin que exista backend, red ni otras usuarias.

Riesgo doble: **Play lo clasifica como contenido engañoso**, y el usuario cree
estar interactuando con personas reales. Nótese además que `commentsCount = 9`
se muestra pero **no existe ninguna pantalla de comentarios** — el número no
lleva a ningún sitio.

---

### A-10 · Cifras de ahorro fabricadas presentadas como dato real

```kotlin
val totalOutfitsCreated = outfits.size.coerceAtLeast(3)
// "Cada outfit consciente evita una compra impulsiva estimada en ~$180.000 COP"
val totalSavingsCOP = (totalOutfitsCreated * 180000.0) + (activeGarments.size * 35000.0)
val impulseAvoided = (totalOutfitsCreated * 0.85).toInt().coerceAtLeast(4)
```

La UI lo presenta como **"$420000 COP"** bajo un titular de ahorro conseguido.

Tres problemas concretos:

1. **Las constantes son inventadas.** No hay fuente, estudio ni metodología
   detrás de "180.000 COP por outfit" ni de "35.000 por prenda activa".
2. **`coerceAtLeast(3)` garantiza un ahorro mínimo ficticio.** Un usuario con
   cero outfits ve *"has ahorrado $540.000 COP"*.
3. **El estado inicial muestra $420.000 antes de cargar nada** —
   `LookIAUiState` lo trae hardcodeado como valor por defecto.

Presentar cifras monetarias inventadas como logro financiero del usuario es un
problema de **claims al consumidor**, no solo de exactitud. En Colombia cae bajo
el Estatuto del Consumidor (Ley 1480 de 2011) en materia de información engañosa.

---

### A-13 · Los looks generados nunca se guardaban: "favorito" y feedback no hacían nada

*Hallazgo detectado durante la corrección, no en la pasada inicial.*

`generateOutfitsForOccasion` construye objetos `Outfit` sin insertarlos en la
base de datos, por lo que **los tres conservan el `id = 0` por defecto**:

```kotlin
Outfit(title = "Look Recomendado…", …)   // id = 0
Outfit(title = "Alternativa Cómoda…", …) // id = 0
Outfit(title = "Alternativa Creativa…", …) // id = 0
```

La interfaz los trata como filas reales:

```kotlin
onToggleFavorite = { onToggleFavorite(outfit.id) }   // siempre 0
```

que acaba en `UPDATE outfits SET isFavorite = NOT isFavorite WHERE id = 0` — una
sentencia que **no coincide con ninguna fila**. Lo mismo con el cuestionario
"Autentica tu look": el feedback se registra contra `outfitId = 0` y
`markOutfitWorn(0)` no actualiza nada.

Resultado para el usuario: pulsa el corazón, la app no da error, y nada se
guarda. Es un fallo silencioso en dos de las funciones principales.

---

### A-11 · Faltan requisitos obligatorios de Play para datos personales

| Requisito | Estado |
|---|---|
| Política de privacidad en URL pública | ❌ No existe |
| Formulario de Data Safety | ❌ Sin preparar |
| Ruta de eliminación de cuenta/datos | ❌ No existe |
| Declaración de público objetivo | ❌ Sin definir |

La app recoge altura, tallas, tipo de cuerpo, tono de piel y ubicación textual
("Bogotá, Colombia"). Todo ello es **dato personal** y obliga a los cuatro puntos
anteriores.

---

### A-12 · `compileOptions` en Java 11 con AGP 9

```kotlin
sourceCompatibility = JavaVersion.VERSION_11
targetCompatibility = JavaVersion.VERSION_11
```

AGP 9.x requiere JDK 17 como mínimo para ejecutar y compilar. Mantener el nivel
de bytecode en 11 es inconsistente con el toolchain y con las librerías AndroidX
actuales, que ya publican bytecode 17.

---

## 4. Severidad media

| ID | Hallazgo | Detalle |
|---|---|---|
| **M-01** | Cero internacionalización | **0 usos de `stringResource`**. Los ~8.000 strings en español están embebidos en el código. `strings.xml` contiene únicamente `app_name`. Sin `localeConfig`. |
| **M-02** | Accesibilidad — en su mayoría correcta | 47 de 71 `contentDescription` son `null`, pero la verificación posterior mostró que **43 son iconos decorativos acompañados de texto**, donde `null` es la práctica correcta: TalkBack debe ignorarlos y leer el texto. Todos los `IconButton` sí llevan etiqueta ("Favorito", "Eliminar", "Cerrar"). **El único hueco real** era el selector de color de `AddGarmentDialog`: un círculo pulsable sin texto alguno, que TalkBack no podía anunciar. Corregido con `semantics { contentDescription = …; role = Role.RadioButton; selected = … }`. |
| **M-03** | Formato de moneda incorrecto | `"$${value.toInt()} COP"` produce `$420000 COP`. Sin separador de miles ni `NumberFormat` por locale. Lo correcto en Colombia es `$420.000`. |
| **M-04** | Sin reglas ProGuard para Room/Moshi | `proguard-rules.pro` está vacío (solo comentarios de plantilla). Al activar R8 (B-04), Room y Moshi romperán en runtime por reflexión. |
| **M-05** | `exportSchema = false` | Impide versionar el esquema y escribir migraciones verificables. |
| **M-13** | Los tests de Robolectric no podían ejecutarse | `AppResourcesTest` y `GreetingScreenshotTest` (ambos de la plantilla) declaran `@Config(sdk = [36])`, y Robolectric exige **Java 21** para crear su sandbox de Android SDK 36. El proyecto venía con `compileOptions` en Java 11 y sin CI, así que esas dos pruebas fallaban con `UnsupportedOperationException` en cualquier entorno con JDK 17 o inferior. Confirmado en CI: *"Failed to create a Robolectric sandbox: Android SDK 36 requires Java 21 (have Java 17)"*. Corregido subiendo el JDK del CI a 21; el bytecode sigue en nivel 17. |
| **M-06** | Cobertura de tests ~0 % | 3 tests unitarios sobre un `object` de textos, 1 screenshot de un `Text("LookIA …")` suelto, 1 instrumentado que compara `packageName`. **Cero** cobertura de `LookIARepository` (toda la lógica de negocio) y de `LookIAViewModel`. |
| **M-07** | Sin CI | No hay `.github/workflows`. Nada verifica que el proyecto compile antes de publicar. |
| **M-08** | Recursos de plantilla sin personalizar | `themes.xml` declara `Theme.MyApplication`; `colors.xml` conserva la paleta morada por defecto (`purple_200`, `teal_700`…) que no se usa y no corresponde a la identidad terracota. |
| **M-09** | Iconos de launcher genéricos | `ic_launcher_foreground.xml` es el robot verde de Android Studio. Existen `ic_jaxia_logo.jpg` e `img_jaxia_hero.jpg` sin integrar como icono. Play exige icono propio de 512×512. |
| **M-10** | `README.md` es el de AI Studio | Incluye banner de Google, enlace a `ai.studio`, e instrucciones que contradicen la configuración real del proyecto. |
| **M-11** | Parámetros ignorados | `generateOutfitsForOccasion(occasion, mood, …)` recibe ambos parámetros y **no los usa** para nada salvo copiarlos al objeto resultante. Las tres propuestas son idénticas para "Reunión de trabajo / Quiero sentirme segura" y para cualquier otra combinación. |
| **M-12** | Datos semilla con identidad ficticia | El avatar por defecto se llama **"Camila"**, con altura 166 cm, talla M y tono de piel concreto. Un usuario nuevo encuentra el perfil de otra persona ya rellenado en lugar de un onboarding. |

---

## 5. Lo que está bien hecho

Conviene registrarlo, porque condiciona la estrategia de corrección — **la
arquitectura no necesita reescribirse**:

- **Separación de capas correcta y consistente.** `model` → `dao` → `repository`
  → `viewmodel` → `ui`, sin filtraciones de Room hacia Compose.
- **Uso idiomático de Flow.** Los DAO devuelven `Flow<List<T>>`; la UI consume
  `StateFlow` vía `collectAsState()`. Reactividad correcta de extremo a extremo.
- **Cero consultas en el hilo principal.** Todos los accesos a Room son `suspend`
  o `Flow`. No hay `allowMainThreadQueries()`.
- **Operaciones atómicas en SQL.** `incrementWear`, `toggleLike` y
  `toggleInterest` se resuelven en una sola sentencia `UPDATE` en lugar de
  leer-modificar-escribir. Correcto y libre de carreras.
- **Guardas contra división por cero** presentes donde importan
  (`coerceAtLeast(1)` en el cálculo de porcentajes de voto, `wearCount > 0` en
  `costPerWear`).
- **`testTag` ya colocados** en los elementos interactivos principales — la base
  para tests de UI está puesta.
- **`dependenciesInfo`** correctamente configurado (`includeInBundle = true`) para
  el análisis de Play Console.
- **Paleta y tipografía coherentes**, con una identidad visual definida y
  agradable.
- **`targetSdk = 36`** — cumple ya el requisito vigente de Play (ver §6).

---

## 6. Cumplimiento de Google Play — situación temporal

Verificado contra la documentación oficial el 18/09/2026:

> Desde el **31 de agosto de 2026**, las apps nuevas y las actualizaciones deben
> apuntar a **Android 16 (API 36)** o superior.

El proyecto ya declara `targetSdk = 36` y `compileSdk = 36`, de modo que **este
requisito está cumplido**. Es el punto más fuerte del estado actual de cara a la
tienda.

Checklist de publicación:

| Requisito | Estado |
|---|---|
| `targetSdk` ≥ 36 | ✅ Cumple |
| Formato AAB | ✅ Configurado |
| Firma de release funcional | ❌ B-03 |
| R8 / minify activo | ❌ B-04 |
| `applicationId` definitivo | ⚠️ Revisar antes de publicar (B-02, irreversible) |
| Política de privacidad (URL) | ❌ A-11 |
| Formulario de Data Safety | ❌ A-11 |
| Eliminación de datos/cuenta | ❌ A-11 |
| Icono 512×512 propio | ❌ M-09 |
| Capturas de pantalla (mín. 2) | ❌ Pendiente |
| Coherencia función ↔ descripción | ❌ B-05, A-09, A-10 |

---

## 7. Decisión de producto que debes tomar: la "IA"

Esta auditoría no puede resolver este punto por ti, porque es una decisión
comercial, no técnica. Tres opciones, comparadas en lo que importa.

> **Restricción transversal:** cualquiera que elijas se rige por
> [`ARQUITECTURA-IA.md`](ARQUITECTURA-IA.md) — la IA recomienda, compara y
> redacta; **nunca** calcula ni inventa un dato.

### Comparación

| | **Opción 1 · IA real ya** | **Opción 2 · Sin IA** | **Opción 3 · Híbrido** |
|---|---|---|---|
| **Tiempo a publicar** | 2–4 meses | Ya (está listo) | Ya, IA después |
| **Coste de desarrollo** | Alto: backend, autenticación, gestión de cuota, reintentos, caché | Ninguno adicional | Ninguno ahora |
| **Coste recurrente** | Por token. Un modelo pequeño ronda décimas de centavo de dólar por recomendación; con 10.000 usuarias activas y 5 consultas/mes son cientos de dólares mensuales, y **escala con el uso, no con los ingresos** | $0 | $0 hasta activarlo |
| **Superficie de ataque** | Clave de API que proteger, tráfico de red, datos del clóset saliendo del dispositivo, prompt injection | **Ninguna**: sin permiso `INTERNET` | Ninguna ahora |
| **Privacidad** | El clóset y las medidas viajan a un tercero. Obliga a rehacer Data Safety y la política | Todo local. La política actual es cierta y fácil de defender | Local ahora |
| **Cumplimiento Play** | Declarar función de IA generativa + mecanismo de reporte de contenido | Nada extra | Nada extra |
| **Funciona sin conexión** | No | Sí | Sí |
| **Calidad percibida** | Alta si funciona; frustrante cuando falla, va lento o agota cuota | Predecible, instantánea, algo rígida | Predecible ahora |
| **Riesgo principal** | Gastar meses y dinero antes de saber si alguien quiere el producto | Que la competencia sí ofrezca algo más flexible | Ninguno relevante |

### Sobre la seguridad, en concreto

Hoy JAXIA **no declara el permiso `INTERNET`**. Eso no es un detalle menor: el
sistema operativo le impide físicamente abrir una conexión. Esa única línea
ausente elimina de golpe filtración de datos, intercepción de tráfico, robo de
clave de API y prompt injection.

Añadir IA en la nube significa renunciar a esa garantía, y además:

- **La clave de API no puede vivir en la app.** Cualquier APK es descompilable;
  una clave embebida se extrae en minutos y te la gastan. Obliga a un backend
  propio que la custodie — que es el verdadero coste oculto de la Opción 1.
- **Cambia lo que debes declarar.** La política de privacidad actual afirma que
  nada sale del dispositivo. Con IA en la nube eso deja de ser cierto, y una
  discrepancia con el formulario de Data Safety es justo el tipo de problema que
  costó el hallazgo B-05.

Una IA **en dispositivo** (Gemini Nano, ML Kit) evita casi todo esto, pero exige
hardware reciente, pesa decenas de MB y su calidad es sensiblemente menor.

### Recomendación

**Opción 3.** Publica ahora con el motor de reglas etiquetado con honestidad, y
añade IA cuando tengas usuarias reales que te digan qué necesitan. Razones:

1. **El producto ya funciona.** El motor de reglas corregido usa ocasión y ánimo,
   rescata prendas olvidadas y calcula el valor real del clóset. Con 25 pruebas
   en verde.
2. **El nombre no te obliga.** "JAXIA" y *"Tu estilo. Tu clóset. Tu
   inteligencia."* leen como marca y como la inteligencia **de la usuaria**. No
   es una afirmación técnica y no crea riesgo de misrepresentation.
3. **Invertir en IA antes de tener tracción es apostar a ciegas.** Cuando sepas
   qué preguntan las usuarias, sabrás qué debe hacer el modelo — y probablemente
   descubras que la mitad se resuelve con más reglas.

Lo que **no** es sostenible es el estado original: declarar capacidad Gemini en
los metadatos y no tener IA. Eso ya está corregido — `metadata.json` ya no
declara `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API`.

---

## 8. Plan de corrección propuesto

### Fase 1 — Desbloquear el build *(aplicada en este repositorio)*
1. Añadir Gradle Wrapper completo (B-01)
2. Renombrar `com.example` → paquete propio (B-02)
3. Reparar la configuración de firma para que el release compile (B-03)
4. Activar R8 + `shrinkResources` con reglas Room/Moshi (B-04, M-04)
5. Subir `compileOptions` a Java 17 (A-12)

### Fase 2 — Riesgo legal y de política *(aplicada)*
6. Eliminar las afirmaciones falsas de privacidad y el escaneo simulado (B-05)
7. Recalcular el ahorro con una metodología honesta y declarada (A-10)
8. Marcar el contenido de comunidad semilla como ejemplo (A-09)
9. Desactivar el backup de datos corporales (B-07)
10. Añadir política de privacidad y ruta de borrado de datos (A-11)

### Fase 3 — Corrección técnica *(aplicada)*
11. Migración Room real, sin `fallbackToDestructiveMigration` (B-06, M-05)
12. Repositorio como singleton en `Application`, sin scope filtrado (A-03)
13. Eliminar la carrera del `combine` (A-04)
14. `rememberSaveable` para el estado de navegación (A-05, A-06)
15. `key` en todas las listas Lazy (A-08)
16. Purgar las 9 dependencias sin usar y el permiso `INTERNET` (A-01, A-02)
17. Forzar tema claro para eliminar la ilegibilidad en modo oscuro (A-07)
18. Formato de moneda con locale `es-CO` (M-03)
19. Persistir los looks generados para que favoritos y feedback funcionen (A-13)
20. Tests reales del motor de looks, del cálculo de ahorro y del formato de moneda (M-06)
21. Etiqueta de accesibilidad en el selector de color (M-02)
22. CI que compile, pruebe y firme el AAB (M-07)
23. Renombrado completo a JAXIA e icono adaptativo vectorial (§8-bis)

### Fase 4 — Pendiente por tu parte *(requiere decisiones o activos)*
- Definir `applicationId` **definitivo** antes de la primera subida (B-02)
- Decidir la estrategia de IA (§7)
- Icono 512×512 y capturas de pantalla (M-09)
- Publicar la política de privacidad en una URL accesible
- Rellenar el formulario de Data Safety
- Extracción completa de strings a `strings.xml` (M-01) — ~8.000 líneas,
  recomendable solo si se planea soportar más de un idioma
- Refactor completo a `MaterialTheme.colorScheme` para dar soporte real a modo
  oscuro (A-07) — 174 sustituciones

---

## 8-bis. Cambio de marca a JAXIA

Durante el trabajo se confirmó el nombre definitivo, **JAXIA**, y se aportó el
logotipo. Cambios aplicados:

| Elemento | Antes | Ahora |
|---|---|---|
| Nombre visible | LookIA | **JAXIA** |
| `namespace` | `com.example` | `com.jaxia.app` |
| `applicationId` | `com.aistudio.lookia.vstclr` | **`com.jaxia.app`** |
| Paquete fuente | `com.example.*` | `com.jaxia.app.*` |
| Clases | `LookIATheme`, `LookIAViewModel`, `LookIARepository`… | `JaxiaTheme`, `JaxiaViewModel`, `JaxiaRepository`… |
| Base de datos | `lookia_database` | `jaxia_database` |
| Tema XML | `Theme.MyApplication` | `Theme.Jaxia` |

### Icono

El icono anterior incrustaba un **JPEG de 1024×1024** como capa frontal del
icono adaptativo. Eso es incorrecto por dos motivos: un JPEG **no tiene canal
alfa**, así que la máscara del launcher recortaba un cuadrado crema en lugar de
la silueta del logo; y la capa `monochrome` (iconos temáticos de Android 13+)
apuntaba a ese mismo `layer-list`, lo cual no es válido.

Se sustituyó por un **icono adaptativo vectorial** con el monograma "J", el
destello y el terminal circular del logotipo, en degradado carbón → rosa sobre
fondo crema, con capa monocroma propia. Al ser vectorial es nítido en cualquier
densidad y pesa ~3 KB frente a los 563 KB del JPEG, que se eliminó por quedar sin
referencias.

También se subió `minSdk` de 24 a **26**: por debajo de API 26 el launcher no usa
iconos adaptativos y recurría a los `mipmap-*dpi/ic_launcher.webp`, que seguían
siendo **el robot verde de Android Studio**. API 24-25 está por debajo del ~1 % de
dispositivos activos en 2026, así que elevar el mínimo elimina el icono obsoleto
en vez de publicarlo. Si necesitas conservar API 24-25, hay que generar los PNG
del icono en las cinco densidades y revertir `minSdk`.

### Paleta

La paleta de marca del logotipo (crema `#F7F4F1`, carbón `#2B2523`, rosa
empolvado `#B07C8A`) se registró en `res/values/brand_colors.xml` y se usa en el
icono y en el tema base de la plataforma.

**La paleta de la interfaz sigue siendo la terracota original**, porque
alinearla al rosa del logotipo implica reescribir los 174 colores hardcodeados
descritos en A-07. Terracota y rosa empolvado son tonos cálidos vecinos y
conviven sin chocar, pero si quieres coherencia total con el logotipo, es el
mismo refactor que habilita el modo oscuro: conviene hacer ambos a la vez.

### Pendiente de tu parte

El logotipo llegó como imagen en el chat, no como archivo, así que **no pude
incrustar ese mapa de bits**. Para la ficha de Play necesitas subir aparte:

- **Icono 512×512 PNG** (32 bits, con alfa) — se sube en Play Console, no va en el AAB
- **Gráfico destacado 1024×500 PNG/JPEG**
- Si quieres el logotipo completo dentro de la app (no solo el monograma),
  añádelo al repositorio como `app/src/main/res/drawable-nodpi/img_jaxia_logo.png`
  **en PNG con transparencia**, no en JPEG

Además, `img_jaxia_hero.jpg` (1 MB) sigue siendo la imagen de portada heredada de
LookIA y se muestra en la pantalla de Inicio: conviene reemplazarla por artwork
de JAXIA y recomprimirla — 1 MB para una sola imagen decorativa es excesivo.

---

## 9. Verificación de compilación

**No pude compilar el proyecto en el entorno de auditoría.** La política de
egreso de red de la sesión bloquea `dl.google.com`, que es el único origen del
SDK de Android (plataformas y build-tools):

```
host: dl.google.com:443 → 403 (policy denial)
```

`maven.google.com`, `repo1.maven.org` y `services.gradle.org` sí eran accesibles,
pero sin `android.jar` no hay compilación posible. Siguiendo la norma del
entorno, se reportó el host bloqueado en lugar de intentar rodearlo.

**Por eso se añadió el workflow de CI, que sí ejecuta la verificación real.**

### Resultado

El pipeline está **en verde** sobre el commit `0717ebe`:

| Paso | Resultado |
|---|---|
| Assemble debug | ✅ |
| Tests unitarios (**25**) | ✅ |
| Build release bundle (AAB) | ✅ |
| Artefactos publicados | `jaxia-release-aab` (4,4 MB), `r8-mapping` (2,1 MB), `unit-test-report` |

La existencia del `mapping.txt` de 2,1 MB confirma que **R8 se ejecutó de
verdad** (B-04): sin minificación no se genera ese archivo.

### Lo que la verificación real encontró

Cinco defectos que el análisis estático no detectó. Se registran aquí porque
son la justificación de tener CI:

1. **Workflow inválido.** GitHub Actions no expone el contexto `secrets` en un
   `if:` de paso; el archivo se rechazaba antes de crear ningún job.
2. **`setup-android` fallaba** instalando el paquete `tools`, que Google retiró
   del repositorio del SDK.
3. **`compileSdk = release(36) { minorApiLevel = 1 }`** exigía la plataforma
   Android 16 QPR1, ausente en las imágenes de CI. Se simplificó a `36`.
4. **Faltaba el import de `rememberSaveable`** en `ProbadorAvatarScreen.kt` —
   un error de compilación introducido al corregir A-05.
5. **Robolectric requiere JDK 21** para Android SDK 36 (M-13).

También se corrigió un defecto propio detectado por los tests nuevos:
`formatCop` truncaba en vez de redondear, mostrando `$12.499` para 12.499,99.

### Lo que sigue sin verificarse

- **El AAB sale sin firmar** mientras no definas los secretos del repositorio
  (`KEYSTORE_BASE64`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`). El paso de
  firma se ejecuta y avisa por log de que no hay keystore.
- **No hay pruebas en dispositivo ni emulador.** Los 25 tests son unitarios de
  JVM; el comportamiento en tiempo de ejecución (navegación, migración de Room
  sobre una base existente, rendimiento de Compose) no está cubierto.
- **No hay tests de UI instrumentados** más allá del de plantilla.

---

*Auditoría generada con [Claude Code](https://claude.ai/code).*
