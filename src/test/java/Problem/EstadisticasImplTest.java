package Problem;

import static Problem.NState.StateMove.*;
import es.usc.citius.hipster.model.impl.WeightedNode;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author marti
 */
public class EstadisticasImplTest {
    
    public EstadisticasImplTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of costeIndividuo method, of class EstadisticasImpl.
     */
    @Test
    public void testCosteIndividuoNull() {
        ArrayList<InterfazTraza> t = null;
        EstadisticasImpl instance = new EstadisticasImpl();
        Double expResult = 0d;
        Double result = instance.costeIndividuo(t);
        assertEquals(expResult, result);
    }

    @Test
    public void testCosteIndividuoVacio() {
        ArrayList<InterfazTraza> t = new ArrayList();
        EstadisticasImpl instance = new EstadisticasImpl();
        Double expResult = 0d;
        Double result = instance.costeIndividuo(t);
        assertEquals(expResult, result);
    }

    @Test
    public void testCosteIndividuoDatos() {
        ArrayList<InterfazTraza> t = new ArrayList();
        InterfazTraza e = new Traza();
        e.setScore(4);
        e.setNumRepeticiones(1);

        InterfazTraza e2 = new Traza();
        e2.setScore(8.5);
        e2.setNumRepeticiones(3);

        t.add(e);
        t.add(e2);
        EstadisticasImpl instance = new EstadisticasImpl();
        Double expResult = 29.5d;
        Double result = instance.costeIndividuo(t);
        assertEquals(expResult, result);
    }

    /**
     * Test of fitness method, of class EstadisticasImpl.
     */
    @Test
    public void testFitnessNull() {
        ArrayList<InterfazTraza> t = null;
        EstadisticasImpl instance = new EstadisticasImpl();
        Double expResult = 0d;
        Double result = instance.fitness(t);
        assertEquals(expResult, result);
    }

    @Test
    public void testFitnessVacio() {
        ArrayList<InterfazTraza> t = new ArrayList();
        EstadisticasImpl instance = new EstadisticasImpl();
        Double expResult = 0d;
        Double result = instance.fitness(t);
        assertEquals(expResult, result);
    }

    @Test
    public void testFitnessDatos() {
        ArrayList<InterfazTraza> t = new ArrayList();
        InterfazTraza e = new Traza();
        e.anadirTarea(1);
        e.anadirTarea(2);
        e.anadirTarea(3);
        e.anadirTarea(4);
        e.anadirTarea(5);
        e.anadirTarea(6);
        e.setScore(4);
        e.setNumRepeticiones(1);

        InterfazTraza e2 = new Traza();
        e2.anadirTarea(1);
        e2.anadirTarea(2);
        e2.anadirTarea(3);
        e2.anadirTarea(4);
        e2.setScore(8.5);
        e2.setNumRepeticiones(3);

        t.add(e);
        t.add(e2);

        EstadisticasImpl instance = new EstadisticasImpl();
        instance.setCosteCorto(1d);
        //Teoricamente daría -0.2291 pero como es negativo es 0
        Double expResult = 0d;
        Double result = instance.fitness(t);
        assertEquals(expResult, result);
    }

    @Test
    public void testFitnessDatos2() {
        ArrayList<InterfazTraza> t = new ArrayList();
        InterfazTraza e = new Traza();
        e.anadirTarea(1);
        e.anadirTarea(2);
        e.anadirTarea(3);
        e.anadirTarea(4);
        e.anadirTarea(5);
        e.anadirTarea(6);
        e.setScore(4);
        e.setNumRepeticiones(1);

        InterfazTraza e2 = new Traza();
        e2.anadirTarea(1);
        e2.anadirTarea(2);
        e2.anadirTarea(3);
        e2.anadirTarea(4);
        e2.setScore(8.5);
        e2.setNumRepeticiones(3);

        t.add(e);
        t.add(e2);

        EstadisticasImpl instance = new EstadisticasImpl();
        instance.setCosteCorto(3d);
        Double expResult = 0.1805d;
        Double result = instance.fitness(t);
        assertEquals(expResult, result, 0.0001);
    }

    /**
     * Test of menorCamino method, of class EstadisticasImpl.
     */
    @Test
    public void testMenorCaminoNull() {
        ArrayList<WeightedNode> nodosSalida = null;
        EstadisticasImpl instance = new EstadisticasImpl();
        int expResult = 999999999;
        int result = instance.menorCamino(nodosSalida);
        assertEquals(expResult, result);
    }

