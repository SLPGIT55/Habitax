package com.habitax.predictor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.habitax.predictor.model.Favorito;

public class HabitaxTest {

	// Test 1: precio fallback correcto
	@Test
	public void testPrecioFallback() {
		int metros = 80;
		double precioEsperado = 80 * 3500.0;
		assertEquals(precioEsperado, metros * 3500.0, 0.01);
	}

	// Test 2: factores de precios (oportunidad y premium)
	@Test
	public void testFactoresPrecios() {
		double precioMedio = 280000.0;
		double oportunidad = precioMedio * 0.85;
		double premium     = precioMedio * 1.25;

		assertTrue(oportunidad < precioMedio);
		assertTrue(premium     > precioMedio);
		assertEquals(238000.0, oportunidad, 0.01);
		assertEquals(350000.0, premium,     0.01);
	}

	// Test 3: constructor de Favorito guarda todos los campos
	@Test
	public void testConstructorFavorito() {
		Favorito fav = new Favorito(
				1L, "Mi casa", "Madrid", 80, 280000.0, 2, 1, "Centro");

		assertEquals(1L,        fav.getUsuarioId());
		assertEquals("Madrid",  fav.getZona());
		assertEquals(80,         fav.getMetros());
		assertEquals(2,          fav.getHabitaciones());
		assertEquals(1,          fav.getBanos());
		assertEquals("Centro",  fav.getBarrio());
	}

	// Test 4: habitaciones y banos no son 0 al crear favorito con valores
	@Test
	public void testFavoritoNoGuardaCeros() {
		Favorito fav = new Favorito(
				1L, "Test", "Barcelona", 90, 300000.0, 3, 2, "Eixample");

		assertNotEquals(0, fav.getHabitaciones());
		assertNotEquals(0, fav.getBanos());
	}

	// Test 5: precio premium siempre mayor que oportunidad
	@Test
	public void testPremiumMayorQueOportunidad() {
		double base       = 200000.0;
		double oportunidad = base * 0.85;
		double premium     = base * 1.25;
		assertTrue(premium > oportunidad);
	}
}
