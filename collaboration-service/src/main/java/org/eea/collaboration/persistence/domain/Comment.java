package org.eea.collaboration.persistence.domain;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Comment
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "COMMENT")
public class Comment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", columnDefinition = "serial")
	private Long id;
	@Column(name = "CONTENT")
	private String content;

	@Column(name = "DATAFLOW_ID")
	private String dataflowId;

	@Column(name = "DATASET_ID")
	private String datasetId;

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Comment comment = (Comment) o;
		return id.equals(comment.id);

	}

	@Override
	public int hashCode() {
		return Objects.hash(id, content, dataflowId, datasetId);
	}
}
