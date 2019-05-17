package org.eea.dataflow.persistence.domain;

import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Submission agreement.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "SUBMISSION_AGREEMENT")
public class SubmissionAgreement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;
  @Column(name = "NAME")
  private String name;
  @Column(name = "DESCRIPTION")
  private String description;
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "DATAFLOW_ID", referencedColumnName = "ID")
  private Dataflow dataflow;

  @Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final SubmissionAgreement submissionAgreement = (SubmissionAgreement) o;
		return id.equals(submissionAgreement.id);

	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, description, dataflow);
	}
 
}
