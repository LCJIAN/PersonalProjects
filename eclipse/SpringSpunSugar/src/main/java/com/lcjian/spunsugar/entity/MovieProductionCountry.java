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

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "movie_production_country")
public class MovieProductionCountry implements java.io.Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 45)
    private String name;

    @ManyToMany(mappedBy = "movieProductionCountries")
    private Set<Movie> movies = new HashSet<Movie>(0);

    public Integer getId() {
        return id;
    }

    public void setIso_3166_1(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Movie> getMovies() {
        return movies;
    }

    public void setMovies(Set<Movie> movies) {
        this.movies = movies;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
