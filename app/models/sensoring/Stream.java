package models.sensoring;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import models.Persistent;
import utils.JsonViews;

import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@javax.persistence.Entity
public class Stream extends Persistent {

    private String key;
    private String name;
    private String tag;

    @ManyToOne(optional=false)
    private Sensor sensor;

    @Transient
    private Long totalSize;

    @OneToMany(cascade= CascadeType.ALL)
    @JsonManagedReference
    @JsonView(JsonViews.Complete.class)
    private List<StreamData> data = new ArrayList<StreamData>();

    public Stream (Sensor sensor) {
        this.key = UUID.randomUUID().toString().replace("-", "");
        this.sensor = sensor;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public Stream setName(String name) {
        this.name = name;
        return this;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public List<StreamData> getData() {
        return data;
    }

    public Stream loadData(List<StreamData> data) {
        this.data = data;
        return this;
    }

    public Stream setTotalSize(long totalSize) {
        this.totalSize = new Long(totalSize);
        return this;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public String getTag() {
        return tag;
    }

    public Stream setTag(String tag) {
        this.tag = tag;
        return this;
    }
}
