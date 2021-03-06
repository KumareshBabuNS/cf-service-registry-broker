package com.gopivotal.cf.srb.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "registered_services")
public class RegisteredService {

    @Id
    private String id;

    /**
     * Marketplace Metadata
     */

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String longDescription;

    @Column(nullable = false)
    private String provider;

    @ElementCollection
    @CollectionTable(name = "registered_service_features", joinColumns = @JoinColumn(name = "registered_service_id"))
    @Column(name = "feature")
    private List<String> features;

    /**
     * Service Binding Credentials
     */

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String basicAuthUser;

    @Column(nullable = false)
    private String basicAuthPassword;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBasicAuthUser() {
        return basicAuthUser;
    }

    public void setBasicAuthUser(String basicAuthUser) {
        this.basicAuthUser = basicAuthUser;
    }

    public String getBasicAuthPassword() {
        return basicAuthPassword;
    }

    public void setBasicAuthPassword(String basicAuthPassword) {
        this.basicAuthPassword = basicAuthPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisteredService that = (RegisteredService) o;

        if (!basicAuthPassword.equals(that.basicAuthPassword)) return false;
        if (!basicAuthUser.equals(that.basicAuthUser)) return false;
        if (!description.equals(that.description)) return false;
        if (!displayName.equals(that.displayName)) return false;
        if (!features.equals(that.features)) return false;
        if (!longDescription.equals(that.longDescription)) return false;
        if (!name.equals(that.name)) return false;
        if (!provider.equals(that.provider)) return false;
        if (!url.equals(that.url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + displayName.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + longDescription.hashCode();
        result = 31 * result + provider.hashCode();
        result = 31 * result + features.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + basicAuthUser.hashCode();
        result = 31 * result + basicAuthPassword.hashCode();
        return result;
    }
}
