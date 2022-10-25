package org.danielcepeda.junit5app.ejemplos;

import org.danielcepeda.junit5app.exceptions.DineroInsuficienteException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

// @TestInstance(TestInstance.Lifecycle.PER_CLASS) //para que se ejecute una sola vez y no cada vez que se ejecuta un test como viene por defecto
class CuentaTest {

    Cuenta cuenta;

    @BeforeAll
    static void beforeAll() {
        System.out.println("Comenzando el test!");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Terminando el test!");
    }

    @BeforeEach
    void initMetodoTest(){
        this.cuenta = new Cuenta("Pablo", new BigDecimal("100023.123123"));
        System.out.println("------- Iniciando el metodo! -------");
    }

    @AfterEach
    void tearDown() {
        System.out.println("-------- Finalizando metodo! --------");
    }

    @Nested @DisplayName("Probando Atributos Cuenta")
    class DatosCuentaTest{

        @Test @DisplayName("Probando el nombre!")
        void testNombreCuenta(TestInfo testInfo, TestReporter testReporter){

            System.out.println("ejecutando: " + testInfo.getDisplayName() + " " + testInfo.getTestMethod()
            + " con las etiquetas: " + testInfo.getTags());
            testReporter.publishEntry("Etiquetas: " + testInfo.getTags().toString());
            // al final de cada prueba se agrega una expresion lamda con el mensaje personalizado
            assertEquals("Pablo", cuenta.getPersona(), () -> "El nombre de la cuenta no es el que se esperaba");
        }

        @Test @DisplayName("probando el saldo de la cuenta, que sea mayor que cero")
        void testSaldoCuenta(){
            assertEquals(100023.123123, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        }

        @Test @Disabled
        void testReferenciaCuenta() {
            fail();
            Cuenta cuenta2 = new Cuenta("Pablo", new BigDecimal("100023.123123"));
            // assertNotEquals(cuenta2, cuenta);
            assertEquals(cuenta2, cuenta);
        }
    }

    @Tag("cuenta")
    @Nested
    class OperacionesCuentaTest{
        @Test
        void testDebitoCuenta() {
            Cuenta cuenta = new Cuenta("Juan", new BigDecimal("1000.12345"));
            cuenta.debito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(900, cuenta.getSaldo().intValue());
            assertEquals("900.12345", cuenta.getSaldo().toPlainString());
        }

        @Test
        void testCreditoCuenta() {
            Cuenta cuenta = new Cuenta("Juan", new BigDecimal("1000.12345"));
            cuenta.credito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(1100, cuenta.getSaldo().intValue());
            assertEquals("1100.12345", cuenta.getSaldo().toPlainString());
        }

        @Tag("banco")
        @Test
        void testDineroInsuficienteExceptionCuenta() {
            Cuenta cuenta = new Cuenta("Juan", new BigDecimal("1000.12345"));
            Exception exception = assertThrows(DineroInsuficienteException.class, ()->{
                cuenta.debito(new BigDecimal("1500"));
            });
            String mensajeActual = exception.getMessage();
            String mensajeEsperado = "Dinero Insuficiente";
            assertEquals(mensajeEsperado, mensajeActual);
        }

        @Tag("banco")
        @Test
        void testTransferirDineroCuentas() {
            Cuenta cuenta1 = new Cuenta("Cristiano Ronaldo", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("Lionel Messi", new BigDecimal("1500.8989"));
            Banco banco = new Banco();
            banco.setNombre("Banco Falso");
            banco.transferir(cuenta2, cuenta1, new BigDecimal("500"));
            assertEquals("1000.8989", cuenta2.getSaldo().toPlainString());
            assertEquals("3000", cuenta1.getSaldo().toPlainString());
        }

        @Test
        void testRelacionBancoCuentas() {
            Cuenta cuenta1 = new Cuenta("Cristiano Ronaldo", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("Lionel Messi", new BigDecimal("1500.8989"));

            Banco banco = new Banco();
            banco.addCueta(cuenta1);
            banco.addCueta(cuenta2);

            banco.setNombre("Banco Falso");
            banco.transferir(cuenta2, cuenta1, new BigDecimal("500"));

            assertAll(() -> {
                assertEquals("1000.8989", cuenta2.getSaldo().toPlainString());
            }, () -> {
                assertEquals("3000", cuenta1.getSaldo().toPlainString());
            }, () -> {
                assertEquals(2, banco.getCuentas().size());
            }, () -> {
                assertEquals("Banco Falso", cuenta1.getBanco().getNombre());
            }, () -> {
                assertEquals("Lionel Messi", banco.getCuentas().stream()
                        .filter(c -> c.getPersona().equals("Lionel Messi"))
                        .findFirst().get().getPersona());
            }, () -> {
                assertTrue(banco.getCuentas().stream()
                        .anyMatch(c -> c.getPersona().equals("Lionel Messi")));
            });
        }
    }



    // clases de test anidadas

    @Nested
    class SistemaOperativoTest{
        @Test @EnabledOnOs(OS.WINDOWS)
        void testSoloWindows(){

        }
        @Test @EnabledOnOs({OS.LINUX, OS.MAC})
        void testSoloLinuxMac(){

        }
        @Test @DisabledOnOs(OS.WINDOWS)
        void testNoWindows(){

        }
    }

    @Nested
    class JavaVersionTest{

        @Test @EnabledOnJre(JRE.JAVA_9)
        void soloJDK9(){

        }

        @Test
        void printSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((key, value) -> System.out.println(key + ": " + value));
        }

        @Test @EnabledIfSystemProperty(named = "java.version", matches = "18") // ".*15.*"
        void testJavaVersion() {
            System.out.println("si tengo la misma version de java");
        }
    }

