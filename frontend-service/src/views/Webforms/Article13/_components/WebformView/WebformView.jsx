import { useContext, useEffect, useReducer } from 'react';

import first from 'lodash/first';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import keys from 'lodash/keys';
import pickBy from 'lodash/pickBy';
import uniq from 'lodash/uniq';
import uniqBy from 'lodash/uniqBy';
import uniqueId from 'lodash/uniqueId';

import styles from './WebformView.module.scss';

import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Spinner } from 'views/_components/Spinner';
import { Toolbar } from 'views/_components/Toolbar';
import { WebformTable } from 'views/Webforms/_components/WebformTable';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { WebformService } from 'services/WebformService';

import { webformViewReducer } from './_functions/Reducers/webformViewReducer';

import { WebformsUtils } from 'views/Webforms/_functions/Utils/WebformsUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const WebformView = ({
  data,
  dataProviderId,
  dataflowId,
  datasetId,
  datasetSchemaId,
  getFieldSchemaId,
  isAddingPamsId = false,
  isRefresh,
  isReporting,
  onUpdatePamsValue,
  pamsRecords,
  selectedTable,
  selectedTableName,
  setTableSchemaId,
  state,
  tables
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const tableSchemaNames = state.schemaTables.map(table => table.name);
  const { getWebformTabs } = WebformsUtils;

  const [webformViewState, webformViewDispatch] = useReducer(webformViewReducer, {
    isLoading: false,
    isVisible: getWebformTabs(
      tables.map(table => table.name),
      state.schemaTables,
      tables,
      selectedTableName
    ),
    singlesCalculatedData: []
  });

  const { isLoading, isVisible, singlesCalculatedData } = webformViewState;

  useEffect(() => {
    const visibleTable = Object.keys(isVisible).filter(key => isVisible[key])[0];
    const visibleTableId = data.filter(table => table.name === visibleTable)[0].tableSchemaId;

    setTableSchemaId(visibleTableId);
  }, [webformViewState.isVisible]);

  useEffect(() => {
    if (!isNil(selectedTableName)) {
      onChangeWebformTab(selectedTableName);
    }
  }, [selectedTableName]);

  useEffect(() => {
    getSingleData();
  }, [datasetId, selectedTable.pamsId]);

  const getSingleData = async () => {
    try {
      const singleData = await WebformService.getSinglePamData(datasetId, selectedTable.pamsId);
      webformViewDispatch({ type: 'SET_SINGLE_CALCULATED_DATA', payload: singleData.data });
    } catch (error) {
      console.error('WebformView - getSingleData.', error);
    }
  };

  const calculateSingle = field => {
    let fields = [];
    switch (field.name.toLowerCase()) {
      case 'unionpolicyother':
        fields = combinationFieldRender('otherUnionPolicy');
        return <ul>{fields?.map(field => !isEmpty(field) && <li key={uniqueId()}>{field}</li>)}</ul>;
      case 'ghgaffected':
      case 'sectoraffected':
      case 'policyinstrument':
      case 'policyimpacting':
      case 'otherpolicyinstrument':
        fields = combinationFieldRender(field.name);
        return <ul>{fields?.map(field => !isEmpty(field) && <li key={uniqueId()}>{field}</li>)}</ul>;
      case 'pamnames':
        fields = combinationFieldRender('paMName', 'id');
        return <ul>{fields?.map(field => !isEmpty(field) && <li key={uniqueId()}>{field}</li>)}</ul>;
      case 'statusimplementation':
        return tableFieldRender(field.name, [
          'implementationperiodstart',
          'implementationperiodfinish',
          'implementationperiodcomment'
        ]);
      case 'projectionsscenario':
      case 'typepolicyinstrument':
      case 'unionpolicylist':
        return tableFieldRender(field.name, []);
      case 'entities':
        return combinationTableRender('entities');
      case 'sectorobjectives':
        return combinationTableRender('sectors');
      default:
        break;
    }

    return <span disabled={true}>{field.value}</span>;
  };

  const combinationFieldRender = (fieldName, previousField = '', separator = '-') => {
    const combinatedValues = [];
    singlesCalculatedData.forEach(singleRecord => {
      let previousFieldValue = '';
      if (previousField !== '') {
        previousFieldValue =
          singleRecord[Object.keys(singleRecord).find(key => key.toLowerCase() === previousField.toLowerCase())];
      }
      const singleRecordValue =
        singleRecord[Object.keys(singleRecord).find(key => key.toLowerCase() === fieldName.toLowerCase())];
      if (!isNil(singleRecordValue)) {
        combinatedValues.push(
          Array.isArray(singleRecordValue)
            ? singleRecordValue
                .map(value => (previousFieldValue !== '' ? `${previousFieldValue} ${separator} ${value}` : value))
                .filter(value => !isNil(value))
                .join(', ')
            : previousFieldValue !== ''
            ? `${previousFieldValue} ${separator} ${singleRecordValue}`
            : singleRecordValue
        );
      }
    });

    return uniq(combinatedValues);
  };

  const combinationTableRender = tableName => {
    const combinatedTableValues = [];

    singlesCalculatedData.forEach(singleRecord => {
      const filteredValue = first(
        singleRecord[Object.keys(singleRecord).find(key => key.toLowerCase() === tableName.toLowerCase())]
      );
      if (!isNil(filteredValue) && !isEmpty(filteredValue)) {
        const singleRecordValue = {
          pamsId: singleRecord['id'],
          pamName: singleRecord['paMName'],
          ...filteredValue
        };
        combinatedTableValues.push(singleRecordValue);
      }
    });

    return renderTable(uniqBy(combinatedTableValues, value => JSON.stringify(value)));
  };

  const isGroup = () => {
    const filteredField = pamsRecords
      .find(pamRecord => pamRecord.recordId === selectedTable.recordId)
      ?.elements.find(element => TextUtils.areEquals(element.name, 'IsGroup'));
    return TextUtils.areEquals(filteredField?.value, 'Group');
  };

  const setIsLoading = value => webformViewDispatch({ type: 'SET_IS_LOADING', payload: { value } });

  const renderColumns = fields =>
    !isNil(fields[0]) &&
    Object.keys(fields[0]).map(field => (
      <Column
        columnResizeMode="expand"
        field={field}
        filter={true}
        filterMatchMode="contains"
        header={resourcesContext.messages[field]}
        key={field}
        sortable={true}
      />
    ));

  const tableFieldRender = (fieldName, columnFields) => {
    const combinatedTableValues = [];
    singlesCalculatedData.forEach(singleRecord => {
      const singleRecordValue =
        singleRecord[Object.keys(singleRecord).find(key => key.toLowerCase() === fieldName.toLowerCase())];
      if (!isNil(singleRecordValue)) {
        const columnFieldsValues = {
          pamsId: singleRecord['id'],
          pamName: singleRecord['paMName'],
          [fieldName]: singleRecordValue
        };
        columnFields.forEach(columnField => {
          const columnFieldValue =
            singleRecord[Object.keys(singleRecord).find(key => key.toLowerCase() === columnField.toLowerCase())];
          if (!isNil(columnFieldValue)) {
            columnFieldsValues[columnField] = columnFieldValue;
          }
        });
        combinatedTableValues.push(columnFieldsValues);
      }
    });
    return renderTable(combinatedTableValues);
  };

  const renderTable = fields => <DataTable value={fields}>{renderColumns(fields)}</DataTable>;

  const onChangeWebformTab = name => {
    Object.keys(isVisible).forEach(tab => {
      isVisible[tab] = false;
      isVisible[name] = true;
    });

    webformViewDispatch({ type: 'ON_CHANGE_TAB', payload: { isVisible } });
  };

  const onUpdateSinglesList = () => {
    getSingleData();
  };

  const renderWebFormHeaders = () => {
    const filteredTabs = data.filter(header => tableSchemaNames.includes(header.name));
    const headers = filteredTabs.map(tab => tab.header || tab.name);
    return data
      .filter(table => table.isVisible)
      .map(webform => {
        const isCreated = headers.includes(webform.name);
        return (
          <Button
            className={`${styles.headerButton} ${isVisible[webform.name] ? 'p-button-primary' : 'p-button-secondary'}`}
            disabled={isLoading}
            icon={!isCreated ? 'info' : 'table'}
            iconClasses={!isVisible[webform.title] ? 'info' : ''}
            iconPos={!isCreated ? 'right' : 'left'}
            key={uniqueId()}
            label={webform.label}
            onClick={() => onChangeWebformTab(webform.name)}
            style={{ display: isReporting && !isCreated ? 'none' : '' }}
          />
        );
      });
  };

  const renderWebFormContent = () => {
    const visibleTitle = keys(pickBy(isVisible))[0];
    const visibleContent = data.filter(table => table.name === visibleTitle && table.isVisible)[0];

    return (
      <WebformTable
        calculateSingle={calculateSingle}
        dataflowId={dataflowId}
        dataProviderId={dataProviderId}
        datasetId={datasetId}
        datasetSchemaId={datasetSchemaId}
        getFieldSchemaId={getFieldSchemaId}
        isGroup={isGroup}
        isRefresh={isRefresh}
        isReporting={isReporting}
        onTabChange={isVisible}
        onUpdatePamsValue={onUpdatePamsValue}
        onUpdateSinglesList={onUpdateSinglesList}
        pamsRecords={pamsRecords}
        selectedTable={selectedTable}
        setIsLoading={setIsLoading}
        webform={visibleContent}
        webformType={'ARTICLE_13'}
      />
    );
  };

  if (isAddingPamsId) return <Spinner style={{ top: 0, marginBottom: '2rem' }} />;

  return (
    <div className={styles.webform}>
      <Toolbar className={styles.toolbar}>
        <div className="p-toolbar-group-left">{renderWebFormHeaders()}</div>
      </Toolbar>
      {renderWebFormContent()}
    </div>
  );
};
