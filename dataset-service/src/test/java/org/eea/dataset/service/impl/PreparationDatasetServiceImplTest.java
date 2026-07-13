package org.eea.dataset.service.impl;

import org.eea.dataset.mapper.PreparationDatasetMapper;
import org.eea.dataset.persistence.metabase.domain.PreparationDataset;
import org.eea.dataset.persistence.metabase.repository.PreparationDatasetRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.PreparationDatasetResponseVO;
import org.eea.interfaces.vo.dataset.PreparationDatasetVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.redis.RedisLockService;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class PreparationDatasetServiceImplTest {

    @InjectMocks
    private PreparationDatasetServiceImpl service;

    @Mock
    private PreparationDatasetRepository repository;
    @Mock
    private DremioHelperService dremioHelperService;
    @Mock
    private DatasetMetabaseService datasetMetabaseService;
    @Mock
    private DatasetSchemaService datasetSchemaService;
    @Mock
    private S3Helper s3Helper;
    @Mock
    private S3Service s3Service;
    @Mock
    private RepresentativeController.RepresentativeControllerZuul representativeControllerZuul;
    @Mock
    private KafkaSenderUtils kafkaSenderUtils;
    @Mock
    private PreparationDatasetMapper mapper;
    @Mock
    private RedisLockService redisLockService;

    /** The security context. */
    private SecurityContext securityContext;

    /** The authentication. */
    private Authentication authentication;

    private PreparationDataset entity;
    private PreparationDatasetVO vo;

    @Before
    public void init() {
        ThreadPropertiesManager.setVariable("user", "user");
        authentication = Mockito.mock(Authentication.class);
        securityContext = Mockito.mock(SecurityContext.class);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        entity = new PreparationDataset();
        entity.setId(1L);
        entity.setDataflowId(10L);
        entity.setProviderId(20L);
        entity.setCode("CODE");

        vo = new PreparationDatasetVO();
        vo.setId(1L);
        vo.setDataflowId(10L);
        vo.setProviderId(20L);
        vo.setCode("CODE");
    }

    // =========================
    // findPreparationDatasets
    // =========================

    @Test
    public void findPreparationDatasetsWithCodeTest() {

        List<PreparationDataset> entities = Collections.singletonList(entity);

        Mockito.when(repository.findByDataflowIdAndProviderIdAndCode(10L, 20L, "CODE"))
                .thenReturn(entities);
        Mockito.when(mapper.entityToClass(entity)).thenReturn(vo);
        Mockito.when(redisLockService.listActiveLocks(Mockito.anyString()))
                .thenReturn(Collections.emptyMap());

        PreparationDatasetResponseVO response =
                service.findPreparationDatasets(10L, 20L, "CODE");

        Assert.assertEquals(1, response.getPreparationDatasetList().size());
    }

    // =========================
    // findByDataflowIdAndProviderIdAndIsCreated
    // =========================

    @Test
    public void findByDataflowIdAndProviderIdAndIsCreatedNullDefaultsToFalseTest() {

        Mockito.when(repository.findByDataflowIdAndProviderIdAndIsCreated(10L, 20L, false))
                .thenReturn(Collections.singletonList(entity));
        Mockito.when(mapper.entityListToClass(Mockito.anyList()))
                .thenReturn(Collections.singletonList(vo));

        List<PreparationDatasetVO> result =
                service.findByDataflowIdAndProviderIdAndIsCreated(10L, 20L, null);

        Assert.assertFalse(result.isEmpty());
    }

    // =========================
    // createPreparationDataset
    // =========================

    @Test(expected = EEAException.class)
    public void createPreparationDatasetAlreadyExistsTest() throws Exception {

        Mockito.when(repository.existsByDataflowIdAndProviderIdAndCode(10L, 20L, "CODE"))
                .thenReturn(true);

        service.createPreparationDataset(10L, vo);
    }

    @Test
    public void createPreparationDatasetSuccessTest() throws Exception {

        Mockito.when(repository.existsByDataflowIdAndProviderIdAndCode(10L, 20L, "CODE"))
                .thenReturn(false);

        service.createPreparationDataset(10L, vo);

        Mockito.verify(repository).save(Mockito.any(PreparationDataset.class));
    }

    // =========================
    // deletePreparationDatasetById
    // =========================

    @Test(expected = EEAException.class)
    public void deletePreparationDatasetNotFoundTest() throws Exception {

        Mockito.when(repository.findById(1L)).thenReturn(Optional.empty());

        service.deletePreparationDatasetById(1L);
    }

    @Test(expected = EEAException.class)
    public void deletePreparationDatasetParentMetadataMissingTest() throws Exception {

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(entity));
        Mockito.when(datasetMetabaseService.getDatasetsByDataflowIdAndProviderId(10L, 20L))
                .thenReturn(Collections.emptyList());

        service.deletePreparationDatasetById(1L);
    }

    @Test
    public void deletePreparationDatasetSuccessTest() throws Exception {

        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(entity));
        Mockito.when(datasetMetabaseService.getDatasetsByDataflowIdAndProviderId(10L, 20L))
                .thenReturn(Collections.singletonList(Mockito.mock(org.eea.interfaces.vo.dataset.DataSetMetabaseVO.class)));
        Mockito.when(datasetSchemaService.getTableSchemasIds(Mockito.anyLong()))
                .thenReturn(Collections.emptyList());

        service.deletePreparationDatasetById(1L);

        Mockito.verify(repository).deleteById(1L);
    }

    // =========================
    // createAllEligiblePreparationSets
    // =========================

    @Test(expected = EEAException.class)
    public void createAllEligiblePreparationSetsLockFailureTest() throws Exception {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("user");

        Mockito.when(redisLockService.checkAndAcquireLock(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(false);
        Mockito.when(redisLockService.listActiveLocks(Mockito.anyString()))
                .thenReturn(Collections.singletonMap("lock", "value"));

        service.createAllEligiblePreparationSets(10L, 20L);
    }

    @Test
    public void createAllEligiblePreparationSetsEmptyQueueTest() throws Exception {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("user");

        Mockito.when(redisLockService.checkAndAcquireLock(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(true);

        Mockito.when(repository.findByDataflowIdAndProviderIdAndIsCreated(10L, 20L, false))
                .thenReturn(Collections.emptyList());
        Mockito.when(mapper.entityListToClass(Mockito.anyList()))
                .thenReturn(Collections.emptyList());

        service.createAllEligiblePreparationSets(10L, 20L);

        Mockito.verify(redisLockService).releaseLock(Mockito.anyString(), Mockito.anyString());
    }
}
