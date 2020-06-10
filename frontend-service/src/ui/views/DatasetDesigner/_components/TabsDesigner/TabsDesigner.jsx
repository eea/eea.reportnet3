import React, { useContext, useState, useEffect } from 'react';
import { withRouter } from 'react-router-dom';

import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { FieldsDesigner } from './_components/FieldsDesigner';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabView } from 'ui/views/_components/TabView';
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel';
import { Validations } from 'ui/views/DatasetDesigner/_components/Validations';

import { DatasetService } from 'core/services/Dataset';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { ValidationContext } from 'ui/views/_functions/Contexts/ValidationContext';
import isEmpty from 'lodash/isEmpty';

export const TabsDesigner = withRouter(
  ({
    activeIndex = 0,
    datasetSchemaDTO,
    datasetSchemas,
    datasetStatistics,
    editable = false,
    history,
    isPreviewModeOn,
    isValidationSelected,
    manageDialogs,
    manageUniqueConstraint,
    match,
    onChangeReference,
    onLoadTableData,
    onTabChange,
    onUpdateTable,
    recordPositionId,
    selectedRecordErrorId,
    setActiveIndex,
    setIsValidationSelected
  }) => {
    const {
      params: { dataflowId, datasetId }
    } = match;

    const leftSideBarContext = useContext(LeftSideBarContext);
    const validationContext = useContext(ValidationContext);

    // const [activeIndex, setActiveIndex] = useState(activeIdx);

    const [datasetSchema, setDatasetSchema] = useState(datasetSchemaDTO);
    const [errorMessage, setErrorMessage] = useState();
    const [errorMessageTitle, setErrorMessageTitle] = useState();
    const [initialTabIndexDrag, setInitialTabIndexDrag] = useState();
    const [isEditing, setIsEditing] = useState(false);
    const [isErrorDialogVisible, setIsErrorDialogVisible] = useState(false);
    const [scrollFn, setScrollFn] = useState();
    const [tabs, setTabs] = useState([]);

    const resources = useContext(ResourcesContext);

    useEffect(() => {
      if (!isNil(datasetSchemaDTO) && !isEmpty(datasetSchemaDTO)) {
        onLoadSchema();
      }
    }, [datasetStatistics]);

    useEffect(() => {
      if (!isUndefined(scrollFn) && !isNull(scrollFn) && !isEditing) {
        scrollFn();
      }
    }, [scrollFn, tabs, isEditing]);

    useEffect(() => {
      onUpdateTable(tabs);
    }, [tabs]);

    useEffect(() => {
      if (!isUndefined(datasetSchema) && !isEmpty(datasetSchema)) {
        setTabs(datasetSchema.tables);
      }
    }, [datasetSchema]);

    useEffect(() => {
      if (isErrorDialogVisible) {
        renderErrors(errorMessageTitle, errorMessage);
      }
    }, [isErrorDialogVisible]);

    const onChangeFields = (fields, isLinkChange, tableSchemaId) => {
      const inmTabs = [...tabs];
      const tabIdx = getIndexByTableSchemaId(tableSchemaId, inmTabs);
      if (!isNil(inmTabs[tabIdx].records)) {
        inmTabs[tabIdx].records[0].fields = fields;
        setTabs(inmTabs);
      } else {
        inmTabs[tabIdx].records = [];
        inmTabs[tabIdx].records[0] = {};
        inmTabs[tabIdx].records[0].fields = fields;
      }
      if (isLinkChange) {
        onChangeReference(inmTabs, datasetSchema.datasetSchemaId);
      }
    };

    const onChangeTableProperties = (tableSchemaId, tableSchemaDescription, readOnly, toPrefill) => {
      const inmTabs = [...tabs];
      const tabIdx = getIndexByTableSchemaId(tableSchemaId, inmTabs);
      inmTabs[tabIdx].description = tableSchemaDescription;
      inmTabs[tabIdx].readOnly = readOnly;
      inmTabs[tabIdx].toPrefill = toPrefill;
      setTabs(inmTabs);
    };

    const onLoadSchema = async () => {
      try {
        const inmDatasetSchema = { ...datasetSchemaDTO };

        inmDatasetSchema.tables.forEach((table, idx) => {
          table.addTab = false;
          table.toPrefill = table.tableSchemaToPrefill;
          table.description = table.tableSchemaDescription;
          table.editable = editable;
          table.hasErrors =
            !isNil(datasetStatistics) && !isEmpty(datasetStatistics)
              ? {
                  ...datasetStatistics.tables.filter(tab => tab['tableSchemaId'] === table['tableSchemaId'])[0]
                }.hasErrors
              : false;
          table.header = table.tableSchemaName;
          table.index = idx;
          table.levelErrorTypes = inmDatasetSchema.levelErrorTypes;
          table.newTab = false;
          table.readOnly = table.tableSchemaReadOnly;
          table.showContextMenu = false;
        });
        //Add tab Button/Tab
        inmDatasetSchema.tables.push({ header: '+', editable: false, addTab: true, newTab: false, index: -1 });
        setDatasetSchema(inmDatasetSchema);
      } catch (error) {
        console.error(`Error while loading schema ${error}`);
        if (error.response.status === 401 || error.response.status === 403) {
          history.push(getUrl(routes.DATAFLOWS, true));
        }
      } finally {
        // setIsLoading(false);
      }
    };

    const onTabAdd = (newTabElement, onTabAddCallback) => {
      //Add a temporary Tab with an input text
      if (!checkEditingTabs()) {
        const inmTabs = [...tabs];
        inmTabs.push({ ...newTabElement, index: getMaxIndex([...tabs]) + 1 });
        [inmTabs[inmTabs.length - 1], inmTabs[inmTabs.length - 2]] = [
          inmTabs[inmTabs.length - 2],
          inmTabs[inmTabs.length - 1]
        ];
        setScrollFn(() => onTabAddCallback);
        setTabs(inmTabs);
      }
    };

    const onTabAddCancel = () => {
      if (!isErrorDialogVisible) {
        const inmTabs = [...tabs];
        const newTab = tabs.filter(tab => tab.newTab === true);
        const filteredTabs = inmTabs.filter(inmTab => inmTab.index !== newTab[0].index);
        setTabs([...filteredTabs]);
      }
    };

    const onTabClicked = event => {
      if (event.header !== '') {
        setActiveIndex(event.index);
        setIsValidationSelected(false);
      }
    };

    const onTabEditingHeader = editing => setIsEditing(editing);

    const onTableAdd = (header, tabIndex, initialHeader) => {
      if (header !== initialHeader) {
        if (checkDuplicates(header, tabIndex)) {
          setErrorMessageTitle(resources.messages['duplicatedTabHeader']);
          setErrorMessage(resources.messages['duplicatedTabHeaderError']);
          setIsErrorDialogVisible(true);
          return { correct: false, tableName: header };
        } else {
          if (tabs[tabIndex].newTab) {
            addTable(header, tabIndex);
          } else {
            updateTableName(tabs[tabIndex].tableSchemaId, header);
          }
        }
      }
    };

    const onTableDelete = deletedTabIndx => deleteTable(deletedTabIndx);

    const onTableDragAndDrop = (draggedTabHeader, droppedTabHeader) => reorderTable(draggedTabHeader, droppedTabHeader);

    const onTableDragAndDropStart = draggedTabIdx => {
      if (!isUndefined(draggedTabIdx)) {
        setActiveIndex(draggedTabIdx);
      } else {
        setActiveIndex(0);
      }
      setInitialTabIndexDrag(draggedTabIdx);
    };

    const onTabNameError = (errorTitle, error) => {
      setErrorMessageTitle(errorTitle);
      setErrorMessage(error);
      setIsErrorDialogVisible(true);
    };

    const addTable = async (header, tabIndex) => {
      try {
        const response = await DatasetService.addTableDesign(datasetId, header);
        if (response.status < 200 || response.status > 299) {
          console.error('Error during table Add');
        } else {
          const inmTabs = [...tabs];
          inmTabs[tabIndex].tableSchemaId = response.data.idTableSchema;
          inmTabs[tabIndex].recordId = response.data.recordSchema.idRecordSchema;
          inmTabs[tabIndex].header = header;
          inmTabs[tabIndex].tableSchemaName = header;
          inmTabs[tabIndex].newTab = false;
          inmTabs[tabIndex].showContextMenu = false;
          setActiveIndex(inmTabs.length - 2);
          setTabs(inmTabs);
        }
      } catch (error) {
        console.error('Error during field Add: ', error);
      } finally {
      }
    };

    const arrayShift = (arr, initialIdx, endIdx) => {
      const element = arr[initialIdx];
      if (Math.abs(endIdx - initialIdx) > 1) {
        arr.splice(initialIdx, 1);
        if (initialIdx < endIdx) {
          arr.splice(endIdx - 1, 0, element);
        } else {
          arr.splice(endIdx, 0, element);
        }
      } else {
        if (endIdx === 0) {
          arr.splice(initialIdx, 1);
          arr.splice(0, 0, element);
        } else {
          arr.splice(initialIdx, 1);
          if (initialIdx < endIdx) {
            arr.splice(endIdx - 1, 0, element);
          } else {
            arr.splice(endIdx, 0, element);
          }
        }
      }
      return arr;
    };

    const checkDuplicates = (header, tabIndex) => {
      const inmTabs = [...tabs];
      const repeatedElements = inmTabs.filter(tab => header.toLowerCase() === tab.header.toLowerCase());
      return repeatedElements.length > 0 && tabIndex !== repeatedElements[0].index;
    };

    const checkEditingTabs = () => {
      const inmTabs = [...tabs];
      const editingTabs = inmTabs.filter(tab => tab.newTab === true);
      return editingTabs.length > 0;
    };

    const deleteTable = async deletedTabIndx => {
      const tableDeleted = await DatasetService.deleteTableDesign(datasetId, tabs[deletedTabIndx].tableSchemaId);
      if (tableDeleted) {
        const inmTabs = [...tabs];
        inmTabs.splice(deletedTabIndx, 1);
        inmTabs.forEach((tab, i) => (tab.index = !tab.addTab ? i : -1));
        if (activeIndex === deletedTabIndx) {
          setActiveIndex(0);
        } else {
          if (deletedTabIndx === inmTabs.length - 2) {
            setActiveIndex(inmTabs.length - 2);
          } else {
            if (activeIndex !== 0) {
              setActiveIndex(activeIndex - 1);
            }
          }
        }
        onChangeReference(inmTabs, datasetSchema.datasetSchemaId);
        setTabs(inmTabs);
      } else {
        console.error('There has been an error while deleting the tab');
      }
    };

    const errorDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button
          label={resources.messages['ok']}
          icon="check"
          onClick={() => {
            setIsErrorDialogVisible(false);
          }}
        />
      </div>
    );

    const getIndexByHeader = (header, tabsArray) => {
      return tabsArray
        .map(tab => {
          return tab.header;
        })
        .indexOf(header);
    };

    const getIndexByTableSchemaId = (tableSchemaId, tabsArray) => {
      return tabsArray
        .map(tab => {
          return tab.tableSchemaId;
        })
        .indexOf(tableSchemaId);
    };

    const getMaxIndex = tabsArray => {
      return Math.max(...tabsArray.map(tab => tab.index));
    };

    // const getSchemaIndexById = (datasetSchemaId, datasetSchemasArray) => {
    //   return datasetSchemasArray
    //     .map(datasetSchema => {
    //       return datasetSchema.datasetSchemaId;
    //     })
    //     .indexOf(datasetSchemaId);
    // };

    const renderErrors = (errorTitle, error) => {
      return (
        <Dialog
          footer={errorDialogFooter}
          header={errorTitle}
          modal={true}
          onHide={() => setIsErrorDialogVisible(false)}
          visible={isErrorDialogVisible}>
          <div className="p-grid p-fluid">{error}</div>
        </Dialog>
      );
    };

    const renderTabViews = () => {
      return (
        <TabView
          activeIndex={activeIndex}
          checkEditingTabs={checkEditingTabs}
          designMode={true}
          history={history}
          initialTabIndexDrag={initialTabIndexDrag}
          isErrorDialogVisible={isErrorDialogVisible}
          isPreviewModeOn={isPreviewModeOn}
          onTabAdd={onTabAdd}
          onTabAddCancel={onTabAddCancel}
          onTabBlur={onTableAdd}
          onTabChange={onTabChange}
          onTabClick={onTabClicked}
          onTabConfirmDelete={onTableDelete}
          onTabDragAndDrop={onTableDragAndDrop}
          onTabDragAndDropStart={onTableDragAndDropStart}
          onTabEditingHeader={onTabEditingHeader}
          onTabNameError={onTabNameError}
          totalTabs={tabs.length}>
          {tabs.length > 0
            ? tabs.map((tab, i) => {
                return (
                  <TabPanel
                    addTab={tab.addTab}
                    editable={tab.editable}
                    hasPKReferenced={tab.hasPKReferenced}
                    header={tab.header}
                    index={tab.index}
                    key={tab.index}
                    newTab={tab.newTab}
                    rightIcon={tab.hasErrors ? config.icons['warning'] : null}>
                    {tabs.length > 1 ? (
                      <FieldsDesigner
                        activeIndex={activeIndex}
                        autoFocus={false}
                        dataflowId={dataflowId}
                        datasetId={datasetId}
                        datasetSchemaId={datasetSchema.datasetSchemaId}
                        datasetSchemas={datasetSchemas}
                        isPreviewModeOn={isPreviewModeOn}
                        isValidationSelected={isValidationSelected}
                        key={tab.index}
                        manageDialogs={manageDialogs}
                        manageUniqueConstraint={manageUniqueConstraint}
                        onChangeFields={onChangeFields}
                        onChangeReference={onChangeReference}
                        onChangeTableProperties={onChangeTableProperties}
                        onLoadTableData={onLoadTableData}
                        recordPositionId={
                          !isNaN(activeIndex)
                            ? tab.index === activeIndex
                              ? recordPositionId
                              : -1
                            : tab.tableSchemaId === activeIndex
                            ? recordPositionId
                            : -1
                        }
                        selectedRecordErrorId={
                          !isNaN(activeIndex)
                            ? tab.index === activeIndex
                              ? selectedRecordErrorId
                              : -1
                            : tab.tableSchemaId === activeIndex
                            ? selectedRecordErrorId
                            : -1
                        }
                        setIsValidationSelected={setIsValidationSelected}
                        table={tabs[i]}
                      />
                    ) : (
                      <h3>{`${resources.messages['datasetDesignerAddTable']}`}</h3>
                    )}
                  </TabPanel>
                );
              })
            : null}
        </TabView>
      );
    };

    const reorderTable = async (draggedTabHeader, droppedTabHeader) => {
      try {
        const inmTabs = [...tabs];
        const draggedTabIdx = getIndexByHeader(draggedTabHeader, inmTabs);
        const droppedTabIdx = getIndexByHeader(droppedTabHeader, inmTabs);
        const tableOrdered = await DatasetService.orderTableDesign(
          datasetId,
          draggedTabIdx > droppedTabIdx ? droppedTabIdx : droppedTabIdx - 1,
          tabs[draggedTabIdx].tableSchemaId
        );
        if (tableOrdered) {
          const shiftedTabs = arrayShift(inmTabs, draggedTabIdx, droppedTabIdx);

          shiftedTabs.forEach((tab, i) => (tab.index = !tab.addTab ? i : -1));
          setActiveIndex(draggedTabIdx > droppedTabIdx ? droppedTabIdx : droppedTabIdx - 1);
          setTabs([...shiftedTabs]);
        }
      } catch (error) {
        console.error(`There has been an error while ordering tables ${error}`);
      } finally {
      }
    };

    const updateTableName = async (tableSchemaId, tableSchemaName) => {
      const tableUpdated = await DatasetService.updateTableNameDesign(tableSchemaId, tableSchemaName, datasetId);
      if (tableUpdated) {
        const inmTabs = [...tabs];
        inmTabs[getIndexByTableSchemaId(tableSchemaId, inmTabs)].header = tableSchemaName;
        inmTabs[getIndexByTableSchemaId(tableSchemaId, inmTabs)].tableSchemaName = tableSchemaName;
        setTabs(inmTabs);
      }
    };

    return (
      <React.Fragment>
        {renderTabViews()}
        {renderErrors(errorMessageTitle, errorMessage)}
        {datasetSchema && tabs && validationContext.isVisible && (
          <Validations
            datasetSchema={datasetSchema}
            datasetSchemas={datasetSchemas}
            tabs={tabs}
            datasetId={datasetId}
          />
        )}
      </React.Fragment>
    );
  }
);
