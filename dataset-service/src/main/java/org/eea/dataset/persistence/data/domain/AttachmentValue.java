package org.eea.dataset.persistence.data.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "ATTACHMENT_VALUE")
public class AttachmentValue {



  @Id
  @GenericGenerator(name = "attachment_sequence_generator",
      strategy = "org.eea.dataset.persistence.data.sequence.AttachmentValueIdGenerator")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachment_sequence_generator")
  @Column(name = "ID", columnDefinition = "serial")
  private String id;

  @Column(name = "FILE_NAME")
  private String fileName;

  @Lob
  @Column(name = "CONTENT")
  @Type(type = "org.hibernate.type.BinaryType")
  private byte content[];

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "FIELD_VALUE_ID")
  private FieldValue fieldValue;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, fileName);
  }



}
