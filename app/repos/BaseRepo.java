package repos;

import db.DatabaseExecutionContext;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.Persistent;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class BaseRepo {

    protected final EbeanServer ebean;
    protected final DatabaseExecutionContext executionContext;

    @Inject
    public BaseRepo(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebean = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    public CompletionStage<Optional<? extends Persistent>> getById(Class<? extends Persistent> clazz, Long oid) {
        return supplyAsync(() -> ebean.find(clazz).setId(oid).findOneOrEmpty(), executionContext);
    }

    public CompletionStage<? extends Persistent> save(Persistent obj) {
        return supplyAsync(() -> {
            ebean.save(obj);
            return obj;
        }, executionContext);
    }

}
