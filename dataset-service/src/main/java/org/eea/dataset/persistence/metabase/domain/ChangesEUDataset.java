package org.eea.dataset.persistence.metabase.domain;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class ChangesEUDataset.
 */
@Setter
@Getter
@ToString
@Entity
@Table(name = "changes_eudataset", schema = "public")
public class ChangesEUDataset implements Serializable {



  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 331689131017596907L;

  /**
   * The id.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "changes_eudataset_id_seq")
  @SequenceGenerator(name = "changes_eudataset_id_seq", sequenceName = "changes_eudataset_id_seq",
      allocationSize = 1)
  @Column(name = "id", columnDefinition = "serial")
  private Long id;


  /** The datacollection. */
  @Column(name = "datacollection")
  private Long datacollection;


  /** The provider. */
  @Column(name = "provider")
  private String provider;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {

    return Objects.hash(id, datacollection, provider);

  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ChangesEUDataset other = (ChangesEUDataset) obj;
    return Objects.equals(datacollection, other.datacollection)
        && Objects.equals(provider, other.provider);
  }
}
