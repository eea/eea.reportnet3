package org.eea.dataset.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.SneakyThrows;
import org.eea.dataset.service.*;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.PreparationDatasetResponseVO;
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
        PreparationDatasetResponseVO preparationDatasetResponseVO = new PreparationDatasetResponseVO();
        List<PreparationDatasetVO> list = new ArrayList<>();
        list.add(new PreparationDatasetVO());
        preparationDatasetResponseVO.setPreparationDatasetList(list);
        preparationDatasetResponseVO.setActiveLocks(new HashMap<>());

        when(preparationDatasetService.findPreparationDatasets(1L, 2L, null))
                .thenReturn(preparationDatasetResponseVO);

        preparationDatasetControllerImpl.list(1L, 2L, null);

        Mockito.verify(preparationDatasetService, times(1))
                .findPreparationDatasets(1L, 2L, null);
    }

    @Test
    public void listPreparationDatasetsWithCodeTest() {
        PreparationDatasetResponseVO preparationDatasetResponseVO = new PreparationDatasetResponseVO();
        List<PreparationDatasetVO> list = new ArrayList<>();
        list.add(new PreparationDatasetVO());
        preparationDatasetResponseVO.setPreparationDatasetList(list);
        preparationDatasetResponseVO.setActiveLocks(new HashMap<>());

        when(preparationDatasetService.findPreparationDatasets(1L, 2L, "albania_set_1"))
                .thenReturn(preparationDatasetResponseVO);

        preparationDatasetControllerImpl.list(1L, 2L, "albania_set_1");

        Mockito.verify(preparationDatasetService, times(1))
                .findPreparationDatasets(1L, 2L, "albania_set_1");
    }

    /**
     * Create preparation dataset test.
     */
    @Test
    public void createPreparationDatasetTest() throws EEAException {
        PreparationDatasetVO vo = new PreparationDatasetVO();
        vo.setDataflowId(1L);
        vo.setProviderId(2L);
        vo.setCode("prep_code");
        vo.setDatasetName("Prep Dataset");

        preparationDatasetControllerImpl.createPreparationDataset(vo);

        Mockito.verify(preparationDatasetService, times(1))
                .createPreparationDataset(
                        vo.getDataflowId(),
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
        vo.setCode("prep_code");
        vo.setDatasetName("Prep Dataset");

        Mockito.doThrow(new EEAException("Error"))
                .when(preparationDatasetService)
                .createPreparationDataset(
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
        preparationDatasetControllerImpl.deletePreparationDatasetById(10L, 1L);

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
