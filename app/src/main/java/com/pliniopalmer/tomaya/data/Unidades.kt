package com.pliniopalmer.tomaya.data

/**
 * Unidad de dosis que se puede elegir en el formulario. Se guarda [singular] en la base
 * de datos; [plural] se usa en la lista y cuando la cantidad es mayor que 1. Las
 * abreviaturas (ml, mg, UI…) no cambian, así que ambas formas coinciden.
 */
data class Unidad(val singular: String, val plural: String = singular)

data class GrupoUnidades(val titulo: String, val unidades: List<Unidad>)

/** Unidades habituales para indicar una dosis, agrupadas por tipo de presentación. */
val GRUPOS_UNIDADES: List<GrupoUnidades> = listOf(
    GrupoUnidades(
        "Sólidos",
        listOf(
            Unidad("pastilla", "pastillas"),
            Unidad("tableta", "tabletas"),
            Unidad("cápsula", "cápsulas"),
            Unidad("comprimido", "comprimidos"),
            Unidad("sobre", "sobres"),
        ),
    ),
    GrupoUnidades(
        "Líquidos",
        listOf(
            Unidad("ml"),
            Unidad("cc"),
            Unidad("gota", "gotas"),
            Unidad("cucharadita", "cucharaditas"),
            Unidad("cucharada", "cucharadas"),
        ),
    ),
    GrupoUnidades(
        "Peso y actividad",
        listOf(
            Unidad("mg"),
            Unidad("g"),
            Unidad("mcg"),
            Unidad("UI"),
            Unidad("unidad", "unidades"),
        ),
    ),
    GrupoUnidades(
        "Otras presentaciones",
        listOf(
            Unidad("inhalación", "inhalaciones"),
            Unidad("atomización", "atomizaciones"),
            Unidad("aplicación", "aplicaciones"),
            Unidad("parche", "parches"),
            Unidad("supositorio", "supositorios"),
            Unidad("óvulo", "óvulos"),
            Unidad("ampolla", "ampollas"),
            Unidad("inyección", "inyecciones"),
        ),
    ),
)

private val UNIDADES: List<Unidad> = GRUPOS_UNIDADES.flatMap { it.unidades }

/** Busca una unidad de la lista por su forma singular o plural, sin distinguir mayúsculas. */
fun buscarUnidad(texto: String): Unidad? {
    val limpio = texto.trim()
    return UNIDADES.firstOrNull {
        it.singular.equals(limpio, ignoreCase = true) || it.plural.equals(limpio, ignoreCase = true)
    }
}

/**
 * Lleva una unidad a la forma en que se guarda (singular). Las unidades escritas a mano
 * antes de existir la lista y que no coinciden con ninguna se dejan como están.
 */
fun normalizarUnidad(texto: String): String = buscarUnidad(texto)?.singular ?: texto.trim()

/** Nombre de la unidad tal como aparece en la lista desplegable. */
fun unidadParaLista(texto: String): String = buscarUnidad(texto)?.plural ?: texto.trim()

/** "pastilla" para 1 o menos, "pastillas" para más de 1. */
fun unidadSegunCantidad(cantidad: Double, unidad: String): String {
    val conocida = buscarUnidad(unidad) ?: return unidad.trim()
    return if (cantidad > 1.0) conocida.plural else conocida.singular
}
