package controllers;

import actors.WsSubscriberActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.google.inject.name.Named;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import javax.inject.Inject;

public class HomeController extends Controller {

    private final ActorSystem actorSystem;
    private final ActorRef sceneActor;
    private final Materializer mat;

    @Inject
    public HomeController(ActorSystem actorSystem,
                          Materializer mat,
                          @Named("scene") ActorRef sceneActor) {
        this.actorSystem = actorSystem;
        this.mat = mat;
        this.sceneActor = sceneActor;
    }

    public Result index() {
        return ok(views.html.index.render());
    }

    public WebSocket subscribe() {
        return WebSocket.Json.accept(
          request -> ActorFlow.actorRef(
                                      out -> WsSubscriberActor.props(sceneActor, out),
                                      actorSystem,
                                      mat)
        );
    }

/*    public WebSocket subscribe(String username) {
        return WebSocket.Json.acceptOrResult(
          request -> {
              CompletionStage<F.Either<Result, Flow<JsonNode, JsonNode, ?>>> future = userRepo.getByUsername(username).thenApplyAsync(
                      userOpt -> {
                          if (userOpt.isPresent()) {
                              return F.Either.Right(ActorFlow.actorRef(
                                      out -> WsSubscriberActor.props(sceneActor, out, userOpt.get()),
                                      actorSystem,
                                      mat));
                          } else {
                              return F.Either.Left(forbidden());
                          }
                      });
              return future;
          }
        );
    }*/

}
            
