package models.scenario;

import br.ufes.inf.lprm.context.model.*;

public class Temperature extends IntrinsicContext<Reading<Double>> {
    public Temperature(String id, Entity bearer) {
        super(id, bearer);
    }

    @Override
    public String toString() {
        return bearer.toString() + " Temperature: " + getValue() + " degrees";
    }
}
