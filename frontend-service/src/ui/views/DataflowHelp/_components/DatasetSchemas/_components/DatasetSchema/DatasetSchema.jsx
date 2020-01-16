import React from 'react';

import { isEmpty, isNull, isUndefined } from 'lodash';

import { TreeView } from 'ui/views/_components/TreeView';

import { CodelistService } from 'core/services/Codelist';

const DatasetSchema = ({ designDataset, index }) => {
  const renderDatasetSchema = () => {
    return !isUndefined(designDataset) && !isNull(designDataset) ? (
      <div>
        <TreeView
          excludeBottomBorder={false}
          groupableProperties={['fields']}
          key={index}
          property={parseDesignDataset(designDataset)}
          propertyName={''}
          rootProperty={''}
        />
      </div>
    ) : null;
  };

  return renderDatasetSchema();
};

const parseDesignDataset = design => {
  const parsedDataset = {};
  parsedDataset.datasetSchemaDescription = design.datasetSchemaDescription;
  parsedDataset.levelErrorTypes = design.levelErrorTypes;

  if (!isUndefined(design.tables) && !isNull(design.tables) && design.tables.length > 0) {
    const tables = design.tables.map(tableDTO => {
      const table = {};
      table.tableSchemaName = tableDTO.tableSchemaName;
      table.tableSchemaDescription = tableDTO.tableSchemaDescription;
      if (
        !isNull(tableDTO.records) &&
        !isUndefined(tableDTO.records[0].fields) &&
        !isNull(tableDTO.records[0].fields) &&
        tableDTO.records[0].fields.length > 0
      ) {
        const existACodelist = tableDTO.records[0].fields.filter(field => field.type === 'CODELIST');
        const fields = tableDTO.records[0].fields.map(fieldDTO => {
          if (!isEmpty(existACodelist)) {
            const codelist = fieldDTO.type === 'CODELIST' ? getCodelistById(fieldDTO.id) : '';
            return {
              name: fieldDTO.name,
              type: fieldDTO.type,
              description: !isNull(fieldDTO.description) ? fieldDTO.description : '-',
              codelist: !isEmpty(codelist) ? codelist.version : ''
            };
          } else {
            return {
              name: fieldDTO.name,
              type: fieldDTO.type,
              description: !isNull(fieldDTO.description) ? fieldDTO.description : '-'
            };
          }
        });
        table.fields = fields;
      }

      return table;
    });

    parsedDataset.tables = tables;
  }
  // parsedDataset.codelists = getCodelists(1);

  // parsedDataset.codelists = {
  //   id: 18,
  //   name: 'cl2',
  //   description: 'code1',
  //   category: {
  //     id: 1,
  //     shortCode: 'c1',
  //     description: 'cat1'
  //   },
  //   version: 1,
  //   items: [
  //     {
  //       id: 16,
  //       shortCode: 'o',
  //       label: 'two',
  //       definition: 'def',
  //       codelistId: null
  //     }
  //   ],
  //   status: 'DESIGN'
  // };

  const dataset = {};
  dataset[design.datasetSchemaName] = parsedDataset;
  return dataset;
};

const getCodelistById = async codelistId => {
  try {
    const codelist = await CodelistService.all(codelistId);
    return codelist;
  } catch (error) {
    console.log(error);
    // const {
    //   dataflow: { name: dataflowName },
    //   dataset: { name: datasetName }
    // } = await getMetadata({ dataflowId, datasetId });
    // const datasetError = {
    //   type: error.message,
    //   content: {
    //     datasetId,
    //     dataflowName,
    //     datasetName
    //   }
    // };
    // notificationContext.add(datasetError);
    // if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
    //   history.push(getUrl(routes.DATAFLOW, { dataflowId }));
    // }
  }
};

export { DatasetSchema };

// codelists = [
//   {
//     name: 'wise',
//     description: '(WISE - Water Information System of Europe)',
//     codelists: [
//       {
//         name: 'BWDObservationStatus',
//         description: '(Bathing water observation status)',
//         version: '1.0',
//         status: 'Ready',
//         items: [
//           {
//             itemId: '1',
//             code: 'confirmedValue',
//             label: 'Confirmed value',
//             definition: 'Status flag to confirm that the reported observation value is...'
//           },
//           {
//             itemId: '2',
//             code: 'limitOfDetectionValue',
//             label: 'Limit of detection value',
//             definition: 'Status flag to inform that a specific observed...'
//           }
//         ]
//       },
//       {
//         name: 'BWDStatus',
//         description: '(Bathing water quality) ',
//         version: '3.0',
//         status: 'Design',
//         items: [
//           {
//             itemId: '3',
//             code: 0,
//             label: 'Not classified',
//             definition: 'Bathing water quality cannot be assessed and classified.'
//           },
//           {
//             itemId: '4',
//             code: 1,
//             label: 'Excellent',
//             definition:
//               'See Annex II (4) of BWD. Bathing water quality status is Excellent if: for inland waters, ( p95(IE) <= 200 ) AND ( p95(EC) <= 500 ) ...'
//           }
//         ]
//       },
//       {
//         name: 'BWDStatus',
//         description: '(Bathing water quality) ',
//         version: '3.1',
//         status: 'Design',
//         items: [
//           {
//             itemId: '5',
//             code: 0,
//             label: 'Not classified',
//             definition: 'Bathing water quality cannot be assessed and classified.'
//           },
//           {
//             itemId: '6',
//             code: 1,
//             label: 'Excellent',
//             definition:
//               'See Annex II (4) of BWD. Bathing water quality status is Excellent if: for inland waters, ( p95(IE) <= 200 ) AND ( p95(EC) <= 500 ) ...'
//           }
//         ]
//       }
//     ]
//   }
// ];
