package org.eea.dataset.service.impl;

import org.eea.dataset.persistence.metabase.domain.DatasetTable;
import org.eea.dataset.persistence.metabase.repository.DatasetTableRepository;
import org.eea.interfaces.vo.dataset.DatasetTableVO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class DatasetTableServiceImplTest {

    @InjectMocks
    private DatasetTableServiceImpl classUnderTest;

    @Mock
    private DatasetTableRepository datasetTableRepositoryMock;


    @Test
    public void getDatasetTablesByEditingUser() {

        final String username = "testUser";
        final List<DatasetTable> datasetTables = new ArrayList<>();
        final DatasetTable datasetTable = new DatasetTable();
        datasetTable.setDatasetId(1L);
        datasetTable.setTableName("table");
        datasetTables.add(datasetTable);

        Mockito.when(datasetTableRepositoryMock.findDatasetTablesByEditingUsername(username))
                .thenReturn(datasetTables);

        final List<DatasetTableVO> actualResult = classUnderTest.getDatasetTablesByEditingUser(username);

        Assert.assertNotNull(actualResult);
        Assert.assertEquals(1, actualResult.size());
        final DatasetTableVO datasetTableVO = actualResult.get(0);
        Assert.assertEquals(Long.valueOf(1L), datasetTableVO.getDatasetId());
        Assert.assertEquals("table", datasetTableVO.getTableName());
    }

    @Test
    public void getDatasetTablesByEditingUser_foundEmptyList() {

        final String username = "testUser";
        final List<DatasetTable> datasetTables = new ArrayList<>();

        Mockito.when(datasetTableRepositoryMock.findDatasetTablesByEditingUsername(username))
                .thenReturn(datasetTables);

        final List<DatasetTableVO> actualResult = classUnderTest.getDatasetTablesByEditingUser(username);

        Assert.assertNotNull(actualResult);
        Assert.assertEquals(0, actualResult.size());
    }


    @Test
    public void getDatasetTablesWithExpiredEditingLocks() {

        final List<DatasetTable> datasetTables = new ArrayList<>();
        final DatasetTable datasetTable = new DatasetTable();
        datasetTable.setDatasetId(1L);
        datasetTable.setTableName("table");
        datasetTables.add(datasetTable);

        Mockito.when(datasetTableRepositoryMock.findDatasetTableByEditLockExpirationDateBefore(Mockito.any(Date.class)))
                .thenReturn(datasetTables);

        final List<DatasetTableVO> actualResult = classUnderTest.getDatasetTablesWithExpiredEditingLocks();

        Assert.assertNotNull(actualResult);
        Assert.assertEquals(1, actualResult.size());
        final DatasetTableVO datasetTableVO = actualResult.get(0);
        Assert.assertEquals(Long.valueOf(1L), datasetTableVO.getDatasetId());
        Assert.assertEquals("table", datasetTableVO.getTableName());
    }

    @Test
    public void getDatasetTablesWithExpiredEditingLocks_foundEmptyList() {

        final List<DatasetTable> datasetTables = new ArrayList<>();

        Mockito.when(datasetTableRepositoryMock.findDatasetTableByEditLockExpirationDateBefore(Mockito.any(Date.class)))
                .thenReturn(datasetTables);

        final List<DatasetTableVO> actualResult = classUnderTest.getDatasetTablesWithExpiredEditingLocks();

        Assert.assertNotNull(actualResult);
        Assert.assertEquals(0, actualResult.size());

    }
}