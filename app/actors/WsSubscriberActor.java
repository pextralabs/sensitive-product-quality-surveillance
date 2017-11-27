package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;

public class WsSubscriberActor extends AbstractActor {

    private ActorRef parent;
    private ActorRef out;

    public static Props props(ActorRef parent, ActorRef out) {
        return Props.create(WsSubscriberActor.class,  parent, out);
    }

    public WsSubscriberActor(ActorRef parent, ActorRef out) {
        this.parent = parent;
        this.out = out;
    }

    @Override
    public void preStart() {
        parent.tell(new Protocols.Subscribe() , self());
    }

    @Override
    public void postStop() {
        parent.tell(new Protocols.Unsubscribe() , self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JsonNode.class, message ->
                        out.tell(message, self())
                )
                .build();
    }

}
