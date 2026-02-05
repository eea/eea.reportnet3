package org.eea.dataset.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import org.eea.dataset.service.*;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.PreparationDatasetVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class PreparationDatasetControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class PreparationDatasetControllerImplTest {

    /** The preparation dataset controller impl. */
    @InjectMocks
    private PreparationDatasetControllerImpl preparationDatasetControllerImpl;

    /** The preparation dataset service. */
    @Mock
    private PreparationDatasetService preparationDatasetService;
    @Mock
    private DatasetMetabaseService datasetMetabaseService;
    @Mock
    private DatasetSchemaService datasetSchemaService;
    @Mock
    private DataLakeDataRetrieverFactory dataLakeDataRetrieverFactory;
    @Mock
    private DataLakeDataRetriever datasetDataRetrieverDL;


    /**
     * Inits the mocks.
     */
    @Before
    public void initMocks() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * List preparation datasets test.
     */
    @Test
    public void listPreparationDatasetsTest() {
        List<PreparationDatasetVO> list = new ArrayList<>();
        list.add(new PreparationDatasetVO());

        when(preparationDatasetService.findPreparationDatasets(1L, 2L))
                .thenReturn(list);

        preparationDatasetControllerImpl.list(1L, 2L);

        Mockito.verify(preparationDatasetService, times(1))
                .findPreparationDatasets(1L, 2L);
    }

    /**
     * Create preparation dataset test.
     */
    @Test
    public void createPreparationDatasetTest() throws EEAException {
        PreparationDatasetVO vo = new PreparationDatasetVO();
        vo.setDataflowId(1L);
        vo.setProviderId(2L);
        vo.setParentDatasetId(3L);
        vo.setCode("prep_code");
        vo.setDatasetName("Prep Dataset");

        preparationDatasetControllerImpl.createPreparationDataset(vo);

        Mockito.verify(preparationDatasetService, times(1))
                .createPreparationDataset(
                        vo.getDataflowId(),
                        vo.getParentDatasetId(),
                        vo
                );
    }

    /**
     * Create preparation dataset test exception.
     */
    @Test(expected = ResponseStatusException.class)
    public void createPreparationDatasetExceptionTest() throws EEAException {
        PreparationDatasetVO vo = new PreparationDatasetVO();
        vo.setDataflowId(1L);
        vo.setProviderId(2L);
        vo.setParentDatasetId(3L);
        vo.setCode("prep_code");
        vo.setDatasetName("Prep Dataset");

        Mockito.doThrow(new EEAException("Error"))
                .when(preparationDatasetService)
                .createPreparationDataset(
                        Mockito.anyLong(),
                        Mockito.anyLong(),
                        Mockito.any()
                );

        preparationDatasetControllerImpl.createPreparationDataset(vo);
    }

    /**
     * Delete preparation dataset by id test.
     */
    @SneakyThrows
    @Test
    public void deletePreparationDatasetByIdTest() {
        preparationDatasetControllerImpl.deletePreparationDatasetById(10L);

        Mockito.verify(preparationDatasetService, times(1))
                .deletePreparationDatasetById(10L);
    }

    /**
     * Get preparation table values DL test.
     */
    @Test
    public void getPreparationTableValuesDLTest() throws Exception {
        DataFlowVO dataFlowVO = new DataFlowVO();
        dataFlowVO.setId(1L);
// add any fields that execute() uses

        Long preparationId = 1542L;
        String preparationCode = "albania_set_1";
        String tableSchemaId = "697a169e470f23e72dc9f354";

        DataSetMetabaseVO dataset = new DataSetMetabaseVO();
        dataset.setDatasetSchema("schemaId");
        dataset.setDatasetTypeEnum(DatasetTypeEnum.REPORTING);


        TableSchemaVO tableSchemaVO = new TableSchemaVO();
        TableVO tableVO = new TableVO();

        when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong())).thenReturn(dataset);
        when(datasetSchemaService.getTableSchemaVO(Mockito.anyString(), Mockito.anyString())).thenReturn(tableSchemaVO);
        when(dataLakeDataRetrieverFactory.getRetriever(Mockito.anyLong())).thenReturn(datasetDataRetrieverDL);
        when(datasetDataRetrieverDL.getPreparationTableResult(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()
        )).thenReturn(tableVO);

        TableVO result = preparationDatasetControllerImpl.getPreparationTableValuesDL(
                preparationId, preparationCode, tableSchemaId,
                0, 10, null, null, null, null, null, null
        );

        Assert.assertNotNull(result);
    }
}
