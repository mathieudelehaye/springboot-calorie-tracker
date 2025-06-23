package org.example.model;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Integer version;

    /** 
     * Returns the primary key.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the primary key.
     * You’ll need this if you ever want to assign an existing ID (e.g. in an update flow).
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the optimistic-locking version.
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Sets the optimistic-locking version.
     */
    public void setVersion(Integer version) {
        this.version = version;
    }
}
