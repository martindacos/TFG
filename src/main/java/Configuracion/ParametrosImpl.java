package Configuracion;

/**
 *
 * @author marti
 */
public class ParametrosImpl implements Parametros {

    private double C_ARTIFICIAL = COSTE_ARTIFICIAL;
    private double C_SKIP = COSTE_SKIP;
    private double C_INSERT = COSTE_INSERT;
    //Este coste no puede ser de 0
    private double C_OK = COSTE_OK;
    private static ParametrosImpl miParametrosImpl;

    public static ParametrosImpl getParametrosImpl() {
        if (miParametrosImpl == null) {
            miParametrosImpl = new ParametrosImpl();
        }
        return miParametrosImpl;
    }
    
    public double getOK() {
        return C_OK;
    }

    public double getINSERT() {
        return C_INSERT;
    }

    public double getSKIP() {
        return C_SKIP;
    }

    public double getARTIFICIAL() {
        return C_ARTIFICIAL;
    }
    
    public void setOK(double c) {
        C_OK = c;
    }

    public void setINSERT(double c) {
        C_INSERT = c;
    }

    public void setSKIP(double c) {
        C_SKIP = c;
    }

    public void setARTIFICIAL(double C_ARTIFICIAL) {
        this.C_ARTIFICIAL = C_ARTIFICIAL;
    }
}
