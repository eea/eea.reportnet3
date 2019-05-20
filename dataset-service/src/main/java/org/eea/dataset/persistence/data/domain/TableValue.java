package org.eea.dataset.persistence.data.domain;

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
@Table(name = "TABLE_VALUE")
public class TableValue {

	@Id
	@Column(name = "ID")
	private Integer id;
	@Column(name = "NAME")
	private String name;
	
	@OneToMany(mappedBy = "tableValue", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Record> records;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TableValue table = (TableValue) o;
		return id.equals(table.id) && name.equals(table.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, records);
	}

}
