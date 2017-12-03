package models.sensoring;

import models.Persistent;

import javax.persistence.Entity;

@Entity
public class Tuple extends Persistent {

    private String key;
    private String value;

    public Tuple(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

}
