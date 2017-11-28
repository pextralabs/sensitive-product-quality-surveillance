package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.runtime.rule.FactHandle;
import play.api.Environment;
import repos.EntityRepo;
import scene.SituationBroadcaster;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.drools.core.ClockType;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.springframework.cglib.proxy.Enhancer;
import play.Logger;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static actors.Protocols.Operation.Type.INSERT;

public class SceneActor extends AbstractActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private final Set<ActorRef> subscribers;
    private final Environment env;
    private final EntityRepo entityRepo;

    private  KieSession kSession;
    private  ExecutorService ex;

    private KieSession newSession(Environment env, String sceneId) {
        // Getting KieServices
        KieServices kServices = KieServices.Factory.get();
        // Instantiating a KieFileSystem and KieModuleModel
        KieFileSystem kFileSystem = kServices.newKieFileSystem();
        KieModuleModel kieModuleModel = kServices.newKieModuleModel();

        ReleaseId releaseId = kServices.newReleaseId("br.ufes.inf.lprm.scene", sceneId, "0.1.0");
        kFileSystem.generateAndWritePomXML(releaseId);

        KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel(sceneId);
        kieBaseModel.setEventProcessingMode(EventProcessingOption.STREAM);
        kieBaseModel.setEqualsBehavior(EqualityBehaviorOption.EQUALITY);
        kieBaseModel.addInclude("sceneKieBase");

        File userpath = env.getFile("conf/scene/");

        File[] files = userpath.listFiles(pathname -> pathname.getName().toLowerCase().endsWith(".drl"));

        if (files != null) {
            for (File file : files) {
                kFileSystem.write(
                        kServices.getResources().newFileSystemResource(file).setResourceType(ResourceType.DRL)
                );
            }
        }

        KieSessionModel kieSessionModel = kieBaseModel.newKieSessionModel(sceneId + ".session");
        kieSessionModel.setClockType(ClockTypeOption.get(ClockType.REALTIME_CLOCK.getId()))
                .setType(KieSessionModel.KieSessionType.STATEFUL);

        kFileSystem.writeKModuleXML(kieModuleModel.toXML());
        KieBuilder kbuilder = kServices.newKieBuilder(kFileSystem);
        ArrayList<Resource> dependencies = new ArrayList();
        try {
            Enumeration<URL> e = env.classLoader().getResources(KieModuleModelImpl.KMODULE_JAR_PATH);
            while ( e.hasMoreElements() ) {
                URL url = e.nextElement();
                String path;
                if (url.getPath().contains(".jar!")) {
                    path = url.getPath().replace("!/" + KieModuleModelImpl.KMODULE_JAR_PATH, "");
                    dependencies.add(kServices.getResources().newUrlResource(path));
                } else {
                    path = url.getPath().replace(KieModuleModelImpl.KMODULE_JAR_PATH, "");
                    dependencies.add(kServices.getResources().newFileSystemResource(path));
                }

            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        kbuilder.setDependencies(dependencies.toArray(new Resource[0]));
        kbuilder.buildAll();
        if (kbuilder.getResults().hasMessages()) {
            throw new IllegalArgumentException("Couldn't build knowledge module " + kbuilder.getResults());
        }

        KieModule kModule = kbuilder.getKieModule();
        KieContainer kContainer = kServices.newKieContainer(kModule.getReleaseId());

        KieSession kSession = kContainer.newKieSession(sceneId + ".session");
        kSession.addEventListener(new SituationBroadcaster(self(), subscribers, logger));
        //kSession.addEventListener(new SCENESessionListener());
        return kSession;

    }

    @Override
    public void preStart() {

        Logger.debug("Starting scene actor");

        kSession = newSession(env, "scene-actor");

        ClassPool classPool = new ClassPool();
        classPool.appendSystemPath();
        classPool.appendClassPath(new LoaderClassPath(Enhancer.class.getClassLoader()));
        classPool.appendClassPath(new LoaderClassPath(env.classLoader()));

        SceneApplication app = new SceneApplication(classPool, kSession, "scene-actor");

        entityRepo.getEntities().thenAccept(
            entities -> {
                self().tell(new Protocols.Operation(entities, INSERT), self() );
                entities.forEach(

                        entity -> self().tell(new Protocols.Operation(entity.getSensors(), INSERT), self() )
                );
            }
        );

        kSession.setGlobal("logger", logger);
        ex = Executors.newSingleThreadExecutor();

        ex.submit((Runnable) kSession::fireUntilHalt);
    }

    @Inject
    public SceneActor(Environment env, EntityRepo entityRepo) {
        this.env = env;
        this.entityRepo = entityRepo;

        this.subscribers = new HashSet<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Protocols.Operation.class, (Protocols.Operation operation) -> {
                    Collection collection;
                    if (operation.obj instanceof Collection) {
                        collection = (Collection) operation.obj;
                    } else {
                        collection = new ArrayList();
                        collection.add(operation.obj);
                    }
                    switch (operation.type) {
                        case INSERT:
                            kSession.submit(session -> collection.forEach(session::insert));
                            break;
                        case UPDATE:
                            kSession.submit(session -> collection.forEach(item -> {
                                FactHandle handle = session.getFactHandle(item);
                                if (handle == null) {
                                    logger.debug("fact handle for `{}` not found... inserting it", item);
                                    session.insert(item);
                                } else {
                                    session.update(handle, item);
                                }
                            }));
                            break;
                        case DELETE:
                            kSession.submit(session -> collection.forEach(item -> {
                                session.delete(session.getFactHandle(item));
                            }));
                    }
                } )
                .match(Protocols.Subscribe.class, subscribe -> {
                    logger.info("adding a new subscriber for `{}`", self().path());
                    subscribers.add(sender());
                })
                .match(Protocols.Unsubscribe.class, unsubscribe -> {
                    logger.info("removing subscriber for `{}`", self().path());
                    subscribers.remove(sender());
                })
                .build();
    }

}