    @Nested
    class SystemPropertiesTest{

        @Test @DisabledIfSystemProperty(named = "os.arch", matches = ".*64.*")
        void soloArquitectura64() {
        }

        @Test @EnabledIfSystemProperty(named = "ENV", matches = "dev")
        void testSoloDev() {
        }
    }

    @Nested
    class VariableAmbienteTest{
        @Test
        void printVariablesAmbiente() {
            Map<String, String> getenv = System.getenv();
            getenv.forEach((nombre, valor) -> System.out.println(nombre + " = " + valor));
        }

        @Test @EnabledIfEnvironmentVariable(named = "JAVA_HOME", matches = ".*jdk-18.0.2.1.*")
        void testJavaHome() {
        }

        @Test @EnabledIfEnvironmentVariable(named = "NUMBER_OF_PROCESSORS", matches = "8")
        void testProcesadores() {
        }

        @Test @EnabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "dev")
        void testEnv() {
        }
    }

    @Test @DisplayName("Test Saldo Cuenta Dev")
    void testSaldoCuentaDev(){
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumeTrue(esDev);
        assertEquals(100023.123123, cuenta.getSaldo().doubleValue());
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test @DisplayName("Test Saldo Cuenta Dev 2")
    void testSaldoCuentaDev2(){
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumingThat(esDev, () -> {
            assertEquals(100023.123123, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        });
    }

    @DisplayName("Probando Cuenta")
    @RepeatedTest(value = 5, name = "{displayName} repeticion numero {currentRepetition} de {totalRepetitions}")
    void testDebitoCuentaRepetido(RepetitionInfo info) {
        if (info.getCurrentRepetition() == 3){
            System.out.println("Lo hice!");
        }
        Cuenta cuenta = new Cuenta("Juan", new BigDecimal("1000.12345"));
        cuenta.debito(new BigDecimal(100));
        assertNotNull(cuenta.getSaldo());
        assertEquals(900, cuenta.getSaldo().intValue());
        assertEquals("900.12345", cuenta.getSaldo().toPlainString());
    }

    @Tag("param")
    @Nested
    class PruebasParametrizadas{

        // test parametrizado
        @ParameterizedTest(name = "numero {index} ejecuntadon con valor {0} - {argumentsWithNames}")
//    @ValueSource(strings = {"100", "200", "300", "500", "700", "1000.12345"})
        @ValueSource(ints = {100,200,300,500,700,1000})
        void testDebitoCuenta(int monto) {
            Cuenta cuenta = new Cuenta("Juan", new BigDecimal("1000.12345"));
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecuntadon con valor {0} - {argumentsWithNames}")
        @CsvSource({"1,100", "2,200", "3,300", "4,500", "5,700", "6,1000.12345"})
        void testDebitoCuentaCsvSource(String index, String monto) {
            System.out.println(index + " -> " + monto);
            Cuenta cuenta = new Cuenta("Juan", new BigDecimal("1000.12345"));
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecuntadon con valor {0} - {argumentsWithNames}")
        @CsvSource({"200,100", "250,200", "299,300", "400,500", "750,700", "1000.12345,1000.12345"})
        void testDebitoCuentaCsvSource2(String saldo, String monto) {
            System.out.println(saldo + " -> " + monto);
            Cuenta cuenta = new Cuenta("Juan", new BigDecimal("1000.12345"));
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecuntadon con valor {0} - {argumentsWithNames}")
        @CsvFileSource(resources = "/data.csv")
        void testDebitoCuentaCsvFileSource(String monto) {
            Cuenta cuenta = new Cuenta("Juan", new BigDecimal("1000.12345"));
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }
    }


    @Tag("param")
    @ParameterizedTest(name = "numero {index} ejecuntadon con valor {0} - {argumentsWithNames}")
    @MethodSource("montoList")
    void testDebitoCuentaMethodSource(String monto) {
        Cuenta cuenta = new Cuenta("Juan", new BigDecimal("1000.12345"));
        cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    static List<String> montoList(){
        return Arrays.asList("100", "200", "300", "500", "700", "1000.12345");
    }

    @Nested
    @Tag("timeout")
    class EjemploTimeOuttest{

        @Test
        @Timeout(5) // si la prueba dura mas de 5sg da error
        void pruebaTimeOut() throws InterruptedException {
            TimeUnit.SECONDS.sleep(4);
        }

        @Test
        @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
        void pruebaTimeOut2() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(450);
        }

        @Test
        void TestTimeOutAssertions() {
            assertTimeout(Duration.ofMillis(1000), () -> {
                TimeUnit.MILLISECONDS.sleep(900);
            });
        }
    }

}