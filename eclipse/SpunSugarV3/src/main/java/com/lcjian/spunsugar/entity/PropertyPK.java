package com.lcjian.spunsugar.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PropertyPK implements java.io.Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @Column(name = "subject_id", nullable = false)
    private Integer subjectId;
    
    @Column(name = "_key", length = 45, nullable = false)
    private String key;
    
    public PropertyPK() {
    }
    
    public PropertyPK(Integer subjectId, String key) {
        super();
        this.subjectId = subjectId;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PropertyPK other = (PropertyPK) obj;
        return ((this.key == null && other.key == null) || this.key.equals(other.key))
                && ((this.subjectId == null && other.subjectId == null) || this.subjectId.equals(other.subjectId));
    }
}
