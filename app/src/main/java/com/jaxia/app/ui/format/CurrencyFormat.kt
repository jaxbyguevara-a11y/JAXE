package com.jaxia.app.ui.format

import java.text.NumberFormat
import java.util.Locale

/** Colombian peso formatting. Amounts are whole pesos; centavos are not used. */
private val COP: NumberFormat = NumberFormat.getIntegerInstance(Locale("es", "CO"))

/**
 * Formats an amount as Colombian pesos, e.g. `420000.0` -> `"$420.000"`.
 *
 * The screens previously interpolated `"$${value.toInt()} COP"`, which rendered
 * `$420000 COP`: no thousands separator and the wrong grouping convention for
 * es-CO (AUDITORIA.md M-03).
 */
fun formatCop(amount: Double): String {
    if (amount.isNaN() || amount.isInfinite()) return "$0"
    return "$" + COP.format(amount.toLong())
}

/** Same as [formatCop] but appends the currency code, for headline figures. */
fun formatCopWithCode(amount: Double): String = "${formatCop(amount)} COP"
