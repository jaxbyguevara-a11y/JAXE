# Cómo instalar el APK de prueba

APK de **depuración** para revisar el funcionamiento de JAXIA en un teléfono
real. No es la versión de publicación.

---

## 1. Descargar

1. Entra a **[Actions](https://github.com/jaxbyguevara-a11y/JAXE/actions)** en el repositorio
2. Abre la ejecución más reciente de **Android CI** de la rama
   `claude/android-app-audit-oswav2` (debe tener ✅)
3. Baja hasta **Artifacts**
4. Descarga **`jaxia-debug-apk`** — llega como `.zip`
5. Descomprímelo: dentro está `app-debug.apk`

> Los artefactos caducan a los 90 días. Cada push genera uno nuevo.

---

## 2. Instalar en el teléfono

### Opción A — directamente desde el teléfono *(la más simple)*

1. Descarga el `.zip` desde el navegador del teléfono y descomprímelo
   (los gestores de archivos de Android lo hacen; si no, *Files by Google*)
2. Toca `app-debug.apk`
3. Android avisará que la app viene de una fuente desconocida. Toca
   **Configuración** → activa **Permitir desde esta fuente** para el navegador o
   el gestor de archivos que estés usando
4. Vuelve atrás y toca **Instalar**
5. Play Protect puede mostrar *"App no segura"* o *"Enviar para analizar"*. Es lo
   normal con un APK sin firmar por Play: toca **Instalar de todas formas**

### Opción B — desde el computador con ADB

Requiere [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools).

1. En el teléfono: **Ajustes → Acerca del teléfono** → toca 7 veces **Número de
   compilación** para habilitar Opciones de desarrollador
2. **Ajustes → Sistema → Opciones de desarrollador** → activa **Depuración USB**
3. Conecta el teléfono por USB y acepta el diálogo de autorización
4. En el computador:

```bash
adb devices          # debe listar tu teléfono como "device"
adb install -r app-debug.apk
```

---

## 3. Qué revisar

La app se instala como **JAXIA** y abre en Inicio. Recorrido sugerido:

| Pestaña | Qué comprobar |
|---|---|
| **Bienvenida** | Al abrir, el logotipo aparece con fundido sobre fondo crema y pasa solo a los ~2 s |
| **Inicio** | Inspiración diaria (flechas para cambiar de día), prenda olvidada, acceso a Comunidad desde la barra superior |
| **Vísteme** | Cambia ocasión y ánimo, pulsa generar. **Las tres propuestas deben cambiar** según lo que elijas — antes salían siempre iguales |
| | Marca una propuesta como favorita: **el corazón debe quedar marcado** (antes no hacía nada) |
| | "Usar hoy" abre el cuestionario y lo guarda |
| **Probador** | Toca prendas para ponérselas. Dos tops no pueden coexistir; un vestido desplaza top y pantalón |
| | Sub-pestaña **Mi Avatar**: cambia altura, tipo de cuerpo, cabello |
| | Sub-pestaña **Privacidad**: lee el texto y prueba **Eliminar todos mis datos** |
| **Clóset** | Añade una prenda, búscala, marca usos, bórrala |
| **Ahorro** | El valor debe empezar coherente con tu clóset, **no en $420.000** |
| | Evaluador de compra: escribe algo que ya tengas y mira el consejo |
| **Comunidad** | Debe aparecer el aviso ámbar de **contenido de ejemplo**. Vota y da me gusta |

### Comprobaciones específicas de la auditoría

- **Rota el teléfono** en cualquier pestaña: debe **quedarse en la misma
  pestaña**, no volver a Inicio *(A-05)*
- **Importes** con separador de miles: `$420.000`, no `$420000` *(M-03)*
- En **Probador → Privacidad** ya **no existe** la opción de escaneo corporal ni
  las afirmaciones de cifrado *(B-05)*
- Tras **Eliminar todos mis datos**, la app vuelve al estado inicial sin cerrarse

---

## 4. Diferencias con la versión de publicación

| | APK de prueba | AAB de publicación |
|---|---|---|
| Identificador | `com.jaxia.app.debug` | `com.jaxia.app` |
| Nombre de versión | `1.0-debug` | `1.0` |
| Ofuscación (R8) | No | Sí |
| Firma | Clave de depuración automática | Tu keystore de subida |
| Tamaño | Mayor | Menor |

El sufijo `.debug` permite tenerlo instalado **junto a** una futura versión de
Play sin conflicto.

---

## 5. Si algo falla

Captura el error con ADB mientras reproduces el problema:

```bash
adb logcat --pid=$(adb shell pidof -s com.jaxia.app.debug) > jaxia-error.txt
```

Comparte `jaxia-error.txt` junto con los pasos exactos que seguiste.

### Problemas frecuentes

| Síntoma | Causa |
|---|---|
| "App no instalada" | Ya existe una versión con otra firma. Desinstala la anterior |
| "Paquete no válido" | El `.zip` no se descomprimió; instala el `.apk`, no el `.zip` |
| No aparece el diálogo de instalación | Falta permitir fuentes desconocidas para esa app concreta |
| `adb` no lista el teléfono | Falta aceptar la autorización de depuración USB en la pantalla |

---

## 6. Desinstalar

**Ajustes → Aplicaciones → JAXIA → Desinstalar**, o bien:

```bash
adb uninstall com.jaxia.app.debug
```

Al desinstalar se borran todos los datos locales, porque la app no hace copias de
seguridad en la nube.
