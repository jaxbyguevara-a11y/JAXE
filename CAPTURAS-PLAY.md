# Capturas y material gráfico para Google Play

Qué exige Play, qué falta y cómo se generarán **sin datos personales reales**.

---

## 1. Requisitos de Google Play

| Recurso | Formato | Obligatorio |
|---|---|---|
| Icono de la app | 512 × 512 px, PNG 32 bits | **Sí** |
| Gráfico destacado | 1024 × 500 px, PNG o JPEG | **Sí** |
| Capturas de teléfono | mín. 2, máx. 8 · lado corto ≥ 320 px, lado largo ≤ 3840 px, proporción máx. 2:1 | **Sí** |
| Capturas de tablet 7" | mín. 2 si declaras compatibilidad | No |
| Capturas de tablet 10" | mín. 2 si declaras compatibilidad | No |
| Vídeo promocional | URL de YouTube | No |

**Recomendación para JAXIA:** 6 capturas de teléfono a **1080 × 2400 px**
(proporción 20:9, el estándar actual). No declarar tablets en la versión 1.0.

---

## 2. Las 6 capturas propuestas

Una por pantalla principal, en el orden en que cuentan la historia del producto:

| # | Pantalla | Qué debe mostrar | Texto superpuesto sugerido |
|---|---|---|---|
| 1 | **Inicio** | Inspiración del día y la prenda olvidada | *Vístete con lo que ya tienes* |
| 2 | **Vísteme** | Las tres propuestas generadas | *Tres looks con tu propio clóset* |
| 3 | **Probador** | Avatar vestido con prendas seleccionadas | *Pruébate sin sacar nada del armario* |
| 4 | **Clóset** | Cuadrícula de prendas con costo por uso | *Descubre lo que no estás usando* |
| 5 | **Ahorro** | Valor reutilizado y utilización del clóset | *Mide lo que aprovechas* |
| 6 | **Comunidad** | Segunda Oportunidad con el aviso de ejemplo | *Da una segunda vida a tu ropa* |

---

## 3. Regla de datos: nada personal, nada falso

Las capturas se generarán con el **contenido de ejemplo** que ya trae la app
(`DefaultSeedData`), que no corresponde a ninguna persona real:

- Perfil por defecto **"Tu estilo"**, no un nombre propio
- Prendas genéricas: *Blazer Terracota*, *Blusa de Lino*…
- Publicaciones de comunidad marcadas dentro de la app como **contenido de
  ejemplo**

**No se usará:**
- Ningún nombre, correo, teléfono o dirección real
- Ninguna fotografía de una persona
- Cifras de ahorro infladas o inventadas — el valor mostrado será el que la app
  calcule con las prendas de ejemplo

> Las capturas deben reflejar lo que la usuaria verá de verdad. Una captura que
> muestre una función inexistente es motivo de rechazo por
> **Misrepresentation**, el mismo riesgo que ya corrigió la auditoría (B-05).

---

## 4. Cómo se generarán

Las capturas se producirán con **Roborazzi**, que ya está configurado y renderiza
pantallas reales de Compose en CI, igual que se hizo con la pantalla de
bienvenida (`WelcomeScreenshotTest`).

Ventajas frente a capturar a mano desde un teléfono:

- **Reproducibles**: mismo resultado en cada ejecución
- **Sin datos reales**: se controla exactamente qué estado se renderiza
- **Versionadas**: cualquier cambio de UI se ve en el diff
- **Sin barra de estado ajena**: sin hora, batería ni notificaciones personales

Alternativa manual, si prefieres capturas con el marco del sistema: instala el
APK de prueba, activa el modo demo de la barra de estado y captura desde el
teléfono.

```bash
adb shell settings put global sysui_demo_allowed 1
adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0941
adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false
adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4
# ... capturar ...
adb shell am broadcast -a com.android.systemui.demo -e command exit
```

---

## 5. Estado actual

| Recurso | Estado |
|---|---|
| Icono 512 × 512 | ⏸ Vista previa aprobada pendiente · falta `JAXIA_logo_original.png` |
| Gráfico destacado 1024 × 500 | ⏸ Igual |
| Captura 1 · Inicio | ❌ Pendiente |
| Captura 2 · Vísteme | ❌ Pendiente |
| Captura 3 · Probador | ❌ Pendiente |
| Captura 4 · Clóset | ❌ Pendiente |
| Captura 5 · Ahorro | ❌ Pendiente |
| Captura 6 · Comunidad | ❌ Pendiente |
| Bienvenida (no va en Play) | ✅ Renderizada en CI |

**Bloqueo declarado:** las seis capturas se generan **cuando las pantallas estén
terminadas**, como pediste. Hoy siguen pendientes decisiones visuales —el modo
oscuro real y la paleta definitiva frente al logotipo— que cambiarían las
capturas. Generarlas antes obligaría a rehacerlas.

---

## 6. Textos de la ficha (borrador)

**Título** (30 caracteres máx.)
> JAXIA — Vístete con lo que tienes

Son 33 caracteres: hay que recortarlo. Alternativa de 26:
> JAXIA: tu clóset consciente

**Descripción breve** (80 caracteres máx.)
> Organiza tu clóset, arma looks con lo que ya tienes y aprovecha cada prenda.

**Descripción completa** (4000 caracteres máx.) — esqueleto:

1. Qué resuelve: tienes ropa y sientes que no tienes qué ponerte
2. Cómo: clóset digital, tres propuestas por ocasión y ánimo, probador con avatar
3. Valor: costo por uso, prendas olvidadas, valor reutilizado
4. Comunidad: intercambio, regalo y venta consciente *(indicar que hoy es local)*
5. Privacidad: funciona sin conexión, sin cuenta, sin publicidad, sin rastreo
6. Qué **no** hace: sin IA, sin escaneo corporal, sin acceso a cámara

> El punto 6 no es habitual en una ficha, pero en este caso juega a favor: es
> justo lo que diferencia a JAXIA de sus competidores y lo que evita
> expectativas que la app no cumple.
