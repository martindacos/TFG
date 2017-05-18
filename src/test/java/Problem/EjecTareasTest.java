package Problem;

import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.HashMap;
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
public class EjecTareasTest {

    public EjecTareasTest() {
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
     * Test of leerTareaSkip method, of class EjecTareas.
     */
    @Test
    public void testLeerTareaSkipNull() {
        EjecTareas instance = new EjecTareas();
        Integer expResult = null;
        Integer result = instance.leerTareaSkip();
        assertEquals(expResult, result);
    }

    @Test
    public void testLeerTareaSkipVacio() {
        EjecTareas instance = new EjecTareas();
        instance.clear();
        Integer expResult = null;
        Integer result = instance.leerTareaSkip();
        assertEquals(expResult, result);
    }

    @Test
    public void testLeerTareaSkipDatos() {
        EjecTareas instance = new EjecTareas();
        instance.clear();
        instance.anadirSkip(0);
        instance.anadirSkip(1);
        Integer expResult = 0;
        Integer result = instance.leerTareaSkip();
        assertEquals(expResult, result);
        Integer expResult2 = 1;
        Integer result2 = instance.leerTareaSkip();
        assertEquals(expResult2, result2);
        Integer expResult3 = null;
        Integer result3 = instance.leerTareaSkip();
        assertEquals(expResult3, result3);
    }

    /**
     * Test of leerTareaArtificial method, of class EjecTareas.
     */
    @Test
    public void testLeerTareaArtificial() {
        System.out.println("leerTareaArtificial");
        EjecTareas instance = new EjecTareas();
        Integer expResult = null;
        Integer result = instance.leerTareaArtificial();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testLeerTareaArtificialNull() {
        EjecTareas instance = new EjecTareas();
        Integer expResult = null;
        Integer result = instance.leerTareaArtificial();
        assertEquals(expResult, result);
    }

    @Test
    public void testLeerTareaArtificialVacio() {
        EjecTareas instance = new EjecTareas();
        instance.clear();
        Integer expResult = null;
        Integer result = instance.leerTareaArtificial();
        assertEquals(expResult, result);
    }

    @Test
    public void testLeerTareaArtificialDatos() {
        EjecTareas instance = new EjecTareas();
        instance.clear();
        HashMap<Integer, Integer> a = new HashMap();
        a.put(1, 0);
        a.put(2, 0);
        instance.setTareasArtificiales(a);
        Integer expResult = 1;
        Integer result = instance.leerTareaArtificial();
        assertEquals(expResult, result);

        Integer expResult2 = 2;
        Integer result2 = instance.leerTareaArtificial();
        assertEquals(expResult2, result2);

        Integer expResult3 = null;
        Integer result3 = instance.leerTareaArtificial();
        assertEquals(expResult3, result3);
    }

    /**
     * Test of getNumOfTokens method, of class EjecTareas.
     */
    @Test
    public void testGetNumOfTokens() {
        System.out.println("getNumOfTokens");
        EjecTareas instance = new EjecTareas();
        int expResult = 0;
        int result = instance.getNumOfTokens();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of tareasTokensEntrada method, of class EjecTareas.
     */
    @Test
    public void testTareasTokensEntradaVacio() {
        EjecTareas instance = new EjecTareas();
        TIntHashSet expResult = new TIntHashSet();
        TIntHashSet result = instance.tareasTokensEntrada();
        assertEquals(expResult, result);
    }

    @Test
    public void testTareasTokensEntradaDatos() {
        EjecTareas instance = new EjecTareas();
        ArrayList<HashMap<TIntHashSet, Integer>> tokens = new ArrayList();
        HashMap<TIntHashSet, Integer> e = new HashMap();
        TIntHashSet key = new TIntHashSet();
        key.add(1);
        key.add(2);
        e.put(key, 1);
        HashMap<TIntHashSet, Integer> e2 = new HashMap();
        TIntHashSet key2 = new TIntHashSet();
        key2.add(3);
        e2.put(key, 0);
        HashMap<TIntHashSet, Integer> e3 = new HashMap();
        TIntHashSet key3 = new TIntHashSet();
        key3.add(1);
        key3.add(3);
        e3.put(key3, 2);
        tokens.add(e);
        tokens.add(e2);
        tokens.add(e3);
        instance.setTokens(tokens);

        TIntHashSet expResult = new TIntHashSet();
        expResult.add(1);
        expResult.add(2);
        expResult.add(3);

        TIntHashSet result = instance.tareasTokensEntrada();
        assertEquals(expResult, result);
    }

    /**
     * Test of tareasTokensRestantes method, of class EjecTareas.
     */
    @Test
    public void testTareasTokensRestantesVacio() {
        EjecTareas instance = new EjecTareas();
        Integer expResult = 0;
        Integer result = instance.tareasTokensRestantes();
        assertEquals(expResult, result);
    }

    @Test
    public void testTareasTokensRestantesDatos() {
        EjecTareas instance = new EjecTareas();
        ArrayList<HashMap<TIntHashSet, Integer>> tokens = new ArrayList();
        HashMap<TIntHashSet, Integer> e = new HashMap();
        TIntHashSet key = new TIntHashSet();
        key.add(1);
        key.add(2);
        e.put(key, 1);
        HashMap<TIntHashSet, Integer> e2 = new HashMap();
        TIntHashSet key2 = new TIntHashSet();
        key2.add(3);
        e2.put(key, 0);
        HashMap<TIntHashSet, Integer> e3 = new HashMap();
        TIntHashSet key3 = new TIntHashSet();
        key3.add(1);
        key3.add(3);
        e3.put(key3, 2);
        HashMap<TIntHashSet, Integer> e4 = new HashMap();
        TIntHashSet key4 = new TIntHashSet();
        key4.add(1);
        key4.add(4);
        e4.put(key4, 0);
        tokens.add(e);
        tokens.add(e2);
        tokens.add(e3);
        tokens.add(e4);
        instance.setTokens(tokens);

        instance.tareasTokensEntrada();
        Integer expResult = 2;
        Integer result = instance.tareasTokensRestantes();
        assertEquals(expResult, result);
    }

    /**
     * Test of tokenUsados method, of class EjecTareas.
     */
    @Test
    public void testTokenUsadosVacio() {
        int task = 0;
        EjecTareas instance = new EjecTareas();
        Integer expResult = null;
        Integer result = instance.tokenUsados(task);
        assertEquals(expResult, result);
    }

    @Test
    public void testTokenUsadosDatos() {
        EjecTareas instance = new EjecTareas();
        ArrayList<HashMap<TIntHashSet, Integer>> tokens = new ArrayList();
        HashMap<TIntHashSet, Integer> e = new HashMap();
        TIntHashSet key = new TIntHashSet();
        key.add(1);
        key.add(2);
        e.put(key, 1);
        HashMap<TIntHashSet, Integer> e2 = new HashMap();
        TIntHashSet key2 = new TIntHashSet();
        key2.add(3);
        e2.put(key, 0);
        HashMap<TIntHashSet, Integer> e3 = new HashMap();
        TIntHashSet key3 = new TIntHashSet();
        key3.add(1);
        key3.add(3);
        e3.put(key3, 2);
        HashMap<TIntHashSet, Integer> e4 = new HashMap();
        TIntHashSet key4 = new TIntHashSet();
        key4.add(1);
        key4.add(4);
        e4.put(key4, 0);
        tokens.add(e);
        tokens.add(e2);
        tokens.add(e3);
        tokens.add(e4);
        instance.setTokens(tokens);

        instance.tareasTokensEntrada();
        instance.tareasTokensRestantes();

        Integer expResult = null;
        Integer result = instance.tokenUsados(0);
        assertEquals(expResult, result);

        Integer expResult2 = 2;
        Integer result2 = instance.tokenUsados(1);
        assertEquals(expResult2, result2);

        Integer expResult3 = 1;
        Integer result3 = instance.tokenUsados(2);
        assertEquals(expResult3, result3);
    }

}
