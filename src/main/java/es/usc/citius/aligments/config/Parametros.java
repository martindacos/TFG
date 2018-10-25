package es.usc.citius.aligments.config;

/**
 *
 * @author marti
 */
//Interfaz que indica los costes asociados a cada movimientoposible del algoritmo
public interface Parametros {

    double COSTE_MODELO_FORZADO = 3d;
    double PENALIZACION_FORZADO = 1d;
    double COSTE_MODELO = 1d;
    double COSTE_TRAZA = 1d;
    //Este coste no puede ser de 0. Restaremos este añadido cuando calculemos los costes
    double COSTE_SINCRONO = 0.00001d;

    //Parámetros específicos del Algoritmo AD*
    //double EPSILON_INICIAL = 100000d;
    double EPSILON_INICIAL = 1d;
    double EPSILON_INTERVALO = 100000d;
    //El menor valor posible de Épsilon debería ser 1
    double EPSILON_FINAL = 100000d;
    //public static final double EPSILON_FINAL = 1d;

    String HEURISTIC_TRACE = "HEURISTIC_TRACE";
    String HEURISTIC_MODEL = "HEURISTIC_MODEL";

    //A Star like CoBeFra

    double MODEL_COST = 1d;
    double LOG_COST = 1d;
    double SYNC_COST = 0d;
    double INVISIBLE_COST = 0d;

    //Difference with cost SYNC
    int delta = 1000;
}
