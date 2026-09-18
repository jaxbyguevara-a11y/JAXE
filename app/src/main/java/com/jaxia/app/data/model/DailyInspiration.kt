package com.jaxia.app.data.model

import java.util.Calendar

data class SpiritualMessage(
    val dayNumber: Int,
    val title: String,
    val theme: String, // "Paz Interior", "Superación", "Gratitud", "Autovaloración", "Propósito", "Soltar con Amor"
    val biblicalOrWisdomQuote: String,
    val reflection: String,
    val affirmation: String,
    val practicalAct: String
)

object DailyInspirationProvider {
    private val messages = listOf(
        SpiritualMessage(
            dayNumber = 1,
            title = "Vestida de Fortaleza y Dignidad",
            theme = "Autovaloración",
            biblicalOrWisdomQuote = "“Se viste de fuerza y dignidad, y sonríe confiada ante el porvenir.” — Proverbios 31:25",
            reflection = "Tu verdadero ropaje no se confecciona en fábricas ni se exhibe en escaparates. Tu belleza auténtica nace de la paz con la que abrazas tu historia, de las cicatrices que has transformado en sabiduría y de la gracia con la que tratas a los demás. Hoy recuérdate que eres una creación divina, irrepetible y colmada de propósito.",
            affirmation = "Hoy me visto de fe, confianza y compasión. Reconozco que mi valor es sagrado e innegociable.",
            practicalAct = "Mírate al espejo con ternura. Agradece a tu cuerpo por sostenerte y sonríe a la vida."
        ),
        SpiritualMessage(
            dayNumber = 2,
            title = "Soltar para Recibir lo Nuevo",
            theme = "Soltar con Amor",
            biblicalOrWisdomQuote = "“No se aferren al pasado ni recuerden las cosas antiguas. ¡Miren! Estoy haciendo algo nuevo; ya está brotando.” — Isaías 43:18-19",
            reflection = "A veces nos aferramos a cosas materiales, expectativas y cargas emocionales que cumplieron su ciclo. Cuando limpias tu clóset y donas o regalas lo que ya no usas, estás haciendo un acto espiritual: estás diciendo al universo que confías en la provisión y abres espacio para que fluya la frescura en tu alma.",
            affirmation = "Suelto con gratitud lo que ya no me pertenece y recibo con brazos abiertos las bendiciones del presente.",
            practicalAct = "Elige una prenda que ames pero ya no uses y regálala hoy con amor a alguien que la necesite."
        ),
        SpiritualMessage(
            dayNumber = 3,
            title = "La Paz Interior no Tiene Precio",
            theme = "Paz Interior",
            biblicalOrWisdomQuote = "“La paz les dejo, mi paz les doy; no se la doy a ustedes como el mundo la da. No se angustie su corazón ni tenga miedo.” — Juan 14:27",
            reflection = "El mundo nos incita a creer que la felicidad se compra en la siguiente vitrina. Pero ninguna prenda puede vestir el vacío de un corazón inquieto. La verdadera serenidad florece cuando descansas en la certeza de que ya tienes suficiente, ya eres suficiente y caminas bajo una gracia inquebrantable.",
            affirmation = "Mi corazón reposa en la calma. No busco llenar afuera lo que Dios ya ha completado dentro de mí.",
            practicalAct = "Toma 5 respiraciones profundas antes de salir. Da gracias por el aire, la vida y tu hogar."
        ),
        SpiritualMessage(
            dayNumber = 4,
            title = "Tu Luz no Necesita Aprobación Externa",
            theme = "Superación",
            biblicalOrWisdomQuote = "“Ustedes son la luz del mundo. Una ciudad situada sobre un monte no se puede ocultar.” — Mateo 5:14",
            reflection = "A menudo intentamos encajar en modas ajenas o en juicios superficiales para sentirnos aceptadas. Pero Dios no te llamó a mimetizarte, te llamó a iluminar. Cuando te vistes con lo que tienes y caminas con seguridad, enseñas al mundo que el verdadero estilo es la autenticidad del espíritu.",
            affirmation = "Permito que mi luz interior guíe mis pasos. Mi alegría no depende de la opinión de los demás.",
            practicalAct = "Usa hoy tu prenda favorita sin esperar la validación de nadie; disfrútala para ti."
        ),
        SpiritualMessage(
            dayNumber = 5,
            title = "La Abundancia de la Gratitud",
            theme = "Gratitud",
            biblicalOrWisdomQuote = "“Den gracias en toda situación, porque esta es la voluntad de Dios para ustedes.” — 1 Tesalonicenses 5:18",
            reflection = "La queja reduce nuestra visión, pero la gratitud multiplica lo que poseemos. Cuando abres tu clóset y bendices cada hilo, abrigo y calzado que te protege, despiertas la ley espiritual de la abundancia. Lo que tienes hoy fue una respuesta a tus anhelos del pasado.",
            affirmation = "Vivo con un corazón agradecido. Todo lo que necesito para ser feliz y bendecir ya me ha sido concedido.",
            practicalAct = "Agradece conscientemente por cada prenda que te pongas esta mañana antes de salir."
        ),
        SpiritualMessage(
            dayNumber = 6,
            title = "Renovación Espiritual y Creatividad",
            theme = "Propósito",
            biblicalOrWisdomQuote = "“Sean transformados mediante la renovación de su mente, para que comprueben la buena voluntad de Dios.” — Romanos 12:2",
            reflection = "La creatividad es una chispa divina en nosotras. Crear looks nuevos combinando prendas que ya tienes es un ejercicio de ingenio, humildad y celebración de la vida. Descubrirás que los mayores tesoros están ocultos en lo que considerabas ordinario.",
            affirmation = "Mi mente se renueva hoy con creatividad, gozo y sencillez. Disfruto reinventar mi camino.",
            practicalAct = "Combina hoy dos prendas que nunca antes habías usado juntas y sonríe ante tu inventiva."
        ),
        SpiritualMessage(
            dayNumber = 7,
            title = "Amor en Acción: La Alegría de Compartir",
            theme = "Segunda Vida y Amor",
            biblicalOrWisdomQuote = "“El que tiene dos túnicas, dé al que no tiene; y el que tiene qué comer, haga lo mismo.” — Lucas 3:11",
            reflection = "Dar una segunda oportunidad a una prenda regalándola o intercambiándola es un puente de bendición humana. La ropa que descansa olvidada en un cajón puede ser el abrigo o la alegría que otra persona ha estado orando por recibir. Dar sin esperar nada a cambio es la máxima elegancia del alma.",
            affirmation = "Soy un canal de bendición y generosidad. Todo lo que doy con amor retorna multiplicado en paz.",
            practicalAct = "Publica hoy una prenda en 'Regalo con amor' o 'Trueque' para bendecir a otra persona en la comunidad."
        ),
        SpiritualMessage(
            dayNumber = 8,
            title = "Belleza que no Marchita",
            theme = "Autovaloración",
            biblicalOrWisdomQuote = "“Que su belleza no sea la externa, sino la interna: el encanto incorruptible de un espíritu sereno y tierno.” — 1 Pedro 3:3-4",
            reflection = "Las modas pasan, los tejidos envejecen y las tendencias se esfuman; pero la bondad, la dulzura y la fortaleza de espíritu permanecen para siempre. Cultiva tu mente con pensamientos nobles y tu corazón con compasión. Esa es la hermosura que jamás pasa de temporada.",
            affirmation = "Cultivo la gracia de mi corazón. Mi elegancia más sublime es la compasión y la serenidad.",
            practicalAct = "Hazle un cumplido sincero a otra mujer sobre su energía, bondad o sonrisa hoy."
        ),
        SpiritualMessage(
            dayNumber = 9,
            title = "Paciencia en tu Proceso",
            theme = "Superación",
            biblicalOrWisdomQuote = "“El amor es paciente, es bondadoso. Todo lo sufre, todo lo cree, todo lo espera, todo lo soporta.” — 1 Corintios 13:4-7",
            reflection = "Sé paciente con tu cuerpo, con tus cambios y con tus etapas. No te compares con fotos editadas ni con vidas de catálogo. Estás en un viaje sagrado de florecimiento. Amarte hoy, tal como estás en este momento, es el primer paso para una vida plena.",
            affirmation = "Honro mi ritmo y mi historia. Estoy exactamente donde necesito estar para seguir creciendo.",
            practicalAct = "Dedica 5 minutos a meditar en silencio y enviar gratitud a cada etapa de tu vida."
        ),
        SpiritualMessage(
            dayNumber = 10,
            title = "Caminar con Propósito y Esperanza",
            theme = "Propósito",
            biblicalOrWisdomQuote = "“Porque yo sé los planes que tengo para ustedes —afirma el Señor—, planes de bienestar y no de calamidad, a fin de darles un futuro y una esperanza.” — Jeremías 29:11",
            reflection = "Levántate hoy sabiendo que tus pasos tienen un rumbo sagrado. No caminas sola; la gracia te envuelve en cada decisión. Al vestir tu cuerpo con prendas que honran tu autenticidad, declaras al mundo que confías plenamente en el futuro que se dibuja para ti.",
            affirmation = "Camino con certeza y alegría. Mis días están guardados por el amor divino y la esperanza.",
            practicalAct = "Escribe una meta noble para este mes y decídete a avanzar con fe."
        )
    )

    fun getDailyMessage(dayOfYear: Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)): SpiritualMessage {
        val index = (dayOfYear - 1).coerceAtLeast(0) % messages.size
        return messages[index]
    }

    fun getAllMessages(): List<SpiritualMessage> = messages
}
