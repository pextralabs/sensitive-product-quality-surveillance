package repos;

import db.DatabaseExecutionContext;
import models.base.Entity;
import models.base.Sensor;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EntityRepo extends BaseRepo {

    @Inject
    public EntityRepo(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        super(ebeanConfig, executionContext);
    }

    public CompletionStage<List<Entity>> getEntities() {
        return supplyAsync(() -> ebean.find(Entity.class).findList(), executionContext);
    }

    public CompletionStage<Optional<Sensor>> getSensorByEntity(Long entityId, Long sensorId) {
        return supplyAsync(() ->
                ebean.find(Sensor.class) .where().eq("bearer.id", entityId).eq("id", sensorId).findOneOrEmpty(),
                executionContext);
    }





}
