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
 * The type ReportingDataset.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "REPORTING_DATASET")
public class ReportingDataset extends Dataset{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", columnDefinition = "serial")
	private Long id;

	
	
	
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ReportingDataset reportingDataset = (ReportingDataset) o;
		return id.equals(reportingDataset.id);

	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

}
