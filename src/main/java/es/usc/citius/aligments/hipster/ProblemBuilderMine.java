package es.usc.citius.aligments.hipster;

import es.usc.citius.hipster.model.Transition;
import es.usc.citius.hipster.model.function.*;
import es.usc.citius.hipster.model.function.impl.BinaryOperation;
import es.usc.citius.hipster.model.function.impl.LazyActionStateTransitionFunction;
import es.usc.citius.hipster.model.function.impl.LazyNodeExpander;
import es.usc.citius.hipster.model.problem.SearchProblem;

public class ProblemBuilderMine<A, S, C extends Comparable<C>>  {
    private es.usc.citius.hipster.model.function.HeuristicFunction<S, C> hf;
    private es.usc.citius.hipster.model.function.CostFunction<A, S, C> cf;
    private es.usc.citius.hipster.model.function.impl.BinaryOperation<C> costAlgebra;
    private final S initialState;
    private ActionStateTransitionFunction<A, S> atf;
    private ActionFunction<A, S> af;
    private final TransitionFunction<A, S> tf;

    public ProblemBuilderMine(HeuristicFunction<S, C> hf, CostFunction<A, S, C> cf, BinaryOperation<C> costAlgebra,
                              S initialState, ActionStateTransitionFunction<A, S> atf, ActionFunction<A, S> af) {
        this.hf = hf;
        this.cf = cf;
        this.costAlgebra = costAlgebra;
        this.initialState = initialState;
        this.atf = atf;
        this.af = af;
        tf = new LazyActionStateTransitionFunction(this.af, this.atf);
    }

    public SearchProblem<A, S, WeightedNodeMine<A, S, C>> build() {
        WeightedNodeFactoryMine<A, S, C> factory = new WeightedNodeFactoryMine(this.cf, this.hf, this.costAlgebra);
        WeightedNodeMine<A, S, C> initialNode = factory.makeNode(null, Transition.create(null, null, this.initialState));
        LazyNodeExpander<A, S, WeightedNodeMine<A, S, C>> nodeExpander = new LazyNodeExpander(this.tf, factory);
        return new SearchProblem(initialNode, nodeExpander);
    }
}
