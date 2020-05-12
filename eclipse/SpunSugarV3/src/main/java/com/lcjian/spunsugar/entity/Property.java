package com.lcjian.spunsugar.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "property")
public class Property implements java.io.Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private PropertyPK id;

    @MapsId("subjectId")
    @ManyToOne
    @JoinColumn(name = "subject_id", referencedColumnName = "id", nullable = false)
    private Subject subject;
    
    @Column(name = "_value", nullable = false)
    private String value;

    public Property() {
    }

    public Property(Subject subject, String key, String value) {
        super();
        this.subject = subject;
        this.id = new PropertyPK(null, key);
        this.value = value;
    }

    public PropertyPK getId() {
        return id;
    }

    public void setId(PropertyPK id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
