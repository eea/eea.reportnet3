package org.eea.interfaces.vo.dremio;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DremioDirectoryItem {

    String id;
    List<String> path;
    String type;
    String datasetType;
    String containerType;
}
