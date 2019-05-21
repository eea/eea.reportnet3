package org.eea.dataset.persistence.data.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "FIELD_VALUE")
public class FieldValue {

	@Id
	@Column(name = "ID")
	private Integer id;
	@Column(name = "TYPE")
	private String type;
	@Column(name = "VALUE")
    private String value;
	@Column(name = "ID_RECORD")
    private Long record;
	
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FieldValue field = (FieldValue) o;
		return id.equals(field.id) && type.equals(field.type) && value.equals(field.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type, value);
	}

}
