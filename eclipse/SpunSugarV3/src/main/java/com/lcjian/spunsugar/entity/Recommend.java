package com.lcjian.spunsugar.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "recommend")
public class Recommend implements java.io.Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;
    
    @Column(name = "type", nullable = false, length = 100)
    private String type;
    
    @Column(name = "extra", length = 100)
    private String extra;
    
    @Column(name = "extra1", length = 100)
    private String extra1;
    
    @Column(name = "subject_id", nullable = false)
    private Integer subjectId;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    public Recommend() {
    }

    public Recommend(String title, String type, String extra, String extra1, Integer subjectId, LocalDateTime createTime) {
        super();
        this.title = title;
        this.type = type;
        this.extra = extra;
        this.extra1 = extra1;
        this.subjectId = subjectId;
        this.createTime = createTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
    
    public String getExtra1() {
        return extra1;
    }

    public void setExtra1(String extra1) {
        this.extra1 = extra1;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
