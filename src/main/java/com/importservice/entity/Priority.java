package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "priority")
public class Priority extends BaseEntity {
    
    @Id
    @JsonProperty("Id")
    @Column(name = "id")
    private Integer id;
    
    @JsonProperty("EnglishName")
    @Column(name = "english_name", length = 500)
    private String englishName;
    
    @JsonProperty("LocalName")
    @Column(name = "local_name", length = 500)
    private String localName;
    
    @JsonProperty("Order")
    @Column(name = "order_value")
    private Integer orderValue;
    
    @JsonProperty("Days")
    @Column(name = "days")
    private Integer days;
    
    @JsonProperty("Notes")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    // Constructors
    public Priority() {}
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }
    
    public String getLocalName() {
        return localName;
    }
    
    public void setLocalName(String localName) {
        this.localName = localName;
    }
    
    public Integer getOrderValue() {
        return orderValue;
    }
    
    public void setOrderValue(Integer orderValue) {
        this.orderValue = orderValue;
    }
    
    public Integer getDays() {
        return days;
    }
    
    public void setDays(Integer days) {
        this.days = days;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}