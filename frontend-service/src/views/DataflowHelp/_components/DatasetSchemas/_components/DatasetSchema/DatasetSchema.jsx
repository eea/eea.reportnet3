import { useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import uniqueId from 'lodash/uniqueId';

import styles from './DatasetSchema.module.scss';

import { config } from 'conf';

import { Accordion, AccordionTab } from 'primereact/accordion';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { DatasetSchemaTable } from './_components/DatasetSchemaTable';
import { DownloadFile } from 'views/_components/DownloadFile';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import ReactTooltip from 'react-tooltip';
import { TabPanel } from 'views/_components/TabView/_components/TabPanel';
import { TabView } from 'views/_components/TabView';
import { Toolbar } from 'views/_components/Toolbar';

import { DatasetService } from 'services/DatasetService';
import { ValidationService } from 'services/ValidationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { DateUtils } from 'views/_functions/Utils';

const DatasetSchema = ({
  dataflowName,
  designDataset,
  extensionsOperationsList = [],
  onGetReferencedFieldName,
  uniqueList = [],
  qcList
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [expandAll, setExpandAll] = useState(true);
  const [isDownloadingQCs, setIsDownloadingQCs] = useState(false);
  const [isDownloadingTableDefinition, setIsDownloadingTableDefinition] = useState(false);

  useEffect(() => {
    if (
      notificationContext.hidden.some(
        notification =>
          notification.key === 'EXPORT_QC_FAILED_EVENT' || notification.key === 'EXPORT_QC_COMPLETED_EVENT'
      )
    ) {
      setIsDownloadingQCs(false);
    }
  }, [notificationContext.hidden]);

  const columnOptions = {
    levelErrorTypes: {
      hasClass: true,
      class: styles.levelError,
      subClasses: [styles.blocker, styles.error, styles.warning, styles.info]
    },
    fields: {
      columns: [
        'pk',
        'required',
        'readOnly',
        'name',
        'description',
        'type',
        'codelistItems',
        'format',
        'referencedField'
      ],
      filtered: true,
      filterType: {
        multiselect: {
          type: [
            { label: 'Number Integer', value: 'NUMBER_INTEGER' },
            { label: 'Number Decimal', value: 'NUMBER_DECIMAL' },
            { label: 'Date', value: 'DATE' },
            { label: 'Datetime', value: 'DATETIME' },
            { label: 'Text', value: 'TEXT' },
            { label: 'Multiline text', value: 'TEXTAREA' },
            { label: 'Email', value: 'Email' },
            { label: 'URL', value: 'URL' },
            { label: 'Phone number', value: 'PHONE' },
            { label: 'Point', value: 'POINT' },
            { label: 'Multiple points', value: 'MULTIPOINT' },
            { label: 'Line', value: 'LINESTRING' },
            { label: 'Multiple lines', value: 'MULTILINESTRING' },
            { label: 'Polygon', value: 'POLYGON' },
            { label: 'Multiple polygons', value: 'MULTIPOLYGON' },
            { label: 'Single select', value: 'CODELIST' },
            { label: 'Multiple select', value: 'MULTISELECT_CODELIST' },
            { label: 'Link', value: 'LINK' },
            { label: 'External link', value: 'EXTERNAL_LINK' },
            { label: 'Attachment', value: 'ATTACHMENT' }
          ]
        }
      },
      groupable: true,
      names: {
        codelistItems: resourcesContext.messages['singleMultipleSelectItems'],
        pk: resourcesContext.messages['primaryKey'],
        readOnly: resourcesContext.messages['readOnly'],
        referencedField: resourcesContext.messages['referencedField'],
        shortCode: resourcesContext.messages['shortCode']
      }
    },
    externalIntegrations: {
      columns: ['operation', 'extension', 'id'],
      filtered: true,
      groupable: true,
      narrow: true,
      invisible: ['datasetSchemaId'],
      names: {
        operation: resourcesContext.messages['operation'],
        fileExtension: resourcesContext.messages['extensions'],
        id: resourcesContext.messages['id']
      }
    },
    uniques: {
      columns: ['tableName', 'fieldName'],
      filtered: true,
      groupable: true,
      narrow: true,
      invisible: ['datasetSchemaId'],
      names: {
        tableName: resourcesContext.messages['table'],
        fieldName: resourcesContext.messages['field']
      }
    },
    qc: {
      columns: [
        'tableName',
        'fieldName',
        'shortCode',
        'name',
        'description',
        'expression',
        'entityType',
        'levelError',
        'message',
        'automatic',
        'enabled'
      ],
      filtered: true,
      filterType: {
        multiselect: {
          entityType: [
            { label: 'FIELD', value: 'FIELD' },
            { label: 'RECORD', value: 'RECORD' },
            { label: 'TABLE', value: 'TABLE' },
            { label: 'DATASET', value: 'DATASET' }
          ],
          automatic: [
            { label: 'Automatic', value: true },
            { label: 'Manual', value: false }
          ],
          enabled: [
            { label: 'Enabled', value: true },
            { label: 'Disabled', value: false }
          ],
          levelError: [
            { label: 'Info', value: 'INFO', class: styles.levelError, subclass: styles.info },
            { label: 'Warning', value: 'WARNING', class: styles.levelError, subclass: styles.warning },
            { label: 'Error', value: 'ERROR', class: styles.levelError, subclass: styles.error },
            { label: 'Blocker', value: 'BLOCKER', class: styles.levelError, subclass: styles.blocker }
          ]
        }
      },
      groupable: true,
      invisible: ['datasetSchemaId', 'id'],
      names: {
        tableName: resourcesContext.messages['table'],
        fieldName: resourcesContext.messages['field'],
        entityType: resourcesContext.messages['entityType'],
        levelError: resourcesContext.messages['levelError'],
        ruleName: resourcesContext.messages['ruleName']
      }
    }
  };

  const renderExternalIntegrations = () => (
    <TabPanel
      header={resourcesContext.messages['externalIntegrations']}
      rightIcon={config.icons['export']}
      rightIconClass={`${styles.tabs} ${styles.externalIntegrationsTab}`}>
      <DatasetSchemaTable
        columnOptions={columnOptions}
        fields={!isNil(parsedDesignDataset.extensionsOperations) ? parsedDesignDataset.extensionsOperations : []}
        type="externalIntegrations"
      />
    </TabPanel>
  );

  const renderProperties = () => (
    <div>
      {renderIconProperty(resourcesContext.messages['availableInPublic'], designDataset.availableInPublic)}
      {renderIconProperty(resourcesContext.messages['referenceDataset'], designDataset.referenceDataset)}
    </div>
  );

  const renderIconProperty = (title, value) => {
    return (
      <div className={styles.property}>
        <span className={styles.propertyTitle}>{`${title}:`}</span>
        {value ? (
          <FontAwesomeIcon aria-label={value} icon={AwesomeIcons('check')} />
        ) : (
          <FontAwesomeIcon aria-label={value} icon={AwesomeIcons('cross')} />
        )}
      </div>
    );
  };

  const renderProperty = (title, value) => {
    return (
      <div className={styles.property}>
        <span className={styles.propertyTitle}>{`${title}:`}</span>
        <span className={styles.propertyValue}>{!isEmpty(value) ? value.toString() : '-'}</span>
      </div>
    );
  };

  const renderTables = () => (
    <TabPanel
      header={resourcesContext.messages['tablesAndFields']}
      rightIcon={config.icons['table']}
      rightIconClass={styles.tabs}>
      <Toolbar className={styles.datasetSchemaToolbar}>
        <div className="p-toolbar-group-left">
          <Button
            className={`p-button-rounded p-button-secondary-transparent ${
              !isDownloadingTableDefinition ? 'p-button-animated-blink' : ''
            }`}
            disabled={isDownloadingTableDefinition}
            icon={isDownloadingTableDefinition ? 'spinnerAnimate' : 'export'}
            label={resourcesContext.messages['exportTablesSchema']}
            onClick={() => onDownloadTableDefinitions(parsedDesignDataset.datasetId)}
          />
          <Button
            className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
            icon={expandAll ? 'angleRight' : 'angleDown'}
            label={expandAll ? resourcesContext.messages['collapseAll'] : resourcesContext.messages['expandAll']}
            onClick={() => setExpandAll(!expandAll)}
          />
        </div>
      </Toolbar>
      <Accordion
        activeIndex={expandAll ? parsedDesignDataset?.tables?.map((_, i) => i) : 0}
        key={uniqueId('tables')}
        multiple={true}>
        {!isNil(parsedDesignDataset) &&
          !isNil(parsedDesignDataset.tables) &&
          parsedDesignDataset.tables.map(table => {
            const { description, readOnly, prefilled, fixedNumber, mandatory } = table.properties;
            return (
              <AccordionTab header={table.tableSchemaName} key={uniqueId(table.tableSchemaName)}>
                {renderProperty(resourcesContext.messages['description'], description)}
                {renderIconProperty(resourcesContext.messages['readOnly'], readOnly)}
                {renderIconProperty(resourcesContext.messages['prefilled'], prefilled)}
                {renderIconProperty(resourcesContext.messages['fixedNumber'], fixedNumber)}
                {renderIconProperty(resourcesContext.messages['notEmpty'], mandatory)}
                <DatasetSchemaTable
                  columnOptions={columnOptions}
                  fields={!isNil(table) ? table.fields : []}
                  type="fields"
                />
              </AccordionTab>
            );
          })}
      </Accordion>
    </TabPanel>
  );

  const renderUniques = () => (
    <TabPanel
      header={resourcesContext.messages['uniques']}
      rightIcon={config.icons['key']}
      rightIconClass={styles.tabs}>
      <DatasetSchemaTable
        columnOptions={columnOptions}
        fields={!isNil(parsedDesignDataset.uniques) ? parsedDesignDataset.uniques : []}
        type="uniques"
      />
    </TabPanel>
  );

  const renderQCs = () => (
    <TabPanel
      header={resourcesContext.messages['qcRules']}
      rightIcon={config.icons['horizontalSliders']}
      rightIconClass={styles.tabs}>
      <Button
        className={`p-button-rounded p-button-secondary-transparent ${
          !isDownloadingQCs ? 'p-button-animated-blink' : ''
        }`}
        disabled={isDownloadingQCs}
        icon={isDownloadingQCs ? 'spinnerAnimate' : 'export'}
        label={resourcesContext.messages['downloadQCsButtonLabel']}
        onClick={() => onDownloadQCRules(parsedDesignDataset.datasetId)}
      />
      <DatasetSchemaTable
        columnOptions={columnOptions}
        fields={!isNil(parsedDesignDataset.qc) ? parsedDesignDataset.qc : []}
        type="qc"
      />
    </TabPanel>
  );

  const getFieldFormat = field => {
    switch (field.type.toUpperCase()) {
      case 'DATE':
        return resourcesContext.messages['dateFieldFormatRestriction'];
      case 'DATETIME':
        return resourcesContext.messages['datetimeFieldFormatRestriction'];
      case 'TEXT':
      case 'TEXTAREA':
        return resourcesContext.messages['textFieldFormatRestriction'];
      case 'RICH_TEXT':
        return resourcesContext.messages['richTextFieldFormatRestriction'];
      case 'NUMBER_INTEGER':
        return resourcesContext.messages['longFieldFormatRestriction'];
      case 'NUMBER_DECIMAL':
        return resourcesContext.messages['decimalFieldFormatRestriction'];
      case 'EMAIL':
        return resourcesContext.messages['emailFieldFormatRestriction'];
      case 'PHONE':
        return resourcesContext.messages['phoneNumberFieldFormatRestriction'];
      case 'URL':
        return resourcesContext.messages['urlFieldFormatRestriction'];
      case 'ATTACHMENT':
        return `${resourcesContext.messages['validExtensions']} ${field.validExtensions.join(', ')}
        - ${resourcesContext.messages['maxFileSize']} ${
          field.maxSize.toString() !== '0'
            ? `${field.maxSize} ${resourcesContext.messages['MB']}`
            : resourcesContext.messages['maxSizeNotDefined']
        }`;
      case 'POINT':
      case 'MULTIPOINT':
      case 'LINESTRING':
      case 'MULTILINESTRING':
      case 'POLYGON':
      case 'MULTIPOLYGON':
        return (
          <a
            href="https://geojsonlint.com/"
            rel="noreferrer"
            target="_blank"
            title={resourcesContext.messages['geomTypeHelpTooltip']}>
            <FontAwesomeIcon
              aria-hidden={false}
              aria-label={resourcesContext.messages['geomTypeHelpTooltip']}
              className="p-breadcrumb-home"
              data-for={`${field.fieldId}_geometricTypeTooltip`}
              data-tip
              icon={AwesomeIcons('externalUrl')}
            />
            <ReactTooltip
              border={true}
              className={styles.tooltipClass}
              effect="solid"
              id={`${field.fieldId}_geometricTypeTooltip`}
              place="top">
              <span>{resourcesContext.messages['geomTypeHelpTooltip']}</span>
            </ReactTooltip>
          </a>
        );
      default:
        return '';
    }
  };

  const parseDesignDataset = (design, extensionsOperationsList, uniqueList, qcList) => {
    const parsedDataset = {};
    parsedDataset.datasetId = design.datasetId;
    parsedDataset.datasetSchemaId = design.datasetSchemaId;
    parsedDataset.datasetSchemaDescription = design.datasetSchemaDescription;
    parsedDataset.availableInPublic = design.availableInPublic;
    parsedDataset.referenceDataset = design.referenceDataset;
    if (!isNil(design.tables) && design.tables.length > 0) {
      const tables = design.tables.map(tableDTO => {
        const tableProperties = {
          description:
            !isNil(tableDTO.tableSchemaDescription) && tableDTO.tableSchemaDescription !== ''
              ? tableDTO.tableSchemaDescription
              : '-',
          readOnly: tableDTO.tableSchemaReadOnly,
          prefilled: !isNil(tableDTO.tableSchemaToPrefill) ? tableDTO.tableSchemaToPrefill : false,
          fixedNumber: !isNil(tableDTO.tableSchemaFixedNumber) ? tableDTO.tableSchemaFixedNumber : false,
          mandatory: tableDTO.tableSchemaNotEmpty
        };

        const table = {};
        table.tableSchemaName = tableDTO.tableSchemaName;
        table.properties = tableProperties;
        if (!isNil(tableDTO.records) && !isNil(tableDTO.records[0].fields) && tableDTO.records[0].fields.length > 0) {
          const containsCodelists = !isEmpty(
            tableDTO.records[0].fields.filter(
              fieldElmt => fieldElmt.type === 'CODELIST' || fieldElmt.type === 'MULTISELECT_CODELIST'
            )
          );
          const containsLinks = !isEmpty(
            tableDTO.records[0].fields.filter(
              fieldElmt => fieldElmt.type === 'LINK' || fieldElmt.type === 'EXTERNAL_LINK'
            )
          );
          const fields = tableDTO.records[0].fields.map(fieldDTO => {
            const field = {};
            let referencedField = {};
            if (!isNil(fieldDTO.referencedField)) {
              referencedField = onGetReferencedFieldName(fieldDTO.referencedField, fieldDTO.type === 'EXTERNAL_LINK');
              referencedField.pkHasMultipleValues = fieldDTO.pkHasMultipleValues;
              referencedField.pkMustBeUsed = fieldDTO.pkMustBeUsed;
            }

            field.pk = fieldDTO.pk;
            field.required = fieldDTO.required;
            field.readOnly = fieldDTO.readOnly;
            field.name = fieldDTO.name;
            field.description = !isNull(fieldDTO.description) ? fieldDTO.description : '-';
            field.type = fieldDTO.type;
            if (containsCodelists) {
              if (fieldDTO.type === 'CODELIST' || fieldDTO.type === 'MULTISELECT_CODELIST') {
                field.codelistItems = fieldDTO.codelistItems;
              } else {
                field.codelistItems = [];
              }
            }
            field.format = getFieldFormat(fieldDTO);
            if (containsLinks) {
              if (!isNil(fieldDTO.referencedField)) {
                field.referencedField = referencedField;
              } else {
                field.referencedField = '';
              }
            }
            return field;
          });

          table.fields = fields;
        }

        return table;
      });

      parsedDataset.tables = tables;
    }
    parsedDataset.uniques = uniqueList;
    parsedDataset.qc = qcList;
    parsedDataset.extensionsOperations = extensionsOperationsList;

    return parsedDataset;
  };

  const parsedDesignDataset = parseDesignDataset(designDataset, extensionsOperationsList, uniqueList, qcList);

  const onDownloadQCRules = async datasetId => {
    setIsDownloadingQCs(true);
    notificationContext.add({ type: 'DOWNLOAD_QC_RULES_START' });

    try {
      await ValidationService.generateQCRulesFile(datasetId);
    } catch (error) {
      console.error('DatasetSchema - onDownloadQCRules.', error);
      notificationContext.add({ type: 'GENERATE_QC_RULES_FILE_ERROR' });
      setIsDownloadingQCs(false);
    }
  };

  const onDownloadTableDefinitions = async datasetSchemaId => {
    try {
      setIsDownloadingTableDefinition(true);
      const { data } = await DatasetService.downloadTableDefinitions(datasetSchemaId);

      if (!isNil(data)) {
        const date = new Date();
        const formattedDate = DateUtils.formatDate(date, isNil(date), true);
        DownloadFile(data, `table-definition-${datasetSchemaId}-${formattedDate}.zip`);
      }
    } catch (error) {
      console.error('DatasetSchema - onDownloadTableDefinitions.', error);
      notificationContext.add({ type: 'DOWNLOAD_TABLE_DEFINITIONS_ERROR' });
    } finally {
      setIsDownloadingTableDefinition(false);
    }
  };

  return (
    <div>
      <div className={styles.datasetSchemaPropertiesWrapper}>
        <h3
          className={
            styles.header
          }>{`${resourcesContext.messages['createDatasetSchemaName']}: ${designDataset.datasetSchemaName}`}</h3>
        {renderProperties()}
      </div>
      <TabView activeIndex={0} hasQueryString={false} name="DatasetSchemas">
        {renderTables()}
        {renderQCs()}
        {renderUniques()}
        {renderExternalIntegrations()}
      </TabView>
    </div>
  );
};

export { DatasetSchema };
