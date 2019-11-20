import React, { useContext, useState, useEffect } from 'react';
import { withRouter } from 'react-router-dom';
import { isUndefined, isNull } from 'lodash';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { FieldsDesigner } from './_components/FieldsDesigner';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { routes } from 'ui/routes';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabView } from 'ui/views/_components/TabView';
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel';

import { DatasetService } from 'core/services/DataSet';

export const TabsDesigner = withRouter(({ editable = false, match, history }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;

  const [activeIndex, setActiveIndex] = useState(0);
  const [datasetSchema, setDatasetSchema] = useState();
  const [errorMessage, setErrorMessage] = useState();
  const [errorMessageTitle, setErrorMessageTitle] = useState();
  const [initialTabIndexDrag, setInitialTabIndexDrag] = useState();
  const [isEditing, setIsEditing] = useState(false);
  const [isErrorDialogVisible, setIsErrorDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedTabId, setSelectedTabId] = useState();
  const [scrollFn, setScrollFn] = useState();
  const [tabs, setTabs] = useState([]);

  const resources = useContext(ResourcesContext);

  useEffect(() => {
    onLoadSchema(datasetId);
  }, []);

  useEffect(() => {
    if (!isUndefined(scrollFn) && !isNull(scrollFn) && !isEditing) {
      scrollFn();
    }
    if (!isUndefined(selectedTabId)) {
      if (!checkExistsHeader()) {
        setActiveIndex(0);
      } else {
        setActiveIndex(getIndexByHeader(selectedTabId, tabs));
      }
    } else {
      if (tabs.length > 0) {
        setSelectedTabId(tabs[0].header);
      }
    }
  }, [scrollFn, tabs, isEditing]);

  useEffect(() => {
    if (!isUndefined(datasetSchema)) {
      setTabs(datasetSchema.tables);
    }
  }, [datasetSchema]);

  useEffect(() => {
    if (isErrorDialogVisible) {
      renderErrors(errorMessageTitle, errorMessage);
    }
  }, [isErrorDialogVisible]);

  const onChangeFields = (fields, tableSchemaId) => {
    const inmTabs = [...tabs];
    const tabIdx = getIndexByTableSchemaId(tableSchemaId, inmTabs);
    if (!isUndefined(inmTabs[tabIdx].records) && !isNull(inmTabs[tabIdx].records)) {
      inmTabs[tabIdx].records[0].fields = fields;
      setTabs(inmTabs);
    }
  };

  const onLoadSchema = async datasetId => {
    try {
      setIsLoading(true);
      const datasetSchemaDTO = await DatasetService.schemaById(datasetId);
      const inmDatasetSchema = { ...datasetSchemaDTO };
      inmDatasetSchema.tables.forEach((table, idx) => {
        table.editable = editable;
        table.addTab = false;
        table.newTab = false;
        table.index = idx;
        table.showContextMenu = false;
        table.header = table.tableSchemaName;
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
      setIsLoading(false);
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
    setSelectedTabId(event.header);
    setActiveIndex(getIndexByHeader(event.header, tabs));
  };

  const onTabEditingHeader = editing => {
    setIsEditing(editing);
  };

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

  const onTableDelete = deletedTabIndx => {
    deleteTable(deletedTabIndx);
  };

  const onTableDragAndDrop = (draggedTabHeader, droppedTabHeader) => {
    reorderTable(draggedTabHeader, droppedTabHeader);
  };

  const onTableDragAndDropStart = (draggedTabIdx, selected, header) => {
    setInitialTabIndexDrag(draggedTabIdx);
    if (selected) {
      setSelectedTabId(header);
    }
  };

  const onTabNameError = (errorTitle, error) => {
    setErrorMessageTitle(errorTitle);
    setErrorMessage(error);
    setIsErrorDialogVisible(true);
  };

  const addTable = async (header, tabIndex) => {
    const tabledAdded = await DatasetService.addTableDesign(datasetId, header);
    if (tabledAdded) {
      onLoadSchema(datasetId);
    } else {
      console.error('');
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
    const repeteadElements = inmTabs.filter(tab => header.toLowerCase() === tab.header.toLowerCase());
    return repeteadElements.length > 0 && tabIndex !== repeteadElements[0].index;
  };

  const checkEditingTabs = () => {
    const inmTabs = [...tabs];
    const editingTabs = inmTabs.filter(tab => tab.newTab === true);
    return editingTabs.length > 0;
  };

  const checkExistsHeader = () => {
    const inmTabs = [...tabs];
    const tabsByHeader = inmTabs.filter(tab => tab.header === selectedTabId);
    return tabsByHeader.length > 0;
  };

  const deleteTable = async deletedTabIndx => {
    const tableDeleted = await DatasetService.deleteTableDesign(datasetId, tabs[deletedTabIndx].tableSchemaId);
    if (tableDeleted) {
      const inmTabs = [...tabs];
      inmTabs.splice(deletedTabIndx, 1);
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
    if (isLoading) {
      return <Spinner />;
    } else {
      return (
        <TabView
          activeIndex={activeIndex}
          checkEditingTabs={checkEditingTabs}
          designMode={true}
          initialTabIndexDrag={initialTabIndexDrag}
          initialTabIndexSelected={
            getIndexByHeader(selectedTabId, tabs) === -1 ? 0 : getIndexByHeader(selectedTabId, tabs)
          }
          isErrorDialogVisible={isErrorDialogVisible}
          onTabAdd={onTabAdd}
          onTabBlur={onTableAdd}
          onTabAddCancel={onTabAddCancel}
          onTabConfirmDelete={onTableDelete}
          onTabClick={onTabClicked}
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
                    header={tab.header}
                    index={tab.index}
                    key={tab.index}
                    newTab={tab.newTab}>
                    {tabs.length > 1 ? (
                      <FieldsDesigner
                        autoFocus={false}
                        dataflowId={dataflowId}
                        datasetId={datasetId}
                        datasetSchemaId={datasetSchema.datasetSchemaId}
                        key={tab.index}
                        onChangeFields={onChangeFields}
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
    }
  };

  const reorderTable = async (draggedTabHeader, droppedTabHeader) => {
    try {
      const inmTabs = [...tabs];
      const draggedTabIdx = getIndexByHeader(draggedTabHeader, inmTabs);
      const droppedTabIdx = getIndexByHeader(droppedTabHeader, inmTabs);

      const tableOrdered = await DatasetService.orderTableDesign(
        datasetId,
        droppedTabIdx,
        tabs[draggedTabIdx].tableSchemaId
      );
      if (tableOrdered) {
        const shiftedTabs = arrayShift(inmTabs, draggedTabIdx, droppedTabIdx);
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
      setSelectedTabId(tableSchemaName);
      const inmTabs = [...tabs];
      inmTabs[getIndexByTableSchemaId(tableSchemaId, inmTabs)].header = tableSchemaName;
      setTabs(inmTabs);
      // onLoadSchema(datasetId);
    }
  };

  return (
    <React.Fragment>
      {renderTabViews()}
      {renderErrors(errorMessageTitle, errorMessage)}
    </React.Fragment>
  );
});
