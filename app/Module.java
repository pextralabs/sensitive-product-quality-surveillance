import actors.SceneActor;
import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;

public class Module extends AbstractModule implements AkkaGuiceSupport {
    @Override
    protected void configure() {
        bindActor(SceneActor.class, "scene");
    }
}


