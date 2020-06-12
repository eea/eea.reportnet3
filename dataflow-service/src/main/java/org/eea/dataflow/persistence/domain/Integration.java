package org.eea.dataflow.persistence.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class Integration.
 */
@Entity
@Table(name = "INTEGRATION")
@Getter
@Setter
@ToString
public class Integration {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "integration_id_seq")
  @SequenceGenerator(name = "integration_id_seq", sequenceName = "integration_id_seq",
      allocationSize = 1)
  private Long id;

  /** The dataflow. */
  @ManyToOne
  @JoinColumn(name = "DATAFLOW_ID")
  private Dataflow dataflow;

  /** The name. */
  @Column(name = "NAME")
  private String name;

  /** The description. */
  @Column(name = "DESCRIPTION")
  private String description;

  /** The tool. */
  @Column(name = "TOOL")
  private String tool;

  /** The operation. */
  @Column(name = "OPERATION")
  @Enumerated(EnumType.STRING)
  private IntegrationOperationTypeEnum operation;

  /** The internal parameters. */
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "integration")
  private List<InternalOperationParameters> internalParameters;

  /** The external parameters. */
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "integration")
  private List<ExternalOperationParameters> externalParameters;



  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Integration integration = (Integration) o;
    return id.equals(integration.id);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

}
