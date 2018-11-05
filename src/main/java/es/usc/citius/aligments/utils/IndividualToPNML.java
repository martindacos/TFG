package es.usc.citius.aligments.utils;

import es.usc.citius.prodigen.domainLogic.petriNet.*;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.CMTask.CMSet;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.CMTask.CMTask;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.properties.IndividualCombUsage;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.util.permute.PermuteImpl;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static es.usc.citius.aligments.utils.PetriNetNamesConstants.*;

/**
 * Adaptation of ProDiGen class to Prom PNML format
 **/

public class IndividualToPNML {

    private HashMap<String, Set<String>> inputsLeft;
    private HashMap<String, Set<String>> outputsLeft;
    private HashMap<String, Set<String>> directOutputs;
    private HashMap<String, Set<String>> directInputs;
    private HashMap<String, Set<String>> from;
    private HashMap<String, Set<String>> to;
    private HashMap<String, Arc> arcs;      // arcos "fromto"
    private HashMap<String, Transition> transitions; // transiciones
    private HashMap<String, Place> places;
    private ArrayList<String> silentTransitions;


    private ArrayList<Set<String>> constraints;

    private int indexPlace;
    private int indexSilentTask;

    private HashMap<String, Integer> connectedPlacesInput;
    private HashMap<String, Integer> connectedPlacesOutput;

    private PermuteImpl permutator;

    public IndividualToPNML() {
        permutator = new PermuteImpl();
    }

    public void write(String path, CMIndividual ind) {
        File file = new File(path);
        FileOutputStream output;
        this.constraints = new ArrayList<>();
        this.transitions = new HashMap<>();
        this.places = new HashMap<>();
        this.arcs = new HashMap<>();
        this.directOutputs = new HashMap<>();
        this.directInputs = new HashMap<>();
        this.inputsLeft = new HashMap<>();
        this.outputsLeft = new HashMap<>();
        this.from = new HashMap<>();
        this.to = new HashMap<>();
        this.connectedPlacesInput = new HashMap<>();
        this.connectedPlacesOutput = new HashMap<>();
        this.silentTransitions = new ArrayList<>();
        this.indexPlace = 0;
        this.indexSilentTask = 0;
        try {
            file.createNewFile();
            output = new FileOutputStream(file);
            XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
            // iniciamos el documento
            Document doc = new Document();
            Element root = new Element("pnml");
            Element net = new Element(NET);
            net.setAttribute(ID, "net1");
            net.setAttribute(TYPE, "http://www.pnml.org/version-2009/grammar/pnmlcoremodel");
            final int numOfTasks = ind.getNumOfTasks();
            for (int indexTask = 0; indexTask < numOfTasks; indexTask++) {
                CMTask actualTask = ind.getTask(indexTask);
                Transition actualTaskTransition = new Transition(actualTask.getTask().getId(), Transition.FINAL);
                transitions.put(actualTask.getTask().getId(), actualTaskTransition);
                CMSet outputs = actualTask.getOutputs();
                generateOutputs(outputs, actualTaskTransition, numOfTasks, ind, actualTask.getTask().getId(), indexTask);
                CMSet inputs = actualTask.getInputs();
                generateInputs(inputs, actualTaskTransition, numOfTasks, ind, actualTask.getTask().getId(), indexTask);
            }

            connectNew();
            removeEqualPlaces();
            filterArcs();

            net.addContent(writeName());
            Element child = new Element("page").setAttribute("id", "n0");

            child.addContent(new Element("name").addContent(new Element("text")));

            // ESCRIBIMOS LAS TRANSICIONES
            Collection<Transition> transitionValues = transitions.values();
            for (final Transition task : transitionValues) {
                child.addContent(writeTransition(task));
            }

            ///ESCRIBIMOS LAS PLAZAS
            Collection<Place> placesValues = places.values();
            for (final Place place : placesValues) {
                child.addContent(writePlace(place));
            }

            /// ESCRIBIMOS LOS ARCOS
            Collection<Arc> arcValues = arcs.values();
            for (final Arc arc : arcValues) {
                child.addContent(writeArc(arc));
            }

            net.addContent(child);
            root.addContent(net);
            doc.setRootElement(root);
            outputter.output(doc, output);
            output.close();

        } catch (IOException ex) {
        }
    }

