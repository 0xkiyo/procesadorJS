package analizadorlexico;

import analizadorSintactico.AnalizadorSintactico;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProcesadorSintacticoTest {

	private static final String BASE_PATH = "./impreso/";
	private static final String TEST_PATH = "./test-results/";

	@Test
	public void pruebaTxtTest() throws FileNotFoundException {
		parseablePrograms("prueba");
	}

	@Test
	public void prueba1TxtTest() throws FileNotFoundException {
		parseablePrograms("prueba1");
	}

	@Test
	public void prueba2TxtTest() throws FileNotFoundException {
		//TODO this should be parametrized
		parseablePrograms("prueba2");
	}

	@Test
	public void prueba3TxtTest() throws FileNotFoundException {
		parseablePrograms("prueba3");
	}

	@Test
	public void prueba4TxtTest() throws FileNotFoundException {
		parseablePrograms("prueba4");
	}

	@Test
	public void prueba5TxtTest() throws FileNotFoundException {
		parseablePrograms("prueba5");
	}

	@Test
	public void prueba6TxtTest() throws FileNotFoundException {

		String fileName = "prueba6";

		AnalizadorSintactico.main(fileName + ".txt");

		assertFalse(isEmpty("error.txt"));
		checkFiles(fileName, "error.txt");

		assertTrue(isEmpty("parse.txt"));
		assertTrue(isEmpty("tablas.txt"));
		assertTrue(isEmpty("tokens.txt"));
	}

	@Test
	public void prueba7TxtTest() throws FileNotFoundException {
		String fileName = "prueba7";

		AnalizadorSintactico.main(fileName + ".txt");

		assertFalse(isEmpty("error.txt"));
		checkFiles(fileName, "error.txt");

		assertTrue(isEmpty("parse.txt"));
		assertTrue(isEmpty("tablas.txt"));
		assertTrue(isEmpty("tokens.txt"));
	}

	@Test
	public void prueba8TxtTest() throws FileNotFoundException {
		String fileName = "prueba8";

		AnalizadorSintactico.main(fileName + ".txt");

		assertFalse(isEmpty("error.txt"));
		checkFiles(fileName, "error.txt");

		assertTrue(isEmpty("parse.txt"));
		assertTrue(isEmpty("tablas.txt"));
		assertTrue(isEmpty("tokens.txt"));
	}

	@Test
	public void prueba9TxtTest() throws FileNotFoundException {
		String fileName = "prueba9";

		AnalizadorSintactico.main(fileName + ".txt");

		assertFalse(isEmpty("error.txt"));
		checkFiles(fileName, "error.txt");

		assertTrue(isEmpty("parse.txt"));
		assertTrue(isEmpty("tablas.txt"));
		assertTrue(isEmpty("tokens.txt"));
	}

	@Test
	public void prueba10TxtTest() throws FileNotFoundException {
		String fileName = "prueba10";

		AnalizadorSintactico.main(fileName + ".txt");

		assertFalse(isEmpty("error.txt"));
		checkFiles(fileName, "error.txt");

		assertTrue(isEmpty("parse.txt"));
		assertTrue(isEmpty("tablas.txt"));
		assertTrue(isEmpty("tokens.txt"));
	}

	private void parseablePrograms(String fileName)
		throws FileNotFoundException {
		AnalizadorSintactico.main(fileName + ".txt");

		assertTrue(isEmpty("error.txt"));

		checkFiles(fileName, "parse.txt");
		checkFiles(fileName, "tablas.txt");
		checkFiles(fileName, "tokens.txt");
	}

	private void checkFiles(String fileName, String resultName)
		throws FileNotFoundException {
		assertEquals(
			read(TEST_PATH + fileName + "-" + resultName).trim(),
			read(BASE_PATH + resultName).trim());
	}

	private String read(String fileName) throws FileNotFoundException {
		return getScanner(fileName).useDelimiter("\\A").next();
	}

	private boolean isEmpty(String fileName) throws FileNotFoundException {
		return !getScanner(BASE_PATH + fileName).hasNext();
	}

	private Scanner getScanner(String fileName) throws FileNotFoundException {
		return new Scanner(new File(fileName));
	}
}
