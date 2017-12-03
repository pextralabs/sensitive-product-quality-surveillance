package repos;

import db.DatabaseExecutionContext;
import io.ebean.ExpressionList;
import io.ebean.Query;
import models.sensoring.Sensor;
import models.sensoring.Stream;
import models.sensoring.StreamData;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class SensorRepo extends BaseRepo {

    @Inject
    public SensorRepo(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        super(ebeanConfig, executionContext);
    }

    public CompletionStage<List<Sensor>> getSensors() {
        return supplyAsync(() -> {

            List<Sensor> list = ebean.find(Sensor.class)
                                        .fetch("owner")
                                        .fetch("streams")
                                        .findList();

            list.forEach(
                sensor -> sensor.getStreams().forEach(
                    stream -> stream.setTotalSize(ebean.find(StreamData.class)
                                                    .where().eq("stream.oid", stream.getId())
                                                    .findCount())
                )
            );

            return list;

        } , executionContext);
    }

    public CompletionStage<Sensor> save(Sensor sensor) {
        return supplyAsync(() -> {
            ebean.save(sensor);
            return sensor;
        }, executionContext);
    }

    public CompletionStage<Optional<Sensor>> getSensorBy(Long sensorId) {
        return supplyAsync(() -> {
            return ebean.find(Sensor.class)
                    .fetch("owner")
                    .fetch("streams")
                    .where()
                    .eq("oid", sensorId)
                    .findOneOrEmpty().map(
                            sensor -> {
                                sensor.getStreams().forEach(
                                        stream -> stream.setTotalSize(ebean.find(StreamData.class)
                                                .where().eq("stream.oid", stream.getId())
                                                .findCount())
                                );
                                return sensor;
                            }
                    );
        } , executionContext);
    }

    public CompletionStage<Optional<Stream>> getStreamBy(Long sensorId, Long streamId) {
        return getStreamBy(sensorId, streamId, 0);
    }

    public CompletionStage<Optional<Stream>> getStreamBy(Long sensorId, Long streamId, int maxData) {
        return supplyAsync(() -> {

            Optional<Stream> streamOpt = ebean.find(Stream.class)
                                        .fetch("sensor")
                                        .where()
                                        .eq("sensor.oid", sensorId)
                                        .eq("oid", streamId)
                                        .findOneOrEmpty().map(
                                            stream -> stream.setTotalSize(ebean.find(StreamData.class)
                                                    .where().eq("stream.oid", stream.getId())
                                                    .findCount()));

            return  maxData > 0 ? streamOpt.map(
                        stream -> ((Stream) stream).loadData(
                                    ebean.find(StreamData.class)
                                            .fetch("values")
                                            .where().eq("stream.oid", ((Stream) stream).getId())
                                            .orderBy("serverTimestamp desc").setMaxRows(maxData)
                                            .findList()))
                        : streamOpt;

        } , executionContext);
    }

    public CompletionStage<Optional<Stream>> getStreamBy(String sensorKey, String streamKey) {
        return getStreamBy(sensorKey, streamKey, 0);
    }

    public CompletionStage<Optional<Stream>> getStreamBy(String sensorKey, String streamKey, int maxData) {
        return supplyAsync(() -> {
            Optional<Stream> streamOpt = ebean.find(Stream.class)
                    .fetch("sensor")
                    .where()
                    .eq("sensor.key", sensorKey)
                    .eq("key", streamKey)
                    .findOneOrEmpty().map(
                            stream -> stream.setTotalSize(ebean.find(StreamData.class)
                                    .where().eq("stream.oid", stream.getId())
                                    .findCount()));

            return  maxData > 0 ? streamOpt.map(
                    stream -> ((Stream) stream).loadData(
                            ebean.find(StreamData.class)
                                    .fetch("values")
                                    .where().eq("stream.oid", ((Stream) stream).getId())
                                    .orderBy("serverTimestamp desc").setMaxRows(maxData)
                                    .findList()))
                    : streamOpt;

        } , executionContext);
    }

    public CompletionStage<Optional<Stream>> getStreamBy(String streamKey) {
        return supplyAsync(() -> {
            return ebean.find(Stream.class)
                    .where().eq("key", streamKey)
                    .findOneOrEmpty();
        } , executionContext);
    }

    public CompletionStage<List<StreamData>> getStreamDataBy(String sensorKey, String streamKey, Map<String, Long> queryParams) {
        return supplyAsync(() -> {
            Query q = ebean.find(StreamData.class).fetch("values");
            ExpressionList ex = q.where().eq("stream.key", streamKey).eq("stream.sensor.key", sensorKey);

            Long start  = queryParams.get("start");
            Long end    = queryParams.get("end");
            Long length = queryParams.get("length");

            if (start != null) ex.ge("serverTimestamp", start);
            if (end != null) ex.le("serverTimestamp", end);

            q = ex.orderBy("serverTimestamp desc");

            return (length != null) ? q.setMaxRows(length.intValue()).findList() : q.findList();

        } , executionContext);
    }

    public CompletionStage<List<StreamData>> getStreamDataBy(Long sensorId, Long streamId, Map<String, Long> queryParams) {
        return supplyAsync(() -> {
            Query q = ebean.find(StreamData.class).fetch("values");
            ExpressionList ex = q.where().eq("stream.oid", streamId).eq("stream.sensor.oid", sensorId);

            Long start  = queryParams.get("start");
            Long end    = queryParams.get("end");
            Long length = queryParams.get("length");

            if (start != null) ex.ge("serverTimestamp", start);
            if (end != null) ex.le("serverTimestamp", end);

            q = ex.orderBy("serverTimestamp desc");

            return (length != null) ? q.setMaxRows(length.intValue()).findList() : q.findList();

        } , executionContext);
    }

    /*public CompletionStage<Stream> loadStreamData(Stream stream, int maxData) {
        return supplyAsync(() ->
            , executionContext);
    }*/

}
