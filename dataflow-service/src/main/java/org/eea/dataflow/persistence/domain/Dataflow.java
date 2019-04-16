package org.eea.dataflow.persistence.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The type Dataflow.
 */
@Entity
@Table(name = "DATAFLOW")
public class Dataflow {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;
  @Column(name = "name")
  private String name;
  @Column(name = "description")
  private String description;


  @OneToMany(mappedBy = "dataflow", cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<Contributor> contributors;

  /**
   * Gets id.
   *
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * Sets id.
   *
   * @param id the id
   */
  public void setId(final Long id) {
    this.id = id;
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets name.
   *
   * @param name the name
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Gets description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets description.
   *
   * @param description the description
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * Gets contributors.
   *
   * @return the contributors
   */
  public List<Contributor> getContributors() {
    return contributors;
  }

  /**
   * Sets contributors.
   *
   * @param contributors the contributors
   */
  public void setContributors(final List<Contributor> contributors) {
    this.contributors = contributors;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Dataflow dataflow = (Dataflow) o;
    return id.equals(dataflow.id);

  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, contributors);
  }

  @Override
  public String toString() {
    return "Dataflow{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", contributors=" + contributors +
        '}';
  }
}
