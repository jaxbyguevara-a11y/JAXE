# Política de Privacidad — LookIA

**Última actualización:** 18 de septiembre de 2026
**Responsable:** *(completar: nombre legal o razón social del desarrollador)*
**Contacto:** *(completar: correo electrónico de contacto)*

> **Antes de publicar:** Google Play exige que esta política esté alojada en una
> **URL pública y accesible** (por ejemplo GitHub Pages) y que esa URL se
> registre en la ficha de Play Console. Un archivo dentro del repositorio no
> cumple el requisito por sí solo. Completa además los campos marcados
> *(completar)* — sin datos de contacto reales la ficha puede ser rechazada.

---

## 1. Resumen

LookIA funciona **completamente sin conexión**. Toda la información que
introduces se guarda **únicamente en tu dispositivo**. No existen servidores de
LookIA, no se crea ninguna cuenta y no se transmite información a nosotros ni a
terceros.

Esto es verificable en el código fuente: la aplicación **no declara el permiso
`INTERNET`** en su `AndroidManifest.xml`, por lo que el sistema operativo Android
le impide realizar cualquier conexión de red.

---

## 2. Qué información se guarda

Toda en una base de datos local (`lookia_database`) dentro del almacenamiento
privado de la app:

| Categoría | Ejemplos | Origen |
|---|---|---|
| Prendas | Nombre, categoría, color, textura, precio de compra, número de usos, notas de reparación | Lo escribes tú |
| Perfil de avatar | Nombre de usuaria, altura, tipo de cuerpo, tallas, tono de piel, color y estilo de cabello, preferencias de estilo y comodidad | Lo escribes tú |
| Looks | Combinaciones guardadas, favoritos, respuestas al cuestionario de feedback | Lo generas tú |
| Publicaciones | Consultas a la comunidad y artículos de "Segunda Oportunidad" que publiques, incluida la ubicación textual que escribas | Lo escribes tú |

---

## 3. Qué NO hace LookIA

- **No accede a la cámara** ni toma fotografías de tu cuerpo.
- **No accede a tu galería** de fotos.
- **No obtiene tu ubicación GPS.** El campo de ubicación es texto libre que tú
  escribes.
- **No recoge datos biométricos.**
- **No muestra publicidad** ni incorpora SDK de publicidad.
- **No incluye analítica ni rastreadores** de terceros.
- **No requiere registro** ni dirección de correo.
- **No comparte, vende ni transfiere** información a terceros.

---

## 4. Copias de seguridad

Las copias de seguridad automáticas de Android (Google Drive) y la transferencia
a un dispositivo nuevo están **desactivadas** para los datos de LookIA. Esto se
declara explícitamente en `res/xml/data_extraction_rules.xml` y
`res/xml/backup_rules.xml`, y mediante `android:allowBackup="false"`.

**Consecuencia práctica:** si desinstalas la aplicación o cambias de teléfono,
**tus datos se pierden**, porque nunca salieron del dispositivo.

---

## 5. Contenido de ejemplo

Al instalarla por primera vez, LookIA incluye prendas, looks y publicaciones de
comunidad **de demostración**, para que la app sea utilizable desde el inicio.

Ese contenido **no proviene de personas reales** y así se indica dentro de la
aplicación. La sección Comunidad no está conectada a ningún servidor: lo que
publiques permanece únicamente en tu dispositivo y ninguna otra persona puede
verlo.

---

## 6. Tus derechos y cómo ejercerlos

Puedes **eliminar todos tus datos** en cualquier momento, sin contactarnos:

> **Probador → pestaña "Privacidad" → "Eliminar todos mis datos"**

Esa acción borra de forma permanente tu clóset, tu avatar, tus looks, tus
respuestas de feedback y tus publicaciones, y devuelve la app a su estado
inicial. **Desinstalar la aplicación** también elimina todos los datos.

Como el tratamiento ocurre íntegramente en tu dispositivo y no conservamos copia
alguna, no podemos acceder, rectificar ni recuperar tu información: el control es
exclusivamente tuyo.

Bajo la Ley 1581 de 2012 (Colombia) y el RGPD (UE), conservas los derechos de
acceso, rectificación, supresión, oposición y portabilidad. En la práctica los
ejerces directamente en la app, mediante los controles descritos arriba.

---

## 7. Menores de edad

LookIA no está dirigida a menores de 13 años y no recoge información de forma
consciente sobre ellos. Al no transmitirse dato alguno fuera del dispositivo, no
existe recolección remota de información de menores.

---

## 8. Cambios en esta política

Si una versión futura incorpora funciones conectadas (por ejemplo, una comunidad
real o recomendaciones mediante inteligencia artificial en la nube), esta
política se actualizará **antes** de publicar dicha versión, y los permisos y el
formulario de Data Safety se ajustarán en consecuencia.

---

## 9. Contacto

*(completar: correo electrónico)*
