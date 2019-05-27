package org.eea.dataset.persistence.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "RECORD")
public class Record {

	@Id
	@Column(name = "ID")
	private Integer id;
	@Column(name = "NAME")
	private String name;
	@Column(name = "ID_TABLE")
    private Long tableValue;
	@OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<FieldValue> fields;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Record record = (Record) o;
		return id.equals(record.id) && name.equals(record.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, tableValue);
	}

}
