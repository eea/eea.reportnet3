package org.eea.dataset.mapper;

import org.eea.dataset.persistence.data.domain.FieldValidation;
import org.eea.interfaces.vo.dataset.FieldValidationVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring"
)
public interface FieldValidationMapper extends IMapper<FieldValidation, FieldValidationVO> {

}
