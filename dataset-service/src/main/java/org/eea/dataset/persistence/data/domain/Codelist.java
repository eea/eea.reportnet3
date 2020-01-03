package org.eea.dataset.persistence.data.domain;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.eea.interfaces.vo.dataset.enums.CodelistStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Codelist.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "CODELIST")
public class Codelist {

  /** The id. */
  @Id
  @SequenceGenerator(name = "codelist_sequence_generator",
      sequenceName = "codelist_sequence_generator", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "codelist_sequence_generator")
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The name. */
  @Column(name = "name")
  private String name;

  /** The description. */
  @Column(name = "description")
  private String description;

  /** The category. */
  @ManyToOne
  @JoinColumn(name = "id_category")
  private CodelistCategory category;

  /** The version. */
  @Column(name = "version")
  private Long version;

  /** The items. */
  @OneToMany(mappedBy = "codelistId", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<CodelistItem> items;

  /** The status. */
  @Column(name = "status")
  private CodelistStatusEnum status;
}
