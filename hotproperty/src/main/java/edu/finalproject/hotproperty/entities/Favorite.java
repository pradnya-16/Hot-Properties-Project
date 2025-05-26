package edu.finalproject.hotproperty.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorites", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "property_id" }) })
public class Favorite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User buyer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "property_id", nullable = false)
  private Property property;

  public Favorite() {
  }

  public Favorite(User buyer, Property property) {
    this.buyer = buyer;
    this.property = property;
  }

  // pre persist is used to set created at before saved to db
  @PrePersist
  protected void onPersist() {
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public User getBuyer() {
    return buyer;
  }

  public void setBuyer(User buyer) {
    this.buyer = buyer;
  }

  public Property getProperty() {
    return property;
  }

  public void setProperty(Property property) {
    this.property = property;
  }
}
