package es.usc.citius.aligments.hipster;


import es.usc.citius.hipster.model.Transition;
import es.usc.citius.hipster.model.function.CostFunction;
import es.usc.citius.hipster.model.function.HeuristicFunction;
import es.usc.citius.hipster.model.function.NodeFactory;
import es.usc.citius.hipster.model.function.impl.BinaryOperation;

public class WeightedNodeFactoryMine<A, S, C extends Comparable<C>> implements NodeFactory<A, S, WeightedNodeMine<A, S, C>> {
    private CostFunction<A, S, C> gf;
    private HeuristicFunction<S, C> hf;
    private BinaryOperation<C> costAccumulator;

    public WeightedNodeFactoryMine(CostFunction<A, S, C> costFunction, HeuristicFunction<S, C> heuristicFunction, BinaryOperation<C> costAccumulator) {
        this.gf = costFunction;
        this.hf = heuristicFunction;
        this.costAccumulator = costAccumulator;
    }

    public WeightedNodeMine<A, S, C> makeNode(WeightedNodeMine<A, S, C> fromNode, Transition<A, S> transition) {
        C cost;
        if (fromNode == null) {
            cost = this.costAccumulator.getIdentityElem();
        } else {
            cost = this.costAccumulator.apply(fromNode.getCost(), this.gf.evaluate(transition));
        }

        C estimatedDistance = this.hf.estimate(transition.getState());
        C score = this.costAccumulator.apply(cost, estimatedDistance);
        return new WeightedNodeMine(fromNode, transition.getState(), transition.getAction(), cost, estimatedDistance, score);
    }
}