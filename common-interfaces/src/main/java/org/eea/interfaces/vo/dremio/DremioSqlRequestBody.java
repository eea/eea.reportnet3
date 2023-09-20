package org.eea.interfaces.vo.dremio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class DremioSqlRequestBody {

    private String sql;
}
