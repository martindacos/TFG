package es.usc.citius.aligments.hipster;


import es.usc.citius.hipster.model.AbstractNode;
import es.usc.citius.hipster.model.HeuristicNode;

public class WeightedNodeMine<A, S, C extends Comparable<C>> extends AbstractNode<A, S, WeightedNodeMine<A, S, C>> implements HeuristicNode<A, S, C, WeightedNodeMine<A, S, C>> {
    private C cost;
    private C estimation;
    private C score;

    public WeightedNodeMine(WeightedNodeMine<A, S, C> previousNode, S state, A action, C cost, C estimation, C score) {
        super(previousNode, state, action);
        this.cost = cost;
        this.estimation = estimation;
        this.score = score;
    }

    @Override
    public int compareTo(WeightedNodeMine<A, S, C> o) {
        int compare = this.score.compareTo(o.getScore());
        if (compare == 0) {
            //We want the biggest cost so exchange vars (more cost explore first so less queuedCost)
            compare = o.getCost().compareTo(this.cost);
        }
        return compare;
    }

    public C getScore() { return this.score; }

    public C getEstimation() {
        return this.estimation;
    }

    public C getCost() {
        return this.cost;
    }
}
