package edu.finalproject.hotproperty.entities;

import edu.finalproject.hotproperty.entities.enums.RoleType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String firstName;

  @Column(nullable = false, length = 100)
  private String lastName;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false, length = 255)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RoleType role;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Set<Property> properties = new HashSet<>();

  @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Set<Message> sentMessages = new HashSet<>();

  @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Set<Favorite> favorites = new HashSet<>();

  public User() {
  }

  public User(String firstName, String lastName, String email, String password, RoleType role) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.role = role;
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public RoleType getRole() {
    return role;
  }

  public void setRole(RoleType role) {
    this.role = role;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Set<Property> getProperties() {
    return properties;
  }

  public void setProperties(Set<Property> properties) {
    this.properties = properties;
  }

  public Set<Message> getSentMessages() {
    return sentMessages;
  }

  public void setSentMessages(Set<Message> sentMessages) {
    this.sentMessages = sentMessages;
  }

  public Set<Favorite> getFavorites() {
    return favorites;
  }

  public void setFavorites(Set<Favorite> favorites) {
    this.favorites = favorites;
  }

  public void addProperty(Property property) {
    properties.add(property);
    property.setAgent(this);
  }

  public void removeProperty(Property property) {
    properties.remove(property);
    property.setAgent(null);
  }

  public void addSentMessage(Message message) {
    sentMessages.add(message);
    message.setSender(this);
  }

  public void removeSentMessage(Message message) {
    sentMessages.remove(message);
    message.setSender(null);
  }

  public void addFavorite(Favorite favorite) {
    favorites.add(favorite);
    favorite.setBuyer(this);
  }

  public void removeFavorite(Favorite favorite) {
    favorites.remove(favorite);
    favorite.setBuyer(null);
  }
}
