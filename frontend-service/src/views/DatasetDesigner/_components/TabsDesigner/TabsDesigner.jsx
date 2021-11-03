import { Fragment, useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { config } from 'conf';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { FieldsDesigner } from './_components/FieldsDesigner';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { routes } from 'conf/routes';
import { TabView } from 'views/_components/TabView';
import { TabPanel } from 'views/_components/TabView/_components/TabPanel';

import { DatasetService } from 'services/DatasetService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { DatasetDesignerUtils } from 'views/DatasetDesigner/_functions/Utils/DatasetDesignerUtils';
import { QuerystringUtils } from 'views/_functions/Utils/QuerystringUtils';
import { TabsUtils } from 'views/_functions/Utils/TabsUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const TabsDesigner = withRouter(
  ({
    changeMode,
    datasetSchema,
    datasetSchemas,
    datasetStatistics,
    designerState,
    editable = false,
    getIsTableCreated,
    getUpdatedTabs,
    history,
    isDataflowOpen,
    isDesignDatasetEditorRead,
    isGroupedValidationDeleted,
    isGroupedValidationSelected,
    isReferenceDataset,
    manageDialogs,
    manageUniqueConstraint,
    match,
    onChangeReference,
    onHideSelectGroupedValidation,
    onLoadTableData,
    onTabChange,
    onUpdateSchema,
    onUpdateTable,
    selectedRuleId,
    selectedRuleLevelError,
    selectedRuleMessage,
    selectedTableSchemaId,
    setActiveTableSchemaId,
    tableSchemaId,
    viewType
  }) => {
    const {
      params: { dataflowId, datasetId }
    } = match;
    const notificationContext = useContext(NotificationContext);

    const [errorMessage, setErrorMessage] = useState();
    const [errorMessageTitle, setErrorMessageTitle] = useState();
    const [initialTabIndexDrag, setInitialTabIndexDrag] = useState();
    const [isEditing, setIsEditing] = useState(false);
    const [isErrorDialogVisible, setIsErrorDialogVisible] = useState(false);
    const [scrollFn, setScrollFn] = useState();
    const [tabs, setTabs] = useState([]);

    const resourcesContext = useContext(ResourcesContext);

    useEffect(() => {
      if (!isNil(datasetSchema) && !isEmpty(datasetSchema)) {
        onLoadSchema();
      }
    }, [datasetStatistics]);

    useEffect(() => {
      if (!isUndefined(scrollFn) && !isNull(scrollFn) && !isEditing) {
        scrollFn();
      }
    }, [scrollFn, tabs, isEditing]);

    useEffect(() => {
      if (!isEmpty(tabs)) {
        getUpdatedTabs(tabs);
        onUpdateTable(tabs);
        onUpdateSchema(tabs);
      }
    }, [tabs]);

    useEffect(() => {
      if (isErrorDialogVisible) {
        renderErrors(errorMessageTitle, errorMessage);
      }
    }, [isErrorDialogVisible]);

    const onChangeFields = (fields, isLinkChange, tabSchemaId) => {
      const inmTabs = [...tabs];
      const tabIdx = TabsUtils.getIndexByTableProperty(tabSchemaId, inmTabs, 'tableSchemaId');
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

    const onChangeTableProperties = (
      tabSchemaId,
      tableSchemaDescription,
      readOnly,
      toPrefill,
      notEmpty,
      fixedNumber
    ) => {
      const inmTabs = [...tabs];
      const tabIdx = TabsUtils.getIndexByTableProperty(tabSchemaId, inmTabs, 'tableSchemaId');
      inmTabs[tabIdx].description = tableSchemaDescription;
      inmTabs[tabIdx].fixedNumber = fixedNumber;
      inmTabs[tabIdx].notEmpty = notEmpty;
      inmTabs[tabIdx].readOnly = readOnly;
      inmTabs[tabIdx].toPrefill = toPrefill;
      setTabs(inmTabs);
    };

    const onLoadSchema = async () => {
      try {
        setTabs(
          DatasetDesignerUtils.getTabs({
            datasetSchema,
            datasetStatistics,
            editable,
            isDataflowOpen,
            isDesignDatasetEditorRead
          })
        );
      } catch (error) {
        console.error('TabsDesigner - onLoadSchema.', error);
        if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
          history.push(getUrl(routes.DATAFLOWS, true));
        }
      }
    };

    const onTabAdd = (newTabElement, onTabAddCallback) => {
      //Add a temporary Tab with an input text
      if (!checkEditingTabs()) {
        const inmTabs = [...tabs];
        inmTabs.push({ ...newTabElement, index: TabsUtils.getMaxIndex([...tabs]) + 1 });
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
        const newTab = tabs.filter(tab => tab.newTab);
        const filteredTabs = inmTabs.filter(inmTab => inmTab.index !== newTab[0].index);
        setTabs([...filteredTabs]);
      }
    };

    const onTabClicked = event => {
      if (event.header !== '') {
        setActiveTableSchemaId(event.tableSchemaId);
      }
    };

    const onTabEditingHeader = editing => setIsEditing(editing);

    const onTableAdd = (header, tabIndex, initialHeader) => {
      if (header !== initialHeader) {
        if (checkDuplicates(header, tabIndex)) {
          setErrorMessageTitle(resourcesContext.messages['duplicatedTabHeader']);
          setErrorMessage(resourcesContext.messages['duplicatedTabHeaderError']);
          setIsErrorDialogVisible(true);
          return { correct: false, tableName: header };
        } else if (checkInvalidCharacters(header)) {
          setErrorMessageTitle(resourcesContext.messages['invalidCharactersTabHeader']);
          setErrorMessage(resourcesContext.messages['invalidCharactersTabHeaderError']);
          setIsErrorDialogVisible(true);
          return { correct: false, tableName: header };
        } else {
          if (tabs[tabIndex].newTab) {
            addTable(header, tabIndex);
            changeMode('design');
          } else {
            updateTableName(tabs[tabIndex].tableSchemaId, header);
          }
        }
      }
    };

    const onTableDelete = deletedTabTableSchemaId => deleteTable(deletedTabTableSchemaId);

    const onTableDragAndDrop = (draggedTabHeader, droppedTabHeader) => reorderTable(draggedTabHeader, droppedTabHeader);

    const onTableDragAndDropStart = (draggedTabIdx, draggedTabId) => {
      if (!isUndefined(draggedTabId)) {
        setActiveTableSchemaId(draggedTabId);
      } else {
        setActiveTableSchemaId(tabs[0].tableSchemaId);
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
        const { data } = await DatasetService.createTableDesign(datasetId, header);
        const inmTabs = [...tabs];
        inmTabs[tabIndex].tableSchemaId = data.idTableSchema;
        inmTabs[tabIndex].recordId = data.recordSchema.idRecordSchema;
        inmTabs[tabIndex].recordSchemaId = data.recordSchema.idRecordSchema;
        inmTabs[tabIndex].header = header;
        inmTabs[tabIndex].tableSchemaName = header;
        inmTabs[tabIndex].newTab = false;
        inmTabs[tabIndex].showContextMenu = false;
        setActiveTableSchemaId(data.idTableSchema);
        setTabs(inmTabs);
        getIsTableCreated(true);
      } catch (error) {
        console.error('TabsDesigner - addTable.', error);
        if (error?.response.status === 400) {
          if (error.response?.data?.message?.includes('name invalid')) {
            notificationContext.add(
              {
                type: 'DATASET_SCHEMA_TABLE_INVALID_NAME',
                content: { customContent: { tableName: header } }
              },
              true
            );
          }
        }
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
      const repeatedElements = inmTabs.filter(tab => TextUtils.areEquals(header, tab.header));
      return repeatedElements.length > 0 && tabIndex !== repeatedElements[0].index;
    };

    const checkEditingTabs = () => {
      const inmTabs = [...tabs];
      const editingTabs = inmTabs.filter(tab => tab.newTab);
      return editingTabs.length > 0;
    };

    const checkInvalidCharacters = header => {
      const invalidCharsRegex = new RegExp(/[^a-zA-Z0-9_-\s]/);
      return invalidCharsRegex.test(header);
    };

    const deleteTable = async deletedTableSchemaId => {
      try {
        await DatasetService.deleteTableDesign(datasetId, deletedTableSchemaId);
        const inmTabs = [...tabs];
        const deletedTabIndx = TabsUtils.getIndexByTableProperty(deletedTableSchemaId, inmTabs, 'tableSchemaId');
        inmTabs.splice(deletedTabIndx, 1);
        inmTabs.forEach(tab => {
          if (tab.addTab) {
            tab.index = -1;
            tab.tableSchemaId = '';
          }
        });
        if (tableSchemaId === deletedTableSchemaId) {
          setActiveTableSchemaId(inmTabs[0].tableSchemaId);
        }
        onChangeReference(inmTabs, datasetSchema.datasetSchemaId);
        setTabs(inmTabs);
      } catch (error) {
        console.error('TabsDesigner - deleteTable.', error);
      }
    };

    const errorDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button icon="check" label={resourcesContext.messages['ok']} onClick={() => setIsErrorDialogVisible(false)} />
      </div>
    );

    const renderErrors = (errorTitle, error) => {
      return (
        isErrorDialogVisible && (
          <Dialog
            footer={errorDialogFooter}
            header={errorTitle}
            modal={true}
            onHide={() => setIsErrorDialogVisible(false)}
            visible={isErrorDialogVisible}>
            <div className="p-grid p-fluid">{error}</div>
          </Dialog>
        )
      );
    };

    const renderTabViews = () => {
      const idx = TabsUtils.getIndexByTableProperty(
        !isNil(tableSchemaId)
          ? tableSchemaId
          : QuerystringUtils.getUrlParamValue('tab') !== ''
          ? QuerystringUtils.getUrlParamValue('tab')
          : 0,
        tabs,
        'tableSchemaId'
      );
      return (
        <TabView
          activeIndex={idx !== -1 ? idx : 0}
          checkEditingTabs={checkEditingTabs}
          designMode={true}
          history={history}
          initialTabIndexDrag={initialTabIndexDrag}
          isDataflowOpen={isDataflowOpen}
          isDesignDatasetEditorRead={isDesignDatasetEditorRead}
          isErrorDialogVisible={isErrorDialogVisible}
          name="TabsDesigner"
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
          tableSchemaId={tableSchemaId}
          tabs={tabs}
          totalTabs={tabs.length}
          viewType={viewType}>
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
                    rightIcon={tab.hasErrors ? config.icons['warning'] : null}
                    tableSchemaId={tab.tableSchemaId}>
                    {(tabs.length > 0 && (isDataflowOpen || isDesignDatasetEditorRead)) || tabs.length > 1 ? (
                      <FieldsDesigner
                        autoFocus={false}
                        dataflowId={dataflowId}
                        datasetId={datasetId}
                        datasetSchemaId={datasetSchema.datasetSchemaId}
                        datasetSchemas={datasetSchemas}
                        designerState={designerState}
                        hasPKReferenced={tab.hasPKReferenced}
                        isDataflowOpen={isDataflowOpen}
                        isDesignDatasetEditorRead={isDesignDatasetEditorRead}
                        isGroupedValidationDeleted={isGroupedValidationDeleted}
                        isGroupedValidationSelected={isGroupedValidationSelected}
                        isReferenceDataset={isReferenceDataset}
                        key={tab.index}
                        manageDialogs={manageDialogs}
                        manageUniqueConstraint={manageUniqueConstraint}
                        onChangeFields={onChangeFields}
                        onChangeReference={onChangeReference}
                        onChangeTableProperties={onChangeTableProperties}
                        onHideSelectGroupedValidation={onHideSelectGroupedValidation}
                        onLoadTableData={onLoadTableData}
                        selectedRuleId={selectedRuleId}
                        selectedRuleLevelError={selectedRuleLevelError}
                        selectedRuleMessage={selectedRuleMessage}
                        selectedTableSchemaId={selectedTableSchemaId}
                        table={tabs[i]}
                        viewType={viewType}
                      />
                    ) : (
                      <h3>{`${resourcesContext.messages['datasetDesignerAddTable']}`}</h3>
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
        const draggedTabIdx = TabsUtils.getIndexByHeader(draggedTabHeader, inmTabs);
        const droppedTabIdx = TabsUtils.getIndexByHeader(droppedTabHeader, inmTabs);
        await DatasetService.updateTableOrder(
          datasetId,
          draggedTabIdx > droppedTabIdx ? droppedTabIdx : droppedTabIdx - 1,
          tabs[draggedTabIdx].tableSchemaId
        );

        const shiftedTabs = arrayShift(inmTabs, draggedTabIdx, droppedTabIdx);

        shiftedTabs.forEach((tab, i) => (tab.index = !tab.addTab ? i : -1));
        setActiveTableSchemaId(
          shiftedTabs[draggedTabIdx > droppedTabIdx ? droppedTabIdx : droppedTabIdx - 1].tableSchemaId
        );
        setTabs([...shiftedTabs]);
      } catch (error) {
        console.error('TabsDesigner - reorderTable.', error);
      }
    };

    const updateTableName = async (tableSchemaId, tableSchemaName) => {
      try {
        await DatasetService.updateTableNameDesign(tableSchemaId, tableSchemaName, datasetId);
        const inmTabs = [...tabs];
        inmTabs[TabsUtils.getIndexByTableProperty(tableSchemaId, inmTabs, 'tableSchemaId')].header = tableSchemaName;
        inmTabs[TabsUtils.getIndexByTableProperty(tableSchemaId, inmTabs, 'tableSchemaId')].tableSchemaName =
          tableSchemaName;
        setTabs(inmTabs);
      } catch (error) {
        console.error('TabsDesigner - updateTableName.', error);
        if (error?.response.status === 400) {
          if (error.response?.data?.message?.includes('name invalid')) {
            notificationContext.add(
              {
                type: 'DATASET_SCHEMA_TABLE_INVALID_NAME',
                content: { customContent: { tableName: tableSchemaName } }
              },
              true
            );
          }
        }
      }
    };

    return (
      <Fragment>
        {renderTabViews()}
        {renderErrors(errorMessageTitle, errorMessage)}
      </Fragment>
    );
  }
);