    @Test
    public void testMenorCaminoVacio() {
        ArrayList<WeightedNode> nodosSalida = new ArrayList();
        EstadisticasImpl instance = new EstadisticasImpl();
        int expResult = 999999999;
        int result = instance.menorCamino(nodosSalida);
        assertEquals(expResult, result);
    }

    @Test
    public void testMenorCaminoDatos() {
        ArrayList<WeightedNode> nodosSalida = new ArrayList();
        WeightedNode e = new WeightedNode(null, null, null, null, null, null);
        WeightedNode e1 = new WeightedNode(e, null, OK, null, null, null);
        WeightedNode e2 = new WeightedNode(e1, null, SKIP, null, null, null);

        WeightedNode f = new WeightedNode(null, null, null, null, null, null);
        WeightedNode f1 = new WeightedNode(f, null, INSERT, null, null, null);
        WeightedNode f2 = new WeightedNode(f1, null, INSERT, null, null, null);

        nodosSalida.add(e2);
        nodosSalida.add(f2);

        EstadisticasImpl instance = new EstadisticasImpl();
        int expResult = 1;
        int result = instance.menorCamino(nodosSalida);
        assertEquals(expResult, result);
    }

    /**
     * Test of fitnessNuevo method, of class EstadisticasImpl.
     */
    @Test
    public void testFitnessNuevoNull() {
        ArrayList<InterfazTraza> t = null;
        ArrayList<WeightedNode> nodosSalida = null;
        EstadisticasImpl instance = new EstadisticasImpl();
        Double expResult = 0d;
        Double result = instance.fitnessNuevo(t, nodosSalida);
        assertEquals(expResult, result);
    }

    @Test
    public void testFitnessNuevoDatosVacio() {
        ArrayList<InterfazTraza> t = new ArrayList();
        InterfazTraza e = new Traza();
        e.anadirTarea(1);
        e.anadirTarea(2);
        e.anadirTarea(3);
        e.anadirTarea(4);
        e.anadirTarea(5);
        e.anadirTarea(6);
        e.setScore(4);
        e.setNumRepeticiones(1);

        InterfazTraza e2 = new Traza();
        e2.anadirTarea(1);
        e2.anadirTarea(2);
        e2.anadirTarea(3);
        e2.anadirTarea(4);
        e2.setScore(8.5);
        e2.setNumRepeticiones(3);

        t.add(e);
        t.add(e2);

        ArrayList<WeightedNode> nodosSalida = new ArrayList();
        EstadisticasImpl instance = new EstadisticasImpl();
        Double expResult = 0.9999d;
        Double result = instance.fitnessNuevo(t, nodosSalida);
        assertEquals(expResult, result, 0.0001);
    }

    @Test
    public void testFitnessNuevoDatosDatos() {
        ArrayList<InterfazTraza> t = new ArrayList();
        InterfazTraza e = new Traza();
        e.anadirTarea(1);
        e.anadirTarea(2);
        e.anadirTarea(3);
        e.anadirTarea(4);
        e.anadirTarea(5);
        e.anadirTarea(6);
        e.setScore(4);
        e.setNumRepeticiones(1);

        InterfazTraza e2 = new Traza();
        e2.anadirTarea(1);
        e2.anadirTarea(2);
        e2.anadirTarea(3);
        e2.anadirTarea(4);
        e2.setScore(8.5);
        e2.setNumRepeticiones(3);

        t.add(e);
        t.add(e2);

        ArrayList<WeightedNode> nodosSalida = new ArrayList();
        WeightedNode n = new WeightedNode(null, null, null, null, null, null);
        WeightedNode n1 = new WeightedNode(n, null, OK, null, null, null);
        WeightedNode n2 = new WeightedNode(n1, null, SKIP, null, null, null);

        WeightedNode f = new WeightedNode(null, null, null, null, null, null);
        WeightedNode f1 = new WeightedNode(f, null, INSERT, null, null, null);
        WeightedNode f2 = new WeightedNode(f1, null, INSERT, null, null, null);

        nodosSalida.add(n2);
        nodosSalida.add(f2);

        EstadisticasImpl instance = new EstadisticasImpl();
        Double expResult = 0.0d;
        Double result = instance.fitnessNuevo(t, nodosSalida);
        assertEquals(expResult, result, 0.0001);
    }

