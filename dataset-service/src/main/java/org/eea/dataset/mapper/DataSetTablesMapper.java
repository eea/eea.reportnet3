package org.eea.dataset.mapper;

import java.util.List;
import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.domain.TableHeadersCollection;
import org.eea.interfaces.vo.metabase.TableCollectionVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * The interface DataSetTablesMapper.
 */
@Mapper(componentModel = "spring")
public interface DataSetTablesMapper extends IMapper<TableCollection, TableCollectionVO> {

  /**
   * Fill ids.
   *
   * @param tableCollectionVO the table collection VO
   * @param tableCollection the table collection
   */
  @AfterMapping
  default void fillIds(TableCollectionVO tableCollectionVO,
      @MappingTarget TableCollection tableCollection) {
    List<TableHeadersCollection> tableCollectionValues =
        tableCollection.getTableHeadersCollections();
    tableCollectionValues.stream()
        .forEach(tableCollectionValue -> tableCollectionValue.setTableId(tableCollection));
  }
}
