package org.eea.validation.service;

import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dataset.GroupValidationVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DataLakeValidationService {

    List<GroupValidationVO> findGroupRecordsByFilter(S3PathResolver s3PathResolver,
                                                     List<ErrorTypeEnum> levelErrorsFilter, List<EntityTypeEnum> typeEntitiesFilter,
                                                     String tableFilter, String fieldValueFilter, Pageable pageable, String headerField,
                                                     Boolean asc, boolean paged);
}
