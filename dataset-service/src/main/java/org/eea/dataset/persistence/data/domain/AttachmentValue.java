package org.eea.dataset.persistence.data.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
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

/**
 * The Class AttachmentValue.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "ATTACHMENT_VALUE")
public class AttachmentValue {


  /** The id. */
  @Id
  @GenericGenerator(name = "attachment_sequence_generator",
      strategy = "org.eea.dataset.persistence.data.sequence.AttachmentValueIdGenerator")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachment_sequence_generator")
  @Column(name = "ID", columnDefinition = "serial")
  private String id;

  /** The file name. */
  @Column(name = "FILE_NAME")
  private String fileName;

  /** The content. */
  @Lob
  @Column(name = "CONTENT")
  @Type(type = "org.hibernate.type.BinaryType")
  private byte content[];


  /** The field value. */
  @OneToOne
  @JoinColumn(name = "FIELD_VALUE_ID")
  private FieldValue fieldValue;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, fileName, fieldValue);
  }


  /**
   * Equals.
   *
   * @param obj the o
   *
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final AttachmentValue attachment = (AttachmentValue) obj;
    return id.equals(attachment.id) && fileName.equals(attachment.fileName)
        && fieldValue.equals(attachment.fieldValue);
  }

}
