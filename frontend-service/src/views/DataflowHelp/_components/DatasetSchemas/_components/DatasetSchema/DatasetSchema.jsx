import { useContext } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import uniqueId from 'lodash/uniqueId';

import styles from './DatasetSchema.module.scss';

import { config } from 'conf';

import { Accordion, AccordionTab } from 'primereact/accordion';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { DatasetSchemaTable } from './_components/DatasetSchemaTable';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import ReactTooltip from 'react-tooltip';
import { TabPanel } from 'views/_components/TabView/_components/TabPanel';
import { TabView } from 'views/_components/TabView';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

const DatasetSchema = ({
  designDataset,
  extensionsOperationsList = [],
  index,
  onGetReferencedFieldName,
  uniqueList = [],
  validationList
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const columnOptions = {
    fields: {
      columns: ['pk', 'required', 'readOnly', 'name', 'description', 'type', 'format', 'referencedField'],
      filtered: false,
      groupable: true,
      names: {
        codelistItems: resourcesContext.messages['codelistEditorItems'],
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
        fileExtension: resourcesContext.messages['extension'],
        id: resourcesContext.messages['id']
      }
    },
    uniques: {
      columns: ['table', 'field'],
      filtered: true,
      groupable: true,
      narrow: true,
      invisible: ['datasetSchemaId'],
      names: {
        tableName: resourcesContext.messages['table'],
        fieldName: resourcesContext.messages['field']
      }
    },
    validations: {
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
            { label: 'Field', value: 'FIELD' },
            { label: 'Record', value: 'RECORD' },
            { label: 'Table', value: 'TABLE' },
            { label: 'Dataset', value: 'DATASET' }
          ],
          automatic: [
            { label: 'True', value: 'true' },
            { label: 'False', value: 'false' }
          ],
          enabled: [
            { label: 'True', value: 'true' },
            { label: 'False', value: 'false' }
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

  const renderHeader = () => (
    <TabPanel
      header={resourcesContext.messages['properties']}
      rightIcon={config.icons['settings']}
      rightIconClass={styles.tabs}>
      {renderIconProperty(resourcesContext.messages['availableInPublic'], designDataset.availableInPublic)}
      {renderIconProperty(resourcesContext.messages['referenceDataset'], designDataset.referenceDataset)}
    </TabPanel>
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
      header={resourcesContext.messages['tables']}
      rightIcon={config.icons['table']}
      rightIconClass={styles.tabs}>
      <Accordion key={uniqueId('tables')} multiple={true}>
        {!isNil(parsedDesignDataset) &&
          !isNil(parsedDesignDataset.tables) &&
          parsedDesignDataset.tables.map(table => {
            return (
              <AccordionTab header={table.tableSchemaName} key={uniqueId(table.name)}>
                {renderProperty(resourcesContext.messages['description'], table.tableSchemaDescription)}
                {renderIconProperty(resourcesContext.messages['readOnly'], table.tableSchemaReadOnly)}
                {renderIconProperty(resourcesContext.messages['prefilled'], table.tableSchemaToPrefill)}
                {renderIconProperty(resourcesContext.messages['fixedNumber'], table.tableSchemaFixedNumber)}
                {renderIconProperty(resourcesContext.messages['notEmpty'], table.tableSchemaNotEmpty)}
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

  const renderValidations = () => (
    <TabPanel
      header={resourcesContext.messages['validations']}
      rightIcon={config.icons['horizontalSliders']}
      rightIconClass={styles.tabs}>
      <DatasetSchemaTable
        columnOptions={columnOptions}
        fields={!isNil(parsedDesignDataset.validations) ? parsedDesignDataset.validations : []}
        type="validations"
      />
    </TabPanel>
  );

  // const renderDatasetSchema = () => {
  //   if (!isNil(designDataset)) {
  //     const parsedDesignDataset = parseDesignDataset(
  //       designDataset,
  //       extensionsOperationsList,
  //       uniqueList,
  //       validationList
  //     );

  //     return (
  //       <div>
  //         <TreeView
  //           columnOptions={columnOptions}
  //           excludeBottomBorder={false}
  //           expandAll={expandAll}
  //           key={index}
  //           property={parsedDesignDataset}
  //           propertyName=""
  //           rootProperty=""
  //         />
  //       </div>
  //     );
  //   } else {
  //     return null;
  //   }
  // };

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

  const parseDesignDataset = (design, extensionsOperationsList, uniqueList, validationList) => {
    const parsedDataset = {};
    parsedDataset.datasetSchemaDescription = design.datasetSchemaDescription;
    parsedDataset.availableInPublic = design.availableInPublic;
    parsedDataset.referenceDataset = design.referenceDataset;
    if (!isNil(design.tables) && design.tables.length > 0) {
      const tables = design.tables.map(tableDTO => {
        const tableProperties = [
          {
            description:
              !isNil(tableDTO.tableSchemaDescription) && tableDTO.tableSchemaDescription !== ''
                ? tableDTO.tableSchemaDescription
                : '-',
            readOnly: tableDTO.tableSchemaReadOnly,
            prefilled: !isNil(tableDTO.tableSchemaToPrefill) ? tableDTO.tableSchemaToPrefill : false,
            fixedNumber: !isNil(tableDTO.tableSchemaFixedNumber) ? tableDTO.tableSchemaFixedNumber : false,
            mandatory: tableDTO.tableSchemaNotEmpty
          }
        ];

        const table = {};
        table.tableSchemaName = tableDTO.tableSchemaName;
        table.properties = tableProperties;
        if (!isNull(tableDTO.records) && !isNil(tableDTO.records[0].fields) && tableDTO.records[0].fields.length > 0) {
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
      // tables.button = {
      //   label: resourcesContext.messages['downloadTableDefinitions'],
      //   icon: 'export',
      //   onClick: () => {
      //     // downloadTableDefinitions(tableDTO.tableSchemaId);
      //     console.log('downloadTableDefinitions');
      //   },
      //   tooltip: resourcesContext.messages['downloadTableDefinitions']
      // };
      parsedDataset.tables = tables;
    }
    parsedDataset.uniques = uniqueList;
    parsedDataset.validations = validationList;
    parsedDataset.extensionsOperations = extensionsOperationsList;

    // const dataset = {};
    // dataset[design.datasetSchemaName] = parsedDataset;
    return parsedDataset;
  };

  const parsedDesignDataset = parseDesignDataset(designDataset, extensionsOperationsList, uniqueList, validationList);
  console.log({ parsedDesignDataset });
  return (
    <div>
      <h3
        className={
          styles.header
        }>{`${resourcesContext.messages['createDatasetSchemaName']}: ${designDataset.datasetSchemaName}`}</h3>
      <TabView activeIndex={0} hasQueryString={false} name="DatasetSchemas">
        {renderHeader()}
        {renderTables()}
        {renderExternalIntegrations()}
        {renderUniques()}
        {renderValidations()}
        {/* <TabPanel header={resourcesContext.messages['webLinks']} headerClassName="dataflowHelp-webLinks-help-step">
            <WebLinks
              dataflowId={dataflowId}
              isLoading={isLoadingWebLinks}
              isToolbarVisible={isToolbarVisible}
              onLoadWebLinks={onLoadWebLinks}
              setSortFieldWebLinks={setSortFieldWebLinks}
              setSortOrderWebLinks={setSortOrderWebLinks}
              sortFieldWebLinks={sortFieldWebLinks}
              sortOrderWebLinks={sortOrderWebLinks}
              webLinks={webLinks}
            />
          </TabPanel>
          <TabPanel
            disabled={isEmpty(datasetsSchemas)}
            header={resourcesContext.messages['datasetSchemas']}
            headerClassName="dataflowHelp-schemas-help-step"
            rightIcon={isEmpty(datasetsSchemas) && isLoadingSchemas ? config.icons['spinnerAnimate'] : null}>
            <DatasetSchemas
              dataflowId={dataflowId}
              datasetsSchemas={datasetsSchemas}
              isCustodian={isCustodian}
              onLoadDatasetsSchemas={onLoadDatasetsSchemas}
            />
          </TabPanel> */}
      </TabView>
    </div>
  );
};

export { DatasetSchema };
