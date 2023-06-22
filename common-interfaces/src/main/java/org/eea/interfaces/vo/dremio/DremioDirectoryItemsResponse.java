package org.eea.interfaces.vo.dremio;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DremioDirectoryItemsResponse {

    String entityType;
    String id;
    List<String> path;
    List<DremioDirectoryItem> children;
}