    private void generateOutputs(CMSet outputs, final Transition task, final int numOfTasks, final CMIndividual ind, final String idTask, int matrixTask) {
        final int outputsSize = outputs.size();
        if (outputsSize != 0) {
            generateNormalOutputs(outputs, task, numOfTasks, ind, idTask, matrixTask, outputsSize);
        } else if (outputs.isEmpty()) {
            generateEndOutputs(task, idTask);
        }
    }

    private void generateEndOutputs(Transition task, String idTask) {
        Place place = places.get(FINAL_PLACE);
        if (place == null) {
            place = new Place(FINAL_PLACE);
            places.put(place.getName(), place);
        }
        String placeName = place.getName();
        addToHashMap(placeName, from, idTask);
        addToHashMap(placeName, directInputs, idTask);
        addToHashMap(placeName, to, "end");
        this.places.put(place.getName(), place);
        createArc(task, place);
        increaseConnection(placeName, connectedPlacesInput, 1);
    }

    private void generateNormalOutputs(CMSet outputs, Transition task, int numOfTasks, CMIndividual ind, String idTask, int matrixTask, int outputsSize) {
        Integer newIndexPlace = indexPlace++;
        if (setWithRepetitions(outputs, numOfTasks)) {
            outputsSetWithRepetitions(outputs, task, ind, idTask, matrixTask, outputsSize, newIndexPlace);
        } else {
            outputsSetWithoutRepetitions(outputs, task, ind, idTask, outputsSize, newIndexPlace);
        }
    }

    private void outputsSetWithoutRepetitions(CMSet outputs, Transition task, CMIndividual ind, String idTask, int outputsSize, Integer newIndexPlace) {
        //ADD silent task
        Place place = new Place(newIndexPlace.toString(), Place.FINAL);
        String placeName = place.getName();
        addToHashMap(placeName, from, idTask);
        addToHashMap(placeName, directInputs, idTask);
        createArc(task, place);
        increaseConnection(placeName, connectedPlacesInput, 1);

        String nameSilent = SILENT_TASK_PROM + " " + indexSilentTask++;
        Transition silentTask = new Transition(nameSilent);
        this.silentTransitions.add(nameSilent);
        transitions.put(nameSilent, silentTask);
        createArc(place, silentTask);
        increaseConnection(placeName, connectedPlacesOutput, 1);
        newIndexPlace = newIndexPlace++;

        ////////////////////

        HashSet<String> placesConstraint = new HashSet<>();
        for (int indexSet = 0; indexSet < outputsSize; indexSet++) {
            place = new Place(newIndexPlace.toString(), Place.FINAL);
            placeName = place.getName();
            addToHashMap(placeName, from, nameSilent);
            addToHashMap(placeName, directInputs, nameSilent);
            createArc(silentTask, place);
            increaseConnection(placeName, connectedPlacesInput, 1);
            for (TIntIterator it = outputs.get(indexSet).iterator(); it.hasNext(); ) {
                int elem = it.next();
                String idOtherTask = ind.getTask(elem).getTask().getId();
                addToHashMap(placeName, to, idOtherTask);
                addToHashMap(placeName, outputsLeft, idOtherTask);
            }
            places.put(placeName, place);
            placesConstraint.add(placeName);
        }
        if (outputsSize > 1) {
            constraints.add(new HashSet<>(placesConstraint));
        }
    }

    private void outputsSetWithRepetitions(CMSet outputs, Transition task, CMIndividual ind, String idTask, int matrixTask, int outputsSize, Integer newIndexPlace) {
        Place place = new Place(newIndexPlace.toString(), Place.FINAL);
        String placeName = place.getName();
        addToHashMap(placeName, from, idTask);
        addToHashMap(placeName, directInputs, idTask);
        places.put(place.getName(), place);
        createArc(task, place);
        increaseConnection(placeName, connectedPlacesInput, 1);
        if (outputsSize > 1) {
            moreThanOneOutput(outputs, ind, idTask, matrixTask, newIndexPlace, place, placeName);
        } else {
            final TIntHashSet subset = outputs.get(0);
            for (TIntIterator it = subset.iterator(); it.hasNext(); ) {
                int elem = it.next();
                String idOtherTask = ind.getTask(elem).getTask().getId();
                addToHashMap(placeName, to, idOtherTask);
                addToHashMap(placeName, outputsLeft, idOtherTask);
            }
        }
    }

