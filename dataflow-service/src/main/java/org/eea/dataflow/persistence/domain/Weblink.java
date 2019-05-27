package org.eea.dataflow.persistence.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Weblink.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "WEBLINK")
public class Weblink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;
  @Column(name = "NAME")
  private String name;
  @Column(name = "URL")
  private String url;
  @ManyToOne
  @JoinColumn(name = "DATAFLOW_ID")
  private Dataflow dataflow;

  
  @Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Weblink contributor = (Weblink) o;
		return id.equals(contributor.id);

	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, url, dataflow);
	}
}
