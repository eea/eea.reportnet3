import { isEmpty, isNil } from 'lodash';
import React, { Fragment } from 'react';

import { WebformDataFormFieldEditor } from './_components/WebformDataFormFieldEditor';

export const WebformDataForm = ({ colsSchema, datasetId, onChangeForm, records, selectedRecord }) => {
  const editWebformRecordForm = colsSchema.map((column, i) => {
    if (!isNil(selectedRecord) && !isEmpty(selectedRecord)) {
      const field = selectedRecord.dataRow.filter(r => Object.keys(r.fieldData)[0] === column.field)[0];
      return (
        <Fragment key={column.field}>
          <div className="p-col-4" style={{ padding: '.75em' }}>
            <label htmlFor={column.field}>{column.header}</label>
            {/* <Button
            className={`${styles.columnInfoButton} p-button-rounded p-button-secondary-transparent`}
            icon="infoCircle"
            onClick={() => onShowFieldInfo(column.header, true)}
            tabIndex="-1"
            tooltip={getTooltipMessage(column)}
            tooltipOptions={{ position: 'top' }}
          /> */}
          </div>

          <div
            className="p-col-8"
            style={{ padding: '.5em', width: column.type === 'CODELIST' || column.type === 'LINK' ? '30%' : '' }}>
            <WebformDataFormFieldEditor
              // autoFocus={i === 0}
              column={column}
              datasetId={datasetId}
              field={column.header}
              fieldValue={field.fieldData[column.field]}
              // hasWritePermissions={hasWritePermissions}
              // isVisible={editDialogVisible}
              onChangeForm={onChangeForm}
              type={column.type}
            />
          </div>
        </Fragment>
      );
    }
  });

  return editWebformRecordForm;
};
