package db;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
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



    /*public CompletionStage<Optional<? extends PersistentModel>> getByOID(Class<? extends PersistentModel> clazz, Long oid) {
        return supplyAsync(() -> {
            return ebean.find(clazz).setId(oid).findOneOrEmpty();
        }, executionContext);
    }*/

}
