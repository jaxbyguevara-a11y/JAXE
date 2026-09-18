# Principio de arquitectura: qué puede y qué no puede hacer la IA

**Estado:** aceptado · 18 de septiembre de 2026
**Aplica a:** todo cálculo presentado al usuario como dato

---

## La regla

> **Todo número que el usuario pueda tomar por un hecho se calcula con reglas
> deterministas sobre datos oficiales versionados. La IA nunca calcula, nunca
> completa un dato que falta y nunca inventa una cifra.**
>
> La IA puede **recomendar**, **comparar**, **explicar** y **redactar**, siempre
> sobre resultados que el motor determinista ya produjo.

Esta separación no es estilística. Un modelo de lenguaje es un generador
probabilístico: ante un dato ausente produce el token más plausible, no un error.
Para una tarifa, una vigencia o un descuento, "plausible" es exactamente el modo
de fallo más peligroso, porque el resultado **parece correcto**.

---

## La frontera, explícita

| Responsabilidad | Quién | Por qué |
|---|---|---|
| Aplicar una tarifa | **Motor de reglas** | Debe ser reproducible y auditable |
| Resolver el grupo etario de una persona | **Motor de reglas** | Es aritmética sobre una fecha, no una inferencia |
| Determinar si un convenio aplica | **Motor de reglas** | Depende de condiciones declaradas, no de criterio |
| Calcular un descuento | **Motor de reglas** | Composición de reglas con orden y topes definidos |
| Comprobar vigencia | **Motor de reglas** | Comparación de fechas contra el período del dato |
| Sumar, redondear, prorratear | **Motor de reglas** | Un LLM no es una calculadora fiable |
| Elegir qué alternativas mostrar | IA (opcional) | Preferencia, no hecho |
| Ordenar opciones por conveniencia | IA (opcional) | Juicio sobre resultados ya calculados |
| Explicar **por qué** una opción conviene | IA (opcional) | Redacción sobre cifras dadas |
| Redactar la propuesta comercial | IA (opcional) | Lenguaje, no aritmética |

**Prueba de una línea:** si la respuesta cambiara ante la misma entrada, o si un
auditor pudiera pedir "demuéstrame de dónde sale este número", entonces **no**
puede salir de un modelo.

---

## Cómo se hace cumplir, no solo se declara

Un principio que solo vive en un documento se incumple en el primer sprint con
prisa. Estas son las restricciones que lo vuelven verificable:

### 1. La IA nunca ve la operación aritmética

Al modelo no se le pide que calcule. Se le entrega el resultado **ya calculado**
y se le pide texto:

```kotlin
// ❌ NUNCA
"Calcula la tarifa para una persona de 34 años con convenio X"

// ✅ Así
"""
Redacta una recomendación breve a partir de estas opciones YA CALCULADAS.
No modifiques ninguna cifra. No añadas cifras que no estén aquí.

Opción A: $180.000/mes · convenio EMPRESA_X · vigente hasta 2026-12-31
Opción B: $210.000/mes · sin convenio · vigente hasta 2027-06-30
"""
```

### 2. Salida estructurada, no prosa libre con números

El modelo devuelve identificadores y texto; **jamás** cifras que luego se
muestren como dato. La UI renderiza los importes desde el objeto del motor, no
desde la respuesta del modelo.

```kotlin
data class RecomendacionIA(
    val opcionRecomendadaId: String,   // referencia a una opción calculada
    val justificacion: String,         // texto libre, sin cifras
    val ordenSugerido: List<String>,   // ids
)
```

Si el modelo devuelve un id que no existe en el conjunto calculado, **se
descarta la respuesta completa** y la UI muestra las opciones sin recomendación.

### 3. Validación de post-condición

Antes de mostrar texto generado, se comprueba que toda cifra que aparezca en él
exista literalmente entre las calculadas. Si aparece una que no está, se rechaza.
Es barato y ataca justo el modo de fallo que importa.

### 4. Los datos oficiales son versionados y trazables

Tarifas, convenios, grupos etarios, descuentos y vigencias viven en una tabla con
**origen, versión y fecha de entrada en vigor**. Todo cálculo registra contra qué
versión se hizo, de modo que un resultado del pasado puede reproducirse
exactamente aunque la tabla haya cambiado.

### 5. Degradación sin IA

Si el modelo no responde, responde tarde o responde mal, **la funcionalidad
principal sigue operando**: las cifras se muestran igual, sin el texto de
recomendación. La IA es una capa opcional encima, nunca un eslabón del cálculo.

---

## Estado actual en JAXIA

**La app hoy no tiene IA ni tiene dominio de tarifas.** Lo que sí existe ya
cumple el principio:

| Cálculo | Implementación | Cumple |
|---|---|---|
| Valor reutilizado del clóset | `JaxiaRepository.calculateSavingsMetrics` | ✅ determinista, con 8 pruebas |
| Costo por uso | `Garment.costPerWear` | ✅ aritmética pura |
| Generación de looks | `generateOutfitsForOccasion` | ✅ motor de reglas, con 7 pruebas |
| Evaluación de compra | `evaluateSmartPurchase` | ✅ reglas sobre el clóset real |

Esto no era así antes de la auditoría: el contador de ahorro multiplicaba por
constantes inventadas y garantizaba un mínimo ficticio (`AUDITORIA.md` A-10). Ese
fallo es **exactamente** el que este principio previene, y ocurrió sin que
hubiera ningún modelo de por medio: basta con inventar la fórmula.

> **Nota de alcance:** las tarifas, convenios, grupos etarios, descuentos y
> vigencias **no existen en el código actual**. Este documento fija la regla por
> anticipado para cuando se incorporen; ver la pregunta abierta en el historial
> de la sesión sobre a qué producto pertenece ese dominio.

---

## Consecuencia para la ficha de Play

Mientras la IA solo redacte y recomiende sobre cifras deterministas:

- No hay que declarar que la app **genera** contenido con IA como función
  principal.
- Si se añade generación de texto visible al usuario, Play exige declararlo y
  ofrecer un mecanismo para reportar contenido ofensivo.
- El texto generado **no** puede presentarse como asesoría financiera, médica ni
  legal sin los descargos correspondientes.