    private void moreThanOneOutput(CMSet outputs, CMIndividual ind, String idTask, int matrixTask, Integer newIndexPlace, Place place, String placeName) {
        CMSet combinations = permutator.doPermute(outputs);
        final int combSize = combinations.size();
        HashMap<String, Place> silentPlaces = new HashMap<>();
        for (int indexComb = 0; indexComb < combSize; indexComb++) {
            if (isOutputUsed(ind.getCombUsage(), combinations.get(indexComb), matrixTask)) {
                if (combinations.get(indexComb).size() > 1) {
                    addSilentTask(ind, idTask, newIndexPlace, place, placeName, combinations, silentPlaces, indexComb, true);
                } else {
                    for (TIntIterator it = combinations.get(indexComb).iterator(); it.hasNext(); ) {
                        int elem = it.next();
                        String idOtherTask = ind.getTask(elem).getTask().getId();
                        addToHashMap(placeName, to, idOtherTask);
                        addToHashMap(placeName, outputsLeft, idOtherTask);
                    }
                }
            }
        }
        for (Place placeIterator : silentPlaces.values()) {
            places.put(placeIterator.getName(), placeIterator);
        }
    }

    private void addSilentTask(CMIndividual ind, String idTask, Integer newIndexPlace, Place place, String placeName, CMSet combinations, HashMap<String, Place> silentPlaces, int indexComb, boolean output) {
        String nameSilent = SILENT_TASK_PROM + " " + indexSilentTask++;
        Transition silentTask = new Transition(nameSilent);
        this.silentTransitions.add(nameSilent);
        transitions.put(nameSilent, silentTask);
        createArc(place, silentTask);
        increaseConnection(placeName, connectedPlacesOutput, 1);
        HashSet<String> placesConstraint = new HashSet<>();
        for (TIntIterator it = combinations.get(indexComb).iterator(); it.hasNext(); ) {
            int elem = it.next();
            String idOtherTask = ind.getTask(elem).getTask().getId();
            Place auxPlace = silentPlaces.get(idOtherTask);
            if (auxPlace == null && output) {
                auxPlace = new Place(newIndexPlace.toString(), Place.FINAL);
            } else if (auxPlace == null && !output) {
                auxPlace = new Place(newIndexPlace.toString(), Place.INITIAL);
            }
            String placeNameOut = auxPlace.getName();
            addToHashMap(placeName, to, idOtherTask);
            addToHashMap(placeName, directOutputs, nameSilent);
            addToHashMap(placeNameOut, to, idOtherTask);
            addToHashMap(placeNameOut, from, idTask);
            addToHashMap(placeNameOut, directInputs, nameSilent);
            addToHashMap(placeNameOut, outputsLeft, idOtherTask);
            createArc(silentTask, auxPlace);
            increaseConnection(placeNameOut, connectedPlacesInput, 1);
            silentPlaces.put(idOtherTask, auxPlace);
            placesConstraint.add(placeNameOut);
        }
        constraints.add(new HashSet<>(placesConstraint));
    }

    private void generateInputs(CMSet inputs, final Transition task, final int numOfTasks, final CMIndividual ind, final String idTask, int matrixTask) {
        final int inputsSize = inputs.size();
        if (inputsSize != 0) {
            generateNormalInputs(inputs, task, numOfTasks, ind, idTask, matrixTask, inputsSize);
        } else if (inputs.isEmpty()) {
            generateStartInputs(task, idTask);
        }
    }

    private void generateStartInputs(Transition task, String idTask) {
        Place place = places.get(INITIAL_PLACE);
        if (place == null) {
            place = new Place(INITIAL_PLACE);
            places.put(place.getName(), place);
        }
        String placeName = place.getName();
        addToHashMap(placeName, to, idTask);
        addToHashMap(placeName, directOutputs, idTask);
        addToHashMap(placeName, from, "start");
        this.places.put(place.getName(), place);
        createArc(place, task);
        increaseConnection(placeName, connectedPlacesOutput, 1);
    }

