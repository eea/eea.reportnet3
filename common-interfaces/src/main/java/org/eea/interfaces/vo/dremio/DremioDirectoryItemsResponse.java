package org.eea.interfaces.vo.dremio;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class DremioDirectoryItemsResponse {

    String entityType;
    String id;
    List<String> path;
    List<DremioDirectoryItem> children;
}
