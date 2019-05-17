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
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Dataflow.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "DATAFLOW")
public class Dataflow {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", columnDefinition = "serial")
	private Long id;

	@Column(name = "NAME")
	private String name;

	@Column(name = "DESCRIPTION")
	private String description;

	@OneToOne(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = true)
	private SubmissionAgreement submissionAgreement;

	@OneToMany(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Contributor> contributors;

	@OneToMany(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Document> documents;

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
		return Objects.hash(id, name, description, contributors, submissionAgreement, documents);
	}

}
