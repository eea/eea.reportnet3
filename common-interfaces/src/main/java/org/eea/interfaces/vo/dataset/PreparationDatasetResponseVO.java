package org.eea.interfaces.vo.dataset;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * There is no entity for this class, is a helper for controllers response
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PreparationDatasetResponseVO {

    private List<PreparationDatasetVO> preparationDatasetList;
    private Map<String, String> activeLocks;

}
