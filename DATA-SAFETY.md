# Declaración de Data Safety — Google Play

**Borrador para tu revisión. NO enviado a Google Play.**

Cómo responder el formulario **Play Console → Contenido de la aplicación →
Seguridad de los datos**, para JAXIA `com.jaxia.app` versión 1.0.

Cada respuesta está justificada con el archivo del código que la respalda, para
que puedas defenderla si Google pide aclaraciones.

---

## Resumen: la respuesta corta

> **JAXIA no recopila ni comparte ningún dato de usuario.**

Suena demasiado simple para ser cierto, pero lo es, y por una razón verificable:
**la app no declara el permiso `INTERNET`**. Sin ese permiso, Android impide
cualquier conexión de red. Un dato que no puede salir del dispositivo **no se
considera recopilado** según la definición de Google.

> **Definición de Google:** *"Recopilación"* significa transmitir datos fuera del
> dispositivo. Los datos que permanecen únicamente en el dispositivo y que el
> usuario puede borrar **no se declaran como recopilados**.

---

## Sección 1 · Recopilación y seguridad de los datos

| Pregunta del formulario | Respuesta | Justificación |
|---|---|---|
| ¿Tu app recopila o comparte alguno de los tipos de datos requeridos? | **No** | Sin permiso `INTERNET`; `AndroidManifest.xml` no declara ninguno |
| ¿Todos los datos están cifrados en tránsito? | *No aplica* | No hay tránsito |
| ¿Ofreces una forma de solicitar la eliminación de datos? | **Sí** | Probador → Privacidad → "Eliminar todos mis datos" |

> Al responder **No** a la primera pregunta, Play oculta el resto del
> cuestionario. Las secciones siguientes documentan **por qué** cada categoría se
> responde así, por si Google solicita revisión.

---

## Sección 2 · Categoría por categoría

### Cuenta / Información personal
**No se recopila.** JAXIA no tiene registro, ni inicio de sesión, ni correo, ni
teléfono. El campo "nombre de usuaria" es texto libre que solo se muestra en el
dispositivo; su valor por defecto es *"Tu estilo"*.

### Fotografías de prendas
**No se recopila.** La app **no accede a la cámara ni a la galería**. La entidad
`Garment` tiene un campo `imageUri`, pero **ninguna pantalla lo escribe ni lo
lee**: es un vestigio de la plantilla original y siempre queda vacío.

> *Recomendación:* elimina ese campo antes de publicar, para que el código no
> sugiera una capacidad que no existe.

### Medidas o imágenes corporales
**No se recopila.** Sí se **almacenan localmente** altura, tipo de cuerpo, tallas
y tono de piel, que la usuaria escribe a mano. Nunca salen del dispositivo, y las
copias de seguridad están desactivadas para esa base de datos
(`data_extraction_rules.xml`).

**No existen imágenes corporales.** La versión original simulaba un escaneo
corporal con textos que afirmaban capturar tres fotografías; esa pantalla fue
**eliminada** porque no tenía implementación detrás y contradecía esta
declaración.

### Avatar
**No se recopila.** El avatar se dibuja con Compose Canvas a partir de las
medidas que la usuaria escribe. No es una fotografía ni un modelo 3D, y no se
transmite.

### Ubicación
**No se recopila.** No se solicita permiso de ubicación y no se usa ninguna API
de localización. En "Segunda Oportunidad" existe un campo de ubicación, pero es
**texto libre que la usuaria escribe** (por ejemplo "Bogotá"), no una coordenada,
y permanece en el dispositivo.

### Analítica y rendimiento
**No se recopila.** No hay Firebase Analytics, ni Crashlytics, ni ningún SDK de
medición. Las dependencias del proyecto son exclusivamente AndroidX y Kotlin.

### Identificadores del dispositivo o de publicidad
**No se recopila.** No se lee el Advertising ID, ni `ANDROID_ID`, ni IMEI, ni
ningún identificador. No hay publicidad.

### Servicios de inteligencia artificial
**No se utiliza ninguno.** Pese al nombre comercial, las propuestas de looks las
genera un **motor de reglas determinista** que se ejecuta en el dispositivo.

La versión original declaraba la capacidad `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API`
en `metadata.json` e incluía la dependencia `firebase-ai`, pero **ninguna línea de
código la usaba**. Ambas fueron eliminadas para que los metadatos coincidan con la
realidad.

