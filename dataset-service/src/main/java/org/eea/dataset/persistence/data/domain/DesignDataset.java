package org.eea.dataset.persistence.data.domain;

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
 * The type DesignDataset.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "DESIGNDATASET")
public class DesignDataset extends Dataset{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", columnDefinition = "serial")
	private Long id;

	@Column(name = "TYPE")
	private String type;

	
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final DesignDataset designDataset = (DesignDataset) o;
		return id.equals(designDataset.id);

	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

}
