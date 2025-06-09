package edu.finalproject.hotproperty.entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "properties")
public class Property {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255, unique = true)
  private String title;

  @Column(nullable = false)
  private Double price;

  @Column(nullable = false, length = 255)
  private String location;

  @Column(nullable = false)
  private Integer size;

  @Column(columnDefinition = "TEXT")
  private String description;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "agent_id", nullable = false)
  private User agent;

  @OneToMany(
      mappedBy = "property",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<PropertyImage> images = new HashSet<>();

  @OneToMany(
      mappedBy = "property",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<Favorite> favorites = new HashSet<>();

  @OneToMany(
      mappedBy = "property",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<Message> messages = new HashSet<>();

  @Transient private int favoriteCount;

  public Property() {}

  public Property(
      String title, Double price, String location, Integer size, String description, User agent) {
    this.title = title;
    this.price = price;
    this.location = location;
    this.size = size;
    this.description = description;
    this.agent = agent;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Double getPrice() {
    return price;
  }

  public void setPrice(Double price) {
    this.price = price;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public User getAgent() {
    return agent;
  }

  public void setAgent(User agent) {
    this.agent = agent;
  }

  public Set<PropertyImage> getImages() {
    return images;
  }

  public void setImages(Set<PropertyImage> images) {
    this.images = images;
  }

  public Set<Favorite> getFavorites() {
    return favorites;
  }

  public void setFavorites(Set<Favorite> favorites) {
    this.favorites = favorites;
  }

  public Set<Message> getMessages() {
    return messages;
  }

  public void setMessages(Set<Message> messages) {
    this.messages = messages;
  }

  public void addImage(PropertyImage image) {
    images.add(image);
    image.setProperty(this);
  }

  public void removeImage(PropertyImage image) {
    images.remove(image);
    image.setProperty(null);
  }

  public void addFavorite(Favorite favorite) {
    favorites.add(favorite);
    favorite.setProperty(this);
  }

  public void removeFavorite(Favorite favorite) {
    favorites.remove(favorite);
    favorite.setProperty(null);
  }

  public void addMessage(Message message) {
    messages.add(message);
    message.setProperty(this);
  }

  public void removeMessage(Message message) {
    messages.remove(message);
    message.setProperty(null);
  }

  public int getFavoriteCount() {
    return favoriteCount;
  }

  public void setFavoriteCount(int favoriteCount) {
    this.favoriteCount = favoriteCount;
  }
}
