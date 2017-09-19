package Configuracion;

/**
 *
 * @author marti
 */
//Interfaz que indica los costes asociados a cada movimientoposible del algoritmo
public interface Parametros {

    public static final double COSTE_MODELO_FORZADO = 3d;
    public static final double COSTE_MODELO = 3d;
    public static final double COSTE_TRAZA = 1d;
    //Este coste no puede ser de 0. Retaremos este añadido cuando calculemos los costes
    public static final double COSTE_SINCRONO = 0.00001d;

    //Parámetros específicos del Algoritmo AD*
    //public static final double EPSILON_INICIAL = 100000d;
    public static final double EPSILON_INICIAL = 1d;
    public static final double EPSILON_INTERVALO = 100000d;
    //El menor valor posible de Épsilon debería ser 1
    //public static final double EPSILON_FINAL = 100000d;
    public static final double EPSILON_FINAL = 1d;
}
