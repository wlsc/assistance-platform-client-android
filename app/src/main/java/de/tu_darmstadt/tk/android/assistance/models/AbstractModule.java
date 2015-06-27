package de.tu_darmstadt.tk.android.assistance.models;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Wladimir Schmidt on 23.06.2015.
 */
public abstract class AbstractModule {

    enum STATE {
      ON, OFF
    };

    private long id;

    private long domainId;

    private String title;

    private String logo;

    private String moduleUrl;

    private String descriptionFull;

    private String descriptionShort;

    private String copyright;

    private Set<AbstractSensor> sensors;

    private Set<AbstractSensor> sensorsRequired;

    public AbstractModule() {
        this. id = -1;
        this.domainId = -1;
        this.title = "";
        this.logo = "";
        this.moduleUrl = "";
        this.descriptionFull = "";
        this.descriptionShort = "";
        this.copyright = "";
        this.sensors = new HashSet<>();
        this.sensorsRequired = new HashSet<>();
    }

    public Set<AbstractSensor> getSensorsRequired() {
        return sensorsRequired;
    }

    public void setSensorsRequired(Set<AbstractSensor> sensorsRequired) {
        this.sensorsRequired = sensorsRequired;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(long domainId) {
        this.domainId = domainId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getModuleUrl() {
        return moduleUrl;
    }

    public void setModuleUrl(String moduleUrl) {
        this.moduleUrl = moduleUrl;
    }

    public String getDescriptionFull() {
        return descriptionFull;
    }

    public void setDescriptionFull(String descriptionFull) {
        this.descriptionFull = descriptionFull;
    }

    public String getDescriptionShort() {
        return descriptionShort;
    }

    public void setDescriptionShort(String descriptionShort) {
        this.descriptionShort = descriptionShort;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public Set<AbstractSensor> getSensors() {
        return sensors;
    }

    public void setSensors(Set<AbstractSensor> sensors) {
        this.sensors = sensors;
    }
}
