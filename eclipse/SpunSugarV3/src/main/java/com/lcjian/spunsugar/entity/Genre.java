package com.lcjian.spunsugar.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "genre", uniqueConstraints ={@UniqueConstraint(columnNames={"type", "name"})})
public class Genre implements java.io.Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    
    @Column(name = "type", nullable = false, length = 45)
    private String type;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @ManyToMany(mappedBy = "genres")
    private Set<Subject> subjects = new HashSet<Subject>(0);

    public Genre() {
    }

    public Genre(String type, String name) {
        super();
        this.type = type;
        this.name = name;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(Set<Subject> subjects) {
        this.subjects = subjects;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
