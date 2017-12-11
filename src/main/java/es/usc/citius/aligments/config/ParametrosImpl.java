package es.usc.citius.aligments.config;

/**
 *
 * @author marti
 */
public class ParametrosImpl implements Parametros {

    private double C_MODELO_FORZADO = COSTE_MODELO_FORZADO;
    private double C_MODELO = COSTE_MODELO;
    private double C_TRAZA = COSTE_TRAZA;
    //Este coste no puede ser de 0
    private double C_SINCRONO = COSTE_SINCRONO;
    private static ParametrosImpl miParametrosImpl;

    private double E_INICIAL = EPSILON_INICIAL;
    private double E_INTERVALO = EPSILON_INTERVALO;
    private double E_FINAL = EPSILON_FINAL;

    public static ParametrosImpl getParametrosImpl() {
        if (miParametrosImpl == null) {
            miParametrosImpl = new ParametrosImpl();
        }
        return miParametrosImpl;
    }

    public double getC_MODELO_FORZADO() {
        return C_MODELO_FORZADO;
    }

    public void setC_MODELO_FORZADO(double C_MODELO_FORZADO) {
        this.C_MODELO_FORZADO = C_MODELO_FORZADO;
    }

    public double getC_MODELO() {
        return C_MODELO;
    }

    public void setC_MODELO(double C_MODELO) {
        this.C_MODELO = C_MODELO;
    }

    public double getC_TRAZA() {
        return C_TRAZA;
    }

    public void setC_TRAZA(double C_TRAZA) {
        this.C_TRAZA = C_TRAZA;
    }

    public double getC_SINCRONO() {
        return C_SINCRONO;
    }

    public void setC_SINCRONO(double C_SINCRONO) {
        this.C_SINCRONO = C_SINCRONO;
    }

    public double getE_INICIAL() {
        return E_INICIAL;
    }

    public void setE_INICIAL(double E_INICIAL) {
        this.E_INICIAL = E_INICIAL;
    }

    public double getE_INTERVALO() {
        return E_INTERVALO;
    }

    public void setE_INTERVALO(double E_INTERVALO) {
        this.E_INTERVALO = E_INTERVALO;
    }

    public double getE_FINAL() {
        return E_FINAL;
    }

    public void setE_FINAL(double E_FINAL) {
        this.E_FINAL = E_FINAL;
    }
}