### Mensajes, contactos, archivos, audio, calendario, salud, finanzas
**No se recopila ninguno.** No se solicitan esos permisos.

---

## Sección 3 · Otras declaraciones de la ficha

| Apartado | Respuesta |
|---|---|
| **Anuncios** | La app no contiene anuncios |
| **Compras dentro de la app** | No hay |
| **Contenido generado por usuarios** | ⚠️ Ver la nota siguiente |
| **Público objetivo** | Mayores de 13 años · no dirigida a menores |
| **Clasificación de contenido** | Responder el cuestionario de IARC: sin violencia, sin contenido sexual, sin lenguaje soez, sin sustancias, sin juegos de azar |
| **Política de privacidad** | La URL pública donde publiques `PRIVACIDAD.md` |
| **Anuncio de app financiada por gobierno** | No |

### ⚠️ Sobre "contenido generado por usuarios"

Esta es la única casilla con matiz. JAXIA **tiene** pantallas donde la usuaria
escribe y publica contenido (Comunidad, Segunda Oportunidad), **pero no hay
servidor**: nada se transmite y ninguna otra persona puede ver lo publicado.

Google exige moderación y mecanismo de denuncia solo cuando el contenido es
**visible para otras personas**. Aquí no lo es.

**Recomendación:** responde que **no hay contenido generado por usuarios visible
para terceros**, y explica en el campo de comentarios que las publicaciones
permanecen en el dispositivo. Si en el futuro conectas un servidor real, **este
apartado cambia** y necesitarás moderación, denuncia y bloqueo de usuarios.

---

## Sección 4 · Guion de respuestas, en orden

Para rellenar el formulario sin dudar:

1. **¿Tu app recopila o comparte alguno de los tipos de datos requeridos?** → **No**
2. Play mostrará un aviso pidiendo confirmar. → **Confirmar**
3. **¿Proporcionas una forma de que los usuarios soliciten la eliminación de sus datos?** → **Sí**
   - Tipo: **Eliminación dentro de la aplicación**
   - Descripción sugerida:
     > Todos los datos se guardan únicamente en el dispositivo. La usuaria puede
     > borrarlos por completo desde Probador → Privacidad → "Eliminar todos mis
     > datos". Desinstalar la aplicación también los elimina.
4. Guardar y enviar a revisión con la ficha.

---

## Sección 5 · Coherencia, que es lo que Google revisa

Play compara tres cosas y rechaza si no coinciden:

| Fuente | Debe decir |
|---|---|
| Formulario de Data Safety | No se recopilan datos |
| Política de privacidad (`PRIVACIDAD.md`) | No se recopilan datos |
| Comportamiento real del APK | Sin permiso `INTERNET`, sin tráfico |

Las tres coinciden hoy. **Un solo cambio puede romper esa coherencia:** si
añades el permiso `INTERNET`, aunque sea para una función menor, debes revisar
las tres.

---

## Sección 6 · Qué invalidaría esta declaración

Antes de publicar cualquier versión futura, comprueba si hiciste alguna de estas
cosas. Cualquiera de ellas obliga a rehacer el formulario:

- [ ] Añadir el permiso `INTERNET`
- [ ] Añadir cámara o acceso a la galería
- [ ] Conectar la Comunidad a un servidor real
- [ ] Añadir Firebase, analítica o reporte de fallos
- [ ] Añadir IA en la nube
- [ ] Añadir publicidad o compras dentro de la app
- [ ] Reactivar las copias de seguridad de Android
- [ ] Añadir cuentas o inicio de sesión

---

## Anexo · Evidencia en el código

| Afirmación | Dónde verificarla |
|---|---|
| Sin permisos | `app/src/main/AndroidManifest.xml` — cero `<uses-permission>` |
| Sin red | Sin Retrofit, OkHttp ni `HttpURLConnection` en las 28 fuentes |
| Sin cámara | Sin CameraX, sin permiso `CAMERA`, sin `MediaStore` |
| Sin analítica | `app/build.gradle.kts` — solo AndroidX y Kotlin |
| Sin IA | `metadata.json` con `majorCapabilities: []` |
| Borrado disponible | `JaxiaRepository.deleteAllUserData()` |
| Backup desactivado | `allowBackup="false"` + `data_extraction_rules.xml` |
