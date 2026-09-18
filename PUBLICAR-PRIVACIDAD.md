# Cómo publicar la política de privacidad en una URL pública

Google Play exige una URL que **abra sin iniciar sesión**, sin muros de registro
y de forma permanente. Estas son las opciones, comparadas.

> **Hazlo solo después de aprobar el texto de [`PRIVACIDAD.md`](PRIVACIDAD.md) y
> de completar los campos de contacto.** Una vez publicada, la URL queda en la
> ficha de Play y cambiarla obliga a actualizarla allí.

---

## Comparación

| | **GitHub Pages** | **Cloudflare Pages** | **Netlify** |
|---|---|---|---|
| Costo | Gratis | Gratis | Gratis |
| Permanencia | Mientras el repo exista | Mientras la cuenta exista | Igual |
| Dominio propio | Sí, gratis | Sí, gratis | Sí, gratis |
| Configuración | **5 minutos** | 10 minutos | 10 minutos |
| Se actualiza solo al hacer push | Sí | Sí | Sí |
| Requiere repositorio **público** | Sí, en el plan gratuito | No | No |

**Recomendación: GitHub Pages.** El repositorio ya existe y es público, el
documento ya está versionado, y publicar es marcar una casilla.

---

## Opción A · GitHub Pages *(recomendada)*

### A.1 Preparar el archivo

GitHub Pages sirve `index.html` o `index.md` desde la raíz o desde `/docs`. Lo
más limpio es una carpeta dedicada:

```bash
mkdir -p docs
cp PRIVACIDAD.md docs/index.md
```

Añade al principio de `docs/index.md` el encabezado que Jekyll necesita:

```yaml
---
title: Política de Privacidad — JAXIA
---
```

Súbelo:

```bash
git add docs/
git commit -m "docs: publicar la política de privacidad en GitHub Pages"
git push
```

### A.2 Activar Pages

1. Ve a **Settings → Pages** en el repositorio
2. En **Source**, elige **Deploy from a branch**
3. Branch: la rama que uses (`main` cuando fusiones) · Folder: **`/docs`**
4. **Save**

En uno o dos minutos la URL queda activa:

```
https://jaxbyguevara-a11y.github.io/JAXE/
```

### A.3 Comprobar que cumple

Abre la URL en **ventana de incógnito** y sin sesión de GitHub. Debe cargar el
texto completo. Si pide iniciar sesión, el repositorio es privado y Pages no
funcionará en el plan gratuito.

```bash
curl -sS -o /dev/null -w "%{http_code}\n" https://jaxbyguevara-a11y.github.io/JAXE/
# debe responder 200
```

### A.4 Registrarla en Play Console

**Play Console → Contenido de la aplicación → Política de privacidad** → pega la
URL → **Guardar**.

---

## Opción B · Cloudflare Pages

Útil si prefieres mantener el repositorio **privado**.

1. Crea una cuenta en [dash.cloudflare.com](https://dash.cloudflare.com)
2. **Workers & Pages → Create → Pages → Connect to Git**
3. Autoriza el repositorio y elige la rama
4. Framework preset: **None** · Build output directory: `docs`
5. **Save and Deploy**

Queda en `https://jaxia.pages.dev` y se actualiza en cada push.

---

## Opción C · Dominio propio *(si compras jaxia.app o similar)*

Un dominio cuesta entre 10 y 15 USD al año y da una URL más profesional:
`https://jaxia.app/privacidad`.

Funciona con cualquiera de las opciones anteriores: en GitHub Pages se configura
en **Settings → Pages → Custom domain**, añadiendo un registro `CNAME` en tu
proveedor de dominio.

**No es necesario para publicar.** La URL de `github.io` es perfectamente válida
para Google Play.

---

## Requisitos que Play verifica

Antes de enviar la ficha, comprueba que tu URL:

- [ ] Abre **sin iniciar sesión** (pruébalo en incógnito)
- [ ] Es accesible desde cualquier país, sin bloqueo geográfico
- [ ] Menciona **JAXIA por su nombre**
- [ ] Describe los datos que la app maneja, coherente con el formulario de Data Safety
- [ ] Incluye un **medio de contacto** real
- [ ] **No** es un PDF descargable ni un documento de Google Drive
- [ ] **No** redirige a una página genérica de otra empresa
- [ ] Usa **HTTPS**

> El motivo de rechazo más frecuente es una política genérica copiada de una
> plantilla, que no corresponde al funcionamiento real de la app. La de JAXIA
> está escrita sobre lo que el código hace y es fácil de defender.

---

## Mantenimiento

Cada vez que edites `PRIVACIDAD.md`, copia el cambio a `docs/index.md` y haz
push: la URL se actualiza sola.

Para no tener que acordarte, puedes automatizarlo en el workflow de CI:

```yaml
- name: Sincronizar la política publicada
  run: |
    printf -- '---\ntitle: Política de Privacidad — JAXIA\n---\n\n' > docs/index.md
    cat PRIVACIDAD.md >> docs/index.md
```

Actualiza también la **fecha de última actualización** del encabezado cuando
cambies algo sustancial.
