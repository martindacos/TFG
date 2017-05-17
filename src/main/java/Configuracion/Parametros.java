package Configuracion;

/**
 *
 * @author marti
 */
//Interfaz que indica los costes asociados a cada movimientoposible del algoritmo
public interface Parametros {
    public static final double COSTE_ARTIFICIAL = 3d;
    public static final double COSTE_SKIP = 3d;
    public static final double COSTE_INSERT = 1d;
    //Este coste no puede ser de 0
    public static final double COSTE_OK = 0.00001d;
}