    private void generateNormalInputs(CMSet inputs, Transition task, int numOfTasks, CMIndividual ind, String idTask, int matrixTask, int inputsSize) {
        Integer newIndexPlace = indexPlace++;
        if (setWithRepetitions(inputs, numOfTasks)) {
            inputsSetWithRepetitions(inputs, task, ind, idTask, matrixTask, inputsSize, newIndexPlace);
        } else {
            inputsSetWithoutRepetitions(inputs, task, ind, idTask, inputsSize, newIndexPlace);
        }
    }

    private void inputsSetWithoutRepetitions(CMSet inputs, Transition task, CMIndividual ind, String idTask, int inputsSize, Integer newIndexPlace) {
        HashSet<String> placesConstraint = new HashSet<>();
        for (int indexSet = 0; indexSet < inputsSize; indexSet++) {
            Place place = new Place(newIndexPlace.toString(), Place.INITIAL);
            String placeName = place.getName();
            addToHashMap(placeName, to, idTask);
            addToHashMap(placeName, directOutputs, idTask);
            createArc(place, task);
            increaseConnection(placeName, connectedPlacesOutput, 1);
            for (TIntIterator it = inputs.get(indexSet).iterator(); it.hasNext(); ) {
                int elem = it.next();
                String idOtherTask = ind.getTask(elem).getTask().getId();
                addToHashMap(placeName, from, idOtherTask);
                addToHashMap(placeName, inputsLeft, idOtherTask);
            }
            places.put(place.getName(), place);
            placesConstraint.add(placeName);
        }
        if (inputsSize > 1) {
            constraints.add(new HashSet<>(placesConstraint));
        }
    }

    private void inputsSetWithRepetitions(CMSet inputs, Transition task, CMIndividual ind, String idTask, int matrixTask, int inputsSize, Integer newIndexPlace) {
        Place place = new Place(newIndexPlace.toString(), Place.INITIAL);
        String placeName = place.getName();
        addToHashMap(placeName, to, idTask);
        addToHashMap(placeName, directOutputs, idTask);
        places.put(place.getName(), place);
        createArc(place, task);
        increaseConnection(placeName, connectedPlacesOutput, 1);
        if (inputsSize > 1) {
            moreThanOneInput(inputs, ind, idTask, matrixTask, newIndexPlace, place, placeName);
        } else {
            final TIntHashSet subset = inputs.get(0);
            for (TIntIterator it = subset.iterator(); it.hasNext(); ) {
                int elem = it.next();
                String idOtherTask = ind.getTask(elem).getTask().getId();
                addToHashMap(placeName, from, idOtherTask);
                addToHashMap(placeName, inputsLeft, idOtherTask);
            }
        }
    }

    private void moreThanOneInput(CMSet inputs, CMIndividual ind, String idTask, int matrixTask, Integer newIndexPlace, Place place, String placeName) {
        CMSet combinations = permutator.doPermute(inputs);
        final int combSize = combinations.size();
        HashMap<String, Place> silentPlaces = new HashMap<>();
        for (int indexComb = 0; indexComb < combSize; indexComb++) {
            if (isInputUsed(ind.getCombUsage(), combinations.get(indexComb), matrixTask)) {
                if (combinations.get(indexComb).size() > 1) {
                    addSilentTask(ind, idTask, newIndexPlace, place, placeName, combinations, silentPlaces, indexComb, false);
                } else {
                    for (TIntIterator it = combinations.get(indexComb).iterator(); it.hasNext(); ) {
                        int elem = it.next();
                        String idOtherTask = ind.getTask(elem).getTask().getId();
                        addToHashMap(placeName, from, idOtherTask);
                        addToHashMap(placeName, inputsLeft, idOtherTask);
                    }
                }
            }
        }
        for (Place placeIterator : silentPlaces.values()) {
            places.put(placeIterator.getName(), placeIterator);
        }
    }

