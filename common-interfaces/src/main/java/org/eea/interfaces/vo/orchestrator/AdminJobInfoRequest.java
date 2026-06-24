package org.eea.interfaces.vo.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AdminJobInfoRequest {

    private List<Long> requestJobIds;
}
