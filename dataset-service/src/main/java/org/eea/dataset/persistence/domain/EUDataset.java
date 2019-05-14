package org.eea.dataset.persistence.domain;

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
 * The type EUDataset.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "EUDATASET")
public class EUDataset extends Dataset{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", columnDefinition = "serial")
	private Long id;

	@Column(name = "NAME")
	private String name;

	@Column(name = "VISIBLE")
	private Boolean visible;



	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final EUDataset euDataset = (EUDataset) o;
		return id.equals(euDataset.id);

	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, visible);
	}

}