    private void removeEqualPlaces() {
        while (true) {
            Collection<Place> listPlaces = new ArrayList<>(places.values());
            boolean flag = true;
            for (Place placeA : listPlaces) {
                if (checkEquality(placeA)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                break;
            }
        }
    }

    private boolean checkEquality(Place placeA) {
        String namePA = placeA.getName();
        Collection<Place> listPlaces = new ArrayList<>(places.values());
        for (Place placeB : listPlaces) {
            String namePB = placeB.getName();
            if (!namePB.equals(namePA)) {
                if (to.get(namePA).equals(to.get(namePB))
                        && from.get(namePA).equals(from.get(namePB))
                        && directOutputs.get(namePA).equals(directInputs.get(namePB))) {
                    Set<String> directOutputsB = directOutputs.get(namePB);
                    deleteRelatedArcs(namePB);
                    for (String transitionB : directOutputsB) {
                        createArc(placeA, transitions.get(transitionB));
                        directOutputs.get(namePA).add(transitionB);
                    }
                    Set<String> directInputsB = directInputs.get(namePB);
                    for (String transitionB : directInputsB) {
                        directOutputs.get(namePA).remove(transitionB);
                        transitions.remove(transitionB);
                        deleteRelatedArcs(transitionB);
                    }

                    places.remove(namePB);
                    return true;
                } else if (to.get(namePA).equals(to.get(namePB)) &&
                        !isConstraint(namePB) && !isConstraint(namePA)) {

                    Set<String> fromA = from.get(namePA);
                    fromA.addAll(from.get(namePB));
                    from.put(namePA, fromA);
                    if (directOutputs.get(namePB) != null)
                        for (String string : directOutputs.get(namePB)) {
                            addToHashMap(namePA, directOutputs, string);
                            createArc(placeA, transitions.get(string));
                        }
                    if (directInputs.get(namePB) != null)
                        for (String string : directInputs.get(namePB)) {
                            addToHashMap(namePA, directInputs, string);
                            createArc(transitions.get(string), placeA);
                        }
                    directOutputs.remove(namePB);
                    directInputs.remove(namePB);
                    from.remove(namePB);
                    to.remove(namePB);
                    deleteRelatedArcs(namePB);
                    places.remove(namePB);
                    replaceConstraint(namePB, namePA);
                    return true;
                } else if (from.get(namePA).equals(from.get(namePB)) &&
                        !isConstraint(namePB) && !isConstraint(namePA)) {

                    Set<String> toA = to.get(namePA);
                    toA.addAll(to.get(namePB));
                    to.put(namePA, toA);
                    Set<String> fromA = from.get(namePA);
                    fromA.addAll(from.get(namePB));
                    from.put(namePA, fromA);
                    if (directOutputs.get(namePB) != null)
                        for (String string : directOutputs.get(namePB)) {
                            addToHashMap(namePA, directOutputs, string);
                            createArc(placeA, transitions.get(string));
                        }
                    if (directInputs.get(namePB) != null)
                        for (String string : directInputs.get(namePB)) {
                            addToHashMap(namePA, directInputs, string);
                            createArc(transitions.get(string), placeA);
                        }
                    directOutputs.remove(namePB);
                    directInputs.remove(namePB);
                    from.remove(namePB);
                    to.remove(namePB);
                    deleteRelatedArcs(namePB);
                    places.remove(namePB);
                    replaceConstraint(namePB, namePA);
                    return true;
                }
            }
        }
        return false;
    }

    private void connectNew() {
        while (true) {
            Collection<Place> listPlaces = new ArrayList<>(places.values());
            boolean flag = true;
            for (Place placeA : listPlaces) {
                String namePA = placeA.getName();
                Set<String> placeATo = to.get(namePA);
                Integer numConectionsTo = connectedPlacesOutput.get(namePA);
                if (numConectionsTo == null) {
                    numConectionsTo = 0;
                }
                if (placeATo != null && numConectionsTo < placeATo.size()) {
                    if (checkConnection(placeA)) {
                        flag = false;
                        break;
                    }
                }
            }
            if (flag) {
                break;
            }
        }
    }

    private boolean checkConnection(Place placeA) {
        String namePA = placeA.getName();
        ArrayList<Place> listPlaces = new ArrayList<>(places.values());
        Set<String> placeATo = to.get(namePA);
        Set<String> placeAFrom = from.get(namePA);
        boolean flag = false;
        for (String elemTo : placeATo) {
            for (int i = 0; i < listPlaces.size(); i++) {
                Place placeB = listPlaces.get(i);
                String namePB = placeB.getName();
                if (!namePB.equals(namePA)) {
                    Set<String> placeBFrom = from.get(namePB);
                    Set<String> placeBTo = to.get(namePB);
                    Integer numConectionsFrom = connectedPlacesInput.get(namePB);
                    if (numConectionsFrom == null) {
                        numConectionsFrom = 0;
                    }
                    if (placeBFrom != null
                            && placeBTo.contains(elemTo)
                            && getSharedElements(placeAFrom, placeBFrom).size() > 0
                            && numConectionsFrom < placeBFrom.size()
                            && getSharedElements(inputsLeft.get(namePB), placeAFrom).size() > 0
                            && getSharedElements(outputsLeft.get(namePA), placeBTo).size() > 0
                            && to.get(namePA).equals(to.get(namePB))) {
                        Set<String> toA = to.get(namePA);
                        toA.addAll(to.get(namePB));
                        to.put(namePA, toA);
                        Set<String> fromA = from.get(namePA);
                        fromA.addAll(from.get(namePB));
                        from.put(namePA, fromA);
                        if (directOutputs.get(namePB) != null) {
                            for (String string : directOutputs.get(namePB)) {
                                addToHashMap(namePA, directOutputs, string);
                                createArc(placeA, transitions.get(string));
                            }
                        }
                        if (directInputs.get(namePB) != null) {
                            for (String string : directInputs.get(namePB)) {
                                addToHashMap(namePA, directInputs, string);
                                createArc(transitions.get(string), placeA);
                            }
                        }
                        if (outputsLeft.get(namePB) != null) {
                            for (String string : outputsLeft.get(namePB)) {
                                addToHashMap(namePA, outputsLeft, string);
                            }
                        }
                        if (inputsLeft.get(namePB) != null) {
                            for (String string : inputsLeft.get(namePB)) {
                                addToHashMap(namePA, inputsLeft, string);
                            }
                        }
                        directOutputs.remove(namePB);
                        directInputs.remove(namePB);
                        from.remove(namePB);
                        to.remove(namePB);
                        deleteRelatedArcs(namePB);
                        places.remove(namePB);
                        replaceConstraint(namePB, namePA);
                        placeA.setType(Place.INITIAL);
                        Integer amount = connectedPlacesInput.get(namePB);
                        if (amount == null) {
                            amount = 0;
                        }
                        increaseConnection(namePA, connectedPlacesInput, amount);
                        amount = connectedPlacesOutput.get(namePB);
                        if (amount == null) {
                            amount = 0;
                        }
                        increaseConnection(namePA, connectedPlacesOutput, amount);
                        flag = true;
                    } else if (placeBFrom != null
                            && placeBTo.contains(elemTo)
                            && getSharedElements(placeAFrom, placeBFrom).size() > 0
                            && numConectionsFrom < placeBFrom.size()
                            && getSharedElements(inputsLeft.get(namePB), placeAFrom).size() > 0
                            && getSharedElements(outputsLeft.get(namePA), placeBTo).size() > 0
                            && from.get(namePA).equals(from.get(namePB))) {
                        Set<String> toA = to.get(namePA);
                        toA.addAll(to.get(namePB));
                        to.put(namePA, toA);
                        Set<String> fromA = from.get(namePA);
                        fromA.addAll(from.get(namePB));
                        from.put(namePA, fromA);
                        if (directOutputs.get(namePB) != null) {
                            for (String string : directOutputs.get(namePB)) {
                                addToHashMap(namePA, directOutputs, string);
                                createArc(placeA, transitions.get(string));
                            }
                        }
                        if (directInputs.get(namePB) != null) {
                            for (String string : directInputs.get(namePB)) {
                                addToHashMap(namePA, directInputs, string);
                                createArc(transitions.get(string), placeA);
                            }
                        }
                        if (outputsLeft.get(namePB) != null) {
                            for (String string : outputsLeft.get(namePB)) {
                                addToHashMap(namePA, outputsLeft, string);
                            }
                        }
                        if (inputsLeft.get(namePB) != null) {
                            for (String string : inputsLeft.get(namePB)) {
                                addToHashMap(namePA, inputsLeft, string);
                            }
                        }
                        directOutputs.remove(namePB);
                        directInputs.remove(namePB);
                        from.remove(namePB);
                        to.remove(namePB);
                        deleteRelatedArcs(namePB);
                        places.remove(namePB);
                        replaceConstraint(namePB, namePA);
                        placeA.setType(Place.INITIAL);
                        Integer amount = connectedPlacesInput.get(namePB);
                        if (amount == null) {
                            amount = 0;
                        }
                        increaseConnection(namePA, connectedPlacesInput, amount);
                        amount = connectedPlacesOutput.get(namePB);
                        if (amount == null) {
                            amount = 0;
                        }
                        increaseConnection(namePA, connectedPlacesOutput, amount);
                        flag = true;
                    } else if (placeBFrom != null
                            && placeBTo.contains(elemTo)
                            && getSharedElements(placeAFrom, placeBFrom).size() > 0
                            && numConectionsFrom < placeBFrom.size()
                            && getSharedElements(inputsLeft.get(namePB), placeAFrom).size() > 0
                            && getSharedElements(outputsLeft.get(namePA), placeBTo).size() > 0) {
                        String nameSilent = SILENT_TASK_PROM + " " + indexSilentTask++;
                        Transition silentTask = new Transition(nameSilent);
                        transitions.put(nameSilent, silentTask);
                        this.silentTransitions.add(nameSilent);
                        createArc(placeA, silentTask);
                        createArc(silentTask, placeB);
                        addToHashMap(namePB, directInputs, nameSilent);
                        addToHashMap(namePA, directOutputs, nameSilent);
                        increaseConnection(namePB, connectedPlacesInput, 1);
                        increaseConnection(namePA, connectedPlacesOutput, 1);

                    }
                }
            }
        }
        return flag;
    }

    private void filterArcs() {
        HashMap<String, Set<String>> silentTaskInputs = new HashMap<>();
        HashMap<String, Set<String>> silentTaskOutputs = new HashMap<>();
        for (Arc actualArc : arcs.values()) {
            String nameA = actualArc.getA().getName();
            String nameB = actualArc.getB().getName();
            if (nameA.contains(SILENT_TASK_PROM)) {
                addToHashMap(nameA, silentTaskOutputs, nameB);
            }
            if (nameB.contains(SILENT_TASK_PROM)) {
                addToHashMap(nameB, silentTaskInputs, nameA);
            }
        }
        for (int i = silentTransitions.size() - 1; i >= 0; i--) {
            String transitionName = silentTransitions.get(i);
            Set<String> inputs = silentTaskInputs.get(transitionName);
            Set<String> outputs = silentTaskOutputs.get(transitionName);
            if ((inputs == null || outputs == null) || inputs.equals(outputs)) {
                silentTaskInputs.remove(transitionName);
                silentTaskOutputs.remove(transitionName);
                deleteRelatedArcs(transitionName);
                transitions.remove(transitionName);
                silentTransitions.remove(i);
            } else {
                for (int j = silentTransitions.size() - 1; j >= 0; j--) {

                    String transitionNameB = silentTransitions.get(j);
                    Set<String> inputsB = silentTaskInputs.get(transitionNameB);
                    Set<String> outputsB = silentTaskOutputs.get(transitionNameB);
                    if (!transitionName.equalsIgnoreCase(transitionNameB) &&
                            inputsB.equals(inputs) && outputsB.equals(outputs)) {
                        silentTaskInputs.remove(transitionName);
                        silentTaskOutputs.remove(transitionName);
                        deleteRelatedArcs(transitionName);
                        transitions.remove(transitionName);
                        silentTransitions.remove(i);
                        break;
                    }
                }
            }
        }
    }

    private void deleteRelatedArcs(String nameNode) {
        Iterator it = arcs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            Arc arc = (Arc) pairs.getValue();
            if (arc.getA().getName().equals(nameNode)
                    || arc.getB().getName().equals(nameNode)) {
                it.remove();
            } // avoids a ConcurrentModificationException
        }
    }

