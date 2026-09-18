# Política de Privacidad — JAXIA

**Versión:** borrador 2 · para tu revisión
**Última actualización:** 18 de septiembre de 2026
**Aplica a:** JAXIA para Android, identificador `com.jaxia.app`, versión 1.0

> ### ⚠️ Campos que debes completar antes de publicar
>
> | Campo | Valor |
> |---|---|
> | Responsable del tratamiento | *(tu nombre completo o razón social)* |
> | Correo de contacto | *(un correo que revises; será público)* |
> | País de residencia | *(Colombia, presumiblemente)* |
> | NIT o cédula | *(solo si publicas como empresa)* |
>
> Google Play exige además que este texto esté en una **URL pública abierta sin
> iniciar sesión**. Un archivo dentro del repositorio no cumple ese requisito.

---

## 1. En una frase

JAXIA funciona **completamente sin conexión**. Todo lo que escribes se guarda
**solo en tu teléfono**. No hay servidores, no hay cuenta, no hay registro, y
nada se envía a nosotros ni a terceros.

---

## 2. Qué datos recopila la aplicación

JAXIA **no recopila nada automáticamente**. Todos los datos existen porque tú los
escribes dentro de la app.

### 2.1 Prendas de tu clóset

Nombre, categoría, color, textura, estilo, precio de compra, número de usos,
fecha del último uso, notas de reparación y nivel de privacidad que le asignes.

### 2.2 Perfil de avatar

Nombre de usuaria (puedes poner cualquier cosa), altura en centímetros, tipo de
cuerpo, talla superior, talla inferior, talla de calzado, tono de piel, color y
estilo de cabello, preferencia de estilo y preferencia de comodidad.

> **Estos son datos personales**, y algunos son sensibles por describir tu
> cuerpo. Por eso nunca salen del dispositivo y puedes borrarlos en dos toques.

### 2.3 Looks y valoraciones

Combinaciones de prendas guardadas, cuáles marcaste como favoritas, cuántas veces
usaste cada una, y tus respuestas al cuestionario "Autentica tu look" (si te
sentiste cómoda, auténtica, segura, si lo repetirías, si encajó con la ocasión) y
las notas libres que escribas.

### 2.4 Publicaciones

Las consultas que publiques en Comunidad y los artículos que publiques en
"Segunda Oportunidad": título, categoría, talla, estado, color, precio o
intercambio propuesto, tu relato, y **el texto de ubicación que escribas a mano**.

---

## 3. Qué NO recopila

Verificable en el código fuente, que es público:

| | |
|---|---|
| **Fotografías tuyas o de tu cuerpo** | La app no accede a la cámara. No existe función de escaneo corporal |
| **Tu galería de fotos** | No se solicita ni se usa |
| **Datos biométricos** | Ninguno |
| **Tu ubicación GPS** | No se solicita. El campo de ubicación es texto que tú escribes |
| **Identificadores de publicidad** | Ninguno |
| **Tu correo, teléfono o nombre real** | No hay registro ni cuenta |
| **Analítica de uso o estadísticas** | Ningún SDK de analítica |
| **Datos de pago** | No hay compras dentro de la app |
| **Contactos, calendario, micrófono, llamadas** | No se solicitan |

**JAXIA no declara ningún permiso de Android**, ni siquiera `INTERNET`. El sistema
operativo le impide físicamente abrir una conexión de red. Esa es la garantía más
fuerte de esta política: no es una promesa, es una restricción técnica que puedes
comprobar en el archivo `AndroidManifest.xml`.

---

## 4. Para qué se usan los datos

Únicamente para que la app funcione, y todo el procesamiento ocurre dentro de tu
teléfono:

| Dato | Uso |
|---|---|
| Prendas | Mostrar tu clóset, armar combinaciones, calcular costo por uso |
| Precios y usos | Calcular el valor que reutilizas de tu clóset |
| Medidas y tipo de cuerpo | Dibujar tu avatar y ajustar las proporciones |
| Preferencias de estilo | Ordenar las propuestas de looks |
| Valoraciones | Mostrarte tu propio historial |
| Publicaciones | Mostrarlas en tu dispositivo |

**No se usan para publicidad, ni para perfilado, ni para entrenar modelos, ni
para venderlos.**

### Sobre inteligencia artificial

Pese al nombre, **JAXIA no utiliza inteligencia artificial**. Las propuestas de
looks salen de un motor de reglas que se ejecuta en tu teléfono y que combina
categorías, estilos y usos previos. No hay ningún modelo, ni local ni en la nube,
y tus datos no se envían a ningún proveedor de IA.

---

## 5. Cómo se almacenan

