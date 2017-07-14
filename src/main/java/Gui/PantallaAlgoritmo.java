/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Gui;

import Algoritmos.AlgoritmoA;
import Algoritmos.AlgoritmoAD;
import Salida.InterfazSalida;
import Problem.InterfazTraza;
import Problem.NState;
import Problem.Readers;
import static Problem.NState.StateMove.*;
import domainLogic.exceptions.EmptyLogException;
import domainLogic.exceptions.InvalidFileExtensionException;
import domainLogic.exceptions.MalformedFileException;
import domainLogic.exceptions.NonFinishedWorkflowException;
import domainLogic.exceptions.WrongLogEntryException;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import es.usc.citius.hipster.model.AbstractNode;
import es.usc.citius.hipster.model.impl.WeightedNode;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author marti
 */
public class PantallaAlgoritmo extends javax.swing.JPanel implements InterfazSalida {

    /**
     * Creates new form Algoritmo
     */
    private Pantalla v;
    private int totalTrazas;

    public Pantalla getV() {
        return v;
    }

    public void setV(Pantalla v) {
        this.v = v;
    }

    private String textoAlineamientos;
    private DefaultTableModel defaultTableModel;
    private HashMap<String, AbstractNode> ali;
    private HashMap<String, InterfazTraza> traces;
    private String[] paths;

    private void inicializarTabla() {
        defaultTableModel = new DefaultTableModel();
        defaultTableModel.setColumnCount(0);
        Trazas.setShowGrid(true);
        Trazas.setShowVerticalLines(true);
        Trazas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Trazas.setRowSelectionAllowed(true);
        Trazas.setVisible(true);
        Trazas.setColumnSelectionAllowed(false);
        Trazas.setModel(defaultTableModel);
        defaultTableModel.addColumn("Nº");
        defaultTableModel.addColumn("Nombre");
        defaultTableModel.addColumn("Nº Repeticiones");
    }

    public PantallaAlgoritmo(String path, String pathModel) {
        initComponents();
        modelo.setEditable(false);
        aliEditor.setEditable(false);
        aliEditor.setContentType("text/html");
        textoAlineamientos = "";

        ali = new HashMap<String, AbstractNode>();
        traces = new HashMap<String, InterfazTraza>();
        this.inicializarTabla();
        String[] params = {path, pathModel};
        this.paths = params;
    }

