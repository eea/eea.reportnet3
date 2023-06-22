package org.eea.interfaces.vo.dremio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class DremioCredentials {

    private String userName;
    private String password;
}
