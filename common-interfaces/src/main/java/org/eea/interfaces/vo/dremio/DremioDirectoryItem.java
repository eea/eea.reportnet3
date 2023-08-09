package org.eea.interfaces.vo.dremio;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class DremioDirectoryItem {

    String id;
    List<String> path;
    String type;
    String datasetType;
    String containerType;
}