    private boolean isConstraint(String name) {
        for (int index = 0; index < constraints.size(); index++) {
            if (constraints.get(index).contains(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean replaceConstraint(String name, String toAdd) {
        for (int index = 0; index < constraints.size(); index++) {
            if (constraints.get(index).contains(name)) {
                constraints.get(index).remove(name);
                constraints.get(index).add(toAdd);
            }
        }
        return false;
    }

    private Set<String> getSharedElements(Set<String> a, Set<String> b) {
        if (a != null) {
            Set<String> shared = new HashSet<>(a);
            if (b != null) {
                shared.retainAll(b);
                return shared;
            }
        }
        return new HashSet<>();
    }

    private void increaseConnection(String place, HashMap<String, Integer> hashamp, int amount) {
        Integer a = hashamp.get(place);
        if (a == null) {
            a = 0;
        }
        a += amount;
        hashamp.put(place, a);
    }

    private void addToHashMap(String task, HashMap<String, Set<String>> hashamp, String taskToAdd) {
        Set<String> set = hashamp.get(task);
        if (set == null) {
            set = new HashSet<>();
        }
        set.add(taskToAdd);
        hashamp.put(task, set);
    }

    private void createArc(Node source, Node target) {
        Arc arc = new Arc(source, target);
        String name = source.getName() + target.getName();
        this.arcs.put(name, arc);
    }

    private boolean isInputUsed(IndividualCombUsage combUsage, TIntHashSet subset, int elem) {
        if (combUsage != null) {
            return combUsage.getCombinationsUsage().get(elem).isInputUsed(subset);
        } else {
            return true;
        }
    }

    private boolean isOutputUsed(IndividualCombUsage combUsage, TIntHashSet subset, int elem) {
        if (combUsage != null) {
            return combUsage.getCombinationsUsage().get(elem).isOutputUsed(subset);
        } else {
            return true;
        }
    }

    private boolean setWithRepetitions(CMSet set, int numOfTasks) {
        int[] elementsRepetitions = new int[numOfTasks];
        for (TIntHashSet subset : set) {
            for (TIntIterator it = subset.iterator(); it.hasNext(); ) {
                int elem = it.next();
                elementsRepetitions[elem]++;
                if (elementsRepetitions[elem] > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private Element writeTransition(Transition task) {
        String taskName = task.getName();
        Element transition = new Element(TRANSITION);
        transition.setAttribute(ID, taskName);
        transition.addContent(new Element("name").addContent(new Element("text").setText(taskName)));
        // identificamos si es una silentTask o no
        if (taskName.contains(SILENT_TASK_PROM)) {
            transition.addContent(writeInfoSilentTask(SILENT_TASK_PROM));
        } else {
            transition.addContent(writeInfoSilentTask(taskName));
        }
        return transition;
    }

    private Element writeInfoSilentTask(String activityName) {
        Element silentTaskInfo = new Element(TOOL_SPECIFIC);
        silentTaskInfo.setAttribute("tool", TOOL_SPECIFIC_NAME);
        silentTaskInfo.setAttribute("version", "6.4");
        silentTaskInfo.setAttribute("activity", activityName);
        return silentTaskInfo;
    }

    private Element writePlace(Place place) {
        String idPlace = place.getName();
        Element placeElement = new Element(PLACE);
        placeElement.setAttribute(ID, idPlace);
        placeElement.addContent(new Element("name").addContent(new Element("text").setText(idPlace)));
        if (INITIAL_PLACE.equals(idPlace)) {
            placeElement.addContent(new Element("initialMarking").addContent(new Element("text").setText("1")));
        }
        return placeElement;
    }

    private Element writeArc(Arc arc) {
        Element arcElement = new Element(ARC);
        arcElement.setAttribute(ID, arc.getName());
        arcElement.setAttribute(SOURCE, arc.getA().getName());
        arcElement.setAttribute(TARGET, arc.getB().getName());
        arcElement.addContent(new Element("name").addContent(new Element("text").setText("1")));
        arcElement.addContent(new Element("arctype").addContent(new Element("text").setText("normal")));
        return arcElement;
    }

    private Element writeName() {
        Element placeElement = new Element("name");
        placeElement.addContent(new Element("text").setText("process"));
        return placeElement;
    }
}