package org.eea.interfaces.vo.dremio;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DremioAuthResponse {

    String token;
    String userName;
    Long expires;
    String email;
}
