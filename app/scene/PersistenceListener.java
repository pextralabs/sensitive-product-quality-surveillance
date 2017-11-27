package scene;

import akka.actor.ActorRef;
import org.kie.api.event.rule.DefaultRuleRuntimeEventListener;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;

import java.util.Set;

public class PersistenceListener extends DefaultRuleRuntimeEventListener {

    ActorRef owner;
    Set<ActorRef> subscribers;

    public PersistenceListener(ActorRef owner, Set<ActorRef> subscribers) {
        this.owner = owner;
        this.subscribers = subscribers;
    }

    @Override
    public void objectInserted(ObjectInsertedEvent event) {
        Object in = event.getObject();
        if (in instanceof br.ufes.inf.lprm.situation.model.Situation) {
            subscribers.forEach(
                    subscriber -> subscriber.tell(in, owner)
            );
        }
    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {
        Object in = event.getObject();
        if (in instanceof br.ufes.inf.lprm.situation.model.Situation) {
            subscribers.forEach(
                    subscriber -> subscriber.tell(in, owner)
            );
        }
    }

}