    @Test
    public void testFitnessNuevoDatosDatos2() {
        ArrayList<InterfazTraza> t = new ArrayList();
        InterfazTraza e = new Traza();
        e.anadirTarea(1);
        e.anadirTarea(2);
        e.anadirTarea(3);
        e.anadirTarea(4);
        e.anadirTarea(5);
        e.anadirTarea(6);
        e.setScore(4);
        e.setNumRepeticiones(1);

        InterfazTraza e2 = new Traza();
        e2.anadirTarea(1);
        e2.anadirTarea(2);
        e2.anadirTarea(3);
        e2.anadirTarea(4);
        e2.setScore(8.5);
        e2.setNumRepeticiones(3);

        t.add(e);
        t.add(e2);

        ArrayList<WeightedNode> nodosSalida = new ArrayList();
        WeightedNode f = new WeightedNode(null, null, null, null, null, null);
        WeightedNode f1 = new WeightedNode(f, null, INSERT, null, null, null);
        WeightedNode f2 = new WeightedNode(f1, null, INSERT, null, null, null);
        WeightedNode f3 = new WeightedNode(f2, null, INSERT, null, null, null);

        nodosSalida.add(f3);

        EstadisticasImpl instance = new EstadisticasImpl();
        Double expResult = 0.1805d;
        Double result = instance.fitnessNuevo(t, nodosSalida);
        assertEquals(expResult, result, 0.0001);
    }
    
    /**
     * Test of tareasPrefijo method, of class EstadisticasImpl.
     */
    @Test
    public void testTareasPrefijoNull() {
        ArrayList<InterfazTraza> t = null;
        ArrayList<Integer> prefijo = null;
        EstadisticasImpl instance = new EstadisticasImpl();
        Integer expResult = 0;
        Integer result = instance.tareasPrefijo(t, prefijo);
        assertEquals(expResult, result);
    }

    @Test
    public void testTareasPrefijoVacio() {
        ArrayList<InterfazTraza> t = new ArrayList();
        ArrayList<Integer> prefijo = new ArrayList();
        EstadisticasImpl instance = new EstadisticasImpl();
        Integer expResult = 0;
        Integer result = instance.tareasPrefijo(t, prefijo);
        assertEquals(expResult, result);
    }

    @Test
    public void testTareasPrefijoDatos() {
        ArrayList<InterfazTraza> t = new ArrayList();
        InterfazTraza e = new Traza();
        e.anadirTarea(1);
        e.anadirTarea(2);
        e.anadirTarea(3);
        e.anadirTarea(4);
        e.anadirTarea(5);
        e.anadirTarea(6);

        InterfazTraza e2 = new Traza();
        e2.anadirTarea(1);
        e2.anadirTarea(2);
        e2.anadirTarea(3);
        e2.anadirTarea(5);

        t.add(e);
        t.add(e2);

        ArrayList<Integer> prefijo = new ArrayList();
        prefijo.add(1);
        prefijo.add(2);
        prefijo.add(3);

        EstadisticasImpl instance = new EstadisticasImpl();
        Integer expResult = 2;
        Integer result = instance.tareasPrefijo(t, prefijo);
        assertEquals(expResult, result);
    }
    /**
     * Test of precision method, of class EstadisticasImpl.
     */
    @Test
    public void testPrecisionNull() {
        ArrayList<InterfazTraza> t = null;
        ArrayList<WeightedNode> nodosSalida = null;
        EstadisticasImpl instance = new EstadisticasImpl();
        Double expResult = 0d;
        Double result = instance.precision(t, nodosSalida);
        assertEquals(expResult, result);
    }

    @Test
    public void testPrecisionVacio() {
        ArrayList<InterfazTraza> t = new ArrayList();
        ArrayList<WeightedNode> nodosSalida = new ArrayList();
        EstadisticasImpl instance = new EstadisticasImpl();
        Double expResult = 0d;
        Double result = instance.precision(t, nodosSalida);
        assertEquals(expResult, result);
    }

    @Test
    public void testPrecisionDatos() {
        ArrayList<InterfazTraza> t = new ArrayList();
        ArrayList<WeightedNode> nodosSalida = new ArrayList();
        EstadisticasImpl instance = new EstadisticasImpl();
        Double expResult = 0d;
        Double result = instance.precision(t, nodosSalida);
        assertEquals(expResult, result);
        fail("Chungo");
    }
    /**
     * Test of tareasActivasEstado method, of class EstadisticasImpl.
     */
    @Test
    public void testTareasActivasEstado() {
        System.out.println("tareasActivasEstado");
        ArrayList<WeightedNode> nodosSalida = null;
        EstadisticasImpl instance = new EstadisticasImpl();
        ArrayList<ArrayList<NState.State>> expResult = null;
        ArrayList<ArrayList<NState.State>> result = instance.tareasActivasEstado(nodosSalida);
        assertEquals(expResult, result);
        //TODO review the generated test code and remove the default call to fail.
        fail("Chungo");
    }
    
}
