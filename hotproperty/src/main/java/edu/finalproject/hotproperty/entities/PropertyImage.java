package edu.finalproject.hotproperty.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "property_images")
public class PropertyImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String imageFileName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "property_id", nullable = false)
  private Property property;

  public PropertyImage() {
  }

  public PropertyImage(String imageFileName, Property property) {
    this.imageFileName = imageFileName;
    this.property = property;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getImageFileName() {
    return imageFileName;
  }

  public void setImageFileName(String imageFileName) {
    this.imageFileName = imageFileName;
  }

  public Property getProperty() {
    return property;
  }

  public void setProperty(Property property) {
    this.property = property;
  }
}