    public void alinear() {
        try {
            AlgoritmoA.main(paths, this);
        } catch (IOException | EmptyLogException | WrongLogEntryException | NonFinishedWorkflowException | InvalidFileExtensionException | MalformedFileException ex) {
            Logger.getLogger(PantallaConfiguracion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void alinear2() {
        try {
            AlgoritmoAD.main(paths, this);
        } catch (IOException | EmptyLogException | WrongLogEntryException | NonFinishedWorkflowException | InvalidFileExtensionException | MalformedFileException ex) {
            Logger.getLogger(PantallaConfiguracion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        modelo = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        fitness = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        precission = new javax.swing.JLabel();
        tiempoC = new javax.swing.JLabel();
        coste = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        aliEditor = new javax.swing.JEditorPane();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        memoria = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        costeAli = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        tiempoCAli = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        memoria1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Trazas = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        mensajeProceso = new javax.swing.JLabel();

        setLayout(null);

        modelo.setColumns(20);
        modelo.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        modelo.setRows(5);
        jScrollPane2.setViewportView(modelo);

        add(jScrollPane2);
        jScrollPane2.setBounds(18, 38, 650, 160);

        jLabel1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel1.setText("Fitness:");
        add(jLabel1);
        jLabel1.setBounds(778, 78, 70, 20);

        jLabel6.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel6.setText("MODELO");
        add(jLabel6);
        jLabel6.setBounds(18, 8, 170, 20);

        jLabel2.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel2.setText("Precission:");
        add(jLabel2);
        jLabel2.setBounds(758, 108, 90, 20);

        fitness.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        fitness.setText("-");
        add(fitness);
        fitness.setBounds(848, 78, 160, 20);

        jLabel5.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel5.setText("Coste:");
        add(jLabel5);
        jLabel5.setBounds(788, 48, 70, 20);

        precission.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        precission.setText("-");
        add(precission);
        precission.setBounds(848, 108, 160, 20);

        tiempoC.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        tiempoC.setText("-");
        add(tiempoC);
        tiempoC.setBounds(848, 138, 130, 20);

        coste.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        coste.setText("-");
        add(coste);
        coste.setBounds(848, 48, 150, 20);

        jLabel8.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel8.setText("MEJOR ALINEAMIENTO");
        add(jLabel8);
        jLabel8.setBounds(624, 219, 206, 20);

        aliEditor.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jScrollPane3.setViewportView(aliEditor);

        add(jScrollPane3);
        jScrollPane3.setBounds(487, 257, 270, 426);

        jLabel10.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel10.setText("Tiempo Total Cálculo: ");
        add(jLabel10);
        jLabel10.setBounds(688, 138, 170, 20);

        jLabel11.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel11.setText("Memoria Consumida:");
        add(jLabel11);
        jLabel11.setBounds(686, 167, 170, 20);

        memoria.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        memoria.setText("-");
        add(memoria);
        memoria.setBounds(848, 167, 130, 20);

        jLabel7.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel7.setText("Coste:");
        add(jLabel7);
        jLabel7.setBounds(780, 320, 45, 20);

        costeAli.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        costeAli.setText("-");
        add(costeAli);
        costeAli.setBounds(840, 320, 170, 20);

        jLabel12.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel12.setText("Tiempo Cálculo: ");
        add(jLabel12);
        jLabel12.setBounds(780, 370, 115, 20);

        tiempoCAli.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        tiempoCAli.setText("-");
        add(tiempoCAli);
        tiempoCAli.setBounds(943, 371, 99, 20);

        jLabel13.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel13.setText("Memoria Consumida:");
        add(jLabel13);
        jLabel13.setBounds(775, 419, 156, 20);

        memoria1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        memoria1.setText("-");
        add(memoria1);
        memoria1.setBounds(946, 419, 77, 20);

        Trazas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        Trazas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TrazasMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(Trazas);

        add(jScrollPane1);
        jScrollPane1.setBounds(18, 257, 386, 426);

        jLabel9.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel9.setText("TRAZAS");
        add(jLabel9);
        jLabel9.setBounds(180, 220, 72, 20);

        mensajeProceso.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        mensajeProceso.setText("Calculando alineamiento x/y");
        add(mensajeProceso);
        mensajeProceso.setBounds(300, 730, 500, 22);
    }// </editor-fold>//GEN-END:initComponents

    private void TrazasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TrazasMouseClicked
        int fila = Trazas.getSelectedRow();
        if (fila >= 0) {
            this.imprimirAlineamiento((String) defaultTableModel.getValueAt(fila, 1));
        }
    }//GEN-LAST:event_TrazasMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Trazas;
    private javax.swing.JEditorPane aliEditor;
    private javax.swing.JLabel coste;
    private javax.swing.JLabel costeAli;
    private javax.swing.JLabel fitness;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel memoria;
    private javax.swing.JLabel memoria1;
    private javax.swing.JLabel mensajeProceso;
    private javax.swing.JTextArea modelo;
    private javax.swing.JLabel precission;
    private javax.swing.JLabel tiempoC;
    private javax.swing.JLabel tiempoCAli;
    // End of variables declaration//GEN-END:variables
    @Override
    public void estadisticasModelo(CMIndividual ind, double coste, long tiempo, double m) {
        this.coste.setText(Objects.toString(coste, null));
        Double fitnes = ind.getFitness().getCompleteness();
        this.fitness.setText(fitnes.toString());
        Double precision = ind.getFitness().getPreciseness();
        this.precission.setText(precision.toString());
        String t = Objects.toString(tiempo, null);
        this.tiempoC.setText(t + " ms");
    }

    @Override
    public void ActualizarTrazas(InterfazTraza trace, AbstractNode nodo, boolean ad, CMIndividual ind) {
        int filas = defaultTableModel.getRowCount();
        defaultTableModel.addRow(new Object[]{filas + 1, trace.getId(), trace.getNumRepeticiones()});
        ali.put(trace.getId(), nodo);
        traces.put(trace.getId(), trace);

        this.mensajeProceso.setText("Calculando alineamiento " + (filas + 1) + "/" + totalTrazas);
    }

    public void imprimirAlineamiento(String traza) {
        String str = "";

        WeightedNode nodo = (WeightedNode) ali.get(traza);
        InterfazTraza trace = traces.get(traza);

        Iterator it2 = nodo.path().iterator();
        //La primera iteración corresponde con el Estado Inicial, que no imprimimos
        it2.next();
        str = str + "<h3> `" + traza + "´</h3>";
        str = str + "<div> &emsp &emsp TRAZA &emsp MODELO";
        while (it2.hasNext()) {
            WeightedNode node = (WeightedNode) it2.next();
            NState.State s = (NState.State) node.state();
            if (node.action().equals(SINCRONO)) {
                str = str + "<br> &emsp &emsp &emsp " + trace.leerTarea(s.getPos() - 1) + " &emsp &emsp &emsp " + s.getTarea();
            } else if (node.action().equals(MODELO)) {
                str = str + "<br> &emsp &emsp &emsp >> &emsp &emsp &ensp " + s.getTarea();
            } else if (node.action().equals(MODELO_FORZADO)) {
                str = str + "<br> &emsp &emsp &emsp >>' &emsp &emsp &ensp " + s.getTarea();
            } else {
                str = str + "<br> &emsp &emsp &emsp " + trace.leerTarea(s.getPos() - 1) + " &emsp &emsp &emsp >> ";
            }
        }
        str = str + "</div>";
        this.aliEditor.setText(str);
//        DecimalFormat df = new DecimalFormat("#,#####");
//        Double coste = Double.valueOf(df.format(trace.getScoreRepetido()));
        this.costeAli.setText(Double.toString(trace.getScoreRepetido()));
//        this.costeAli.setText(Double.toString(coste));
        this.tiempoCAli.setText(Double.toString(trace.getTiempoC()) + " ms");
    }

    @Override
    public void imprimirModelo(CMIndividual ind) {
        String str = "<h3>Modelo " + 1 + " impreso</h3>";
        modelo.append(str);
    }

    @Override
    public void setTotalTrazas(int size) {
        totalTrazas = size;
    }
}
