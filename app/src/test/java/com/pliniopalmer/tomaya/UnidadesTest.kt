package com.pliniopalmer.tomaya

import com.pliniopalmer.tomaya.data.GRUPOS_UNIDADES
import com.pliniopalmer.tomaya.data.MAX_LARGO_UNIDAD
import com.pliniopalmer.tomaya.data.normalizarUnidad
import com.pliniopalmer.tomaya.data.unidadParaLista
import com.pliniopalmer.tomaya.data.unidadSegunCantidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnidadesTest {

    private val todas = GRUPOS_UNIDADES.flatMap { it.unidades }

    @Test
    fun `todas las unidades caben en el limite y no se repiten`() {
        assertTrue(todas.all { it.singular.length <= MAX_LARGO_UNIDAD })
        assertEquals(todas.size, todas.map { it.singular }.toSet().size)
    }

    @Test
    fun `usa singular hasta 1 y plural por encima`() {
        assertEquals("pastilla", unidadSegunCantidad(1.0, "pastilla"))
        assertEquals("pastilla", unidadSegunCantidad(0.5, "pastilla"))
        assertEquals("pastillas", unidadSegunCantidad(1.5, "pastilla"))
        assertEquals("inhalaciones", unidadSegunCantidad(2.0, "inhalación"))
    }

    @Test
    fun `las abreviaturas no cambian`() {
        assertEquals("ml", unidadSegunCantidad(5.0, "ml"))
        assertEquals("UI", unidadSegunCantidad(10.0, "UI"))
    }

    @Test
    fun `reconoce unidades escritas a mano en plural o con mayusculas`() {
        assertEquals("gota", normalizarUnidad(" Gotas "))
        assertEquals("gotas", unidadParaLista("gota"))
        assertEquals("pastilla", unidadSegunCantidad(1.0, "Pastillas"))
    }

    @Test
    fun `conserva las unidades que no estan en la lista`() {
        assertEquals("chorrito", normalizarUnidad("chorrito"))
        assertEquals("chorrito", unidadSegunCantidad(3.0, "chorrito"))
    }
}