En una base de datos local llamada `jaxia_database`, dentro del almacenamiento
privado de la aplicación. Android aísla esa carpeta: ninguna otra app del
teléfono puede leerla.

**No hay cifrado adicional por parte de JAXIA.** Los datos quedan protegidos por
el cifrado del propio dispositivo, que en Android 10 y superiores está activo por
defecto y depende del bloqueo de pantalla que tengas configurado. Lo decimos
explícitamente para no atribuirnos una protección que no implementamos.

### Copias de seguridad desactivadas

Las copias automáticas a Google Drive y la transferencia a un teléfono nuevo
están **desactivadas** para los datos de JAXIA (`allowBackup="false"` más reglas
explícitas de exclusión). Así tus medidas corporales no salen del dispositivo ni
siquiera hacia tu propia cuenta de Google.

**Consecuencia práctica:** si desinstalas la app o cambias de teléfono, **pierdes
tus datos**. Es el precio de que nunca salgan de ahí.

---

## 6. Con quién se comparten

**Con nadie.** No hay servidores de JAXIA, no hay proveedores de análisis, no hay
redes publicitarias, no hay procesadores de pago, no hay servicios de IA.

La sección Comunidad **no está conectada a ningún servidor**: lo que publiques
queda en tu teléfono y ninguna otra persona puede verlo. Las publicaciones que ves
de otras personas son **contenido de ejemplo** incluido con la app, y así se
indica dentro de ella.

Tampoco compartimos datos por requerimiento legal, sencillamente porque **no
tenemos ninguno**.

---

## 7. Cuánto tiempo se conservan

Hasta que tú los borres o desinstales la aplicación. No hay caducidad automática
y no conservamos copia alguna.

---

## 8. Cómo eliminar tus datos

### Dentro de la app, en cualquier momento

> **Probador → pestaña "Privacidad" → "Eliminar todos mis datos"**

Se te pedirá confirmación. Al aceptar, se borran de forma permanente:

- Todas tus prendas
- Tu perfil de avatar completo, incluidas medidas y tallas
- Tus looks guardados y favoritos
- Todas tus respuestas de valoración
- Tus publicaciones de Comunidad y de Segunda Oportunidad

La app vuelve a su estado inicial con el contenido de ejemplo. **La acción no se
puede deshacer.**

### Desinstalando

Desinstalar JAXIA elimina la base de datos completa. Como las copias de seguridad
están desactivadas, no queda ningún rastro en la nube.

### No necesitas escribirnos

Como el tratamiento ocurre íntegramente en tu dispositivo y no conservamos
copia, **no podemos** acceder a tus datos, rectificarlos ni recuperarlos. El
control es exclusivamente tuyo. Si nos escribes pidiendo una eliminación, solo
podremos indicarte los pasos anteriores.

---

## 9. Tus derechos

Bajo la **Ley 1581 de 2012** de Colombia y el **RGPD** europeo, tienes derecho a
acceder, rectificar, suprimir, oponerte al tratamiento y portar tus datos.

Por el diseño de la app, los ejerces directamente:

| Derecho | Cómo |
|---|---|
| Acceso | Abre la app; todos tus datos están visibles |
| Rectificación | Edita cualquier prenda o tu perfil |
| Supresión | Botón "Eliminar todos mis datos", o desinstala |
| Oposición | No hay tratamiento al que oponerse fuera del dispositivo |
| Portabilidad | *No disponible en la versión 1.0* — ver §11 |

---

## 10. Menores de edad

JAXIA no está dirigida a menores de 13 años y no recopila datos de forma
consciente sobre ellos. Al no transmitirse nada fuera del dispositivo, no existe
recolección remota de información de menores.

---

## 11. Limitaciones conocidas de la versión 1.0

Por transparencia:

- **No hay exportación de datos.** No puedes llevarte tu clóset a otra app.
  Está previsto para una versión posterior.
- **No hay copia de seguridad.** Si pierdes el teléfono, pierdes los datos.
- **No hay bloqueo dentro de la app.** Quien desbloquee tu teléfono puede abrir
  JAXIA y ver tus medidas.

---

## 12. Cambios en esta política

Si una versión futura incorpora funciones conectadas —una comunidad real,
sincronización, copia de seguridad o recomendaciones mediante IA en la nube—,
esta política se actualizará **antes** de publicar esa versión, se solicitarán los
permisos correspondientes y se corregirá el formulario de Data Safety.

Publicaremos la fecha de la última actualización al inicio del documento.

---

## 13. Contacto

**Responsable:** *(completar)*
**Correo:** *(completar)*
**País:** *(completar)*

Si tienes dudas sobre esta política o sobre cómo funciona la app, escríbenos a la
dirección anterior.
