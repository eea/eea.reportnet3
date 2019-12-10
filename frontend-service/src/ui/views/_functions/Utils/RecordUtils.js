import { isUndefined, isNull, isString } from 'lodash';

export const RecordUtils = {
  changeRecordValue: (recordData, field, value) => {
    //Delete \r and \n values for tabular paste
    if (!isUndefined(value) && !isNull(value) && isString(value)) {
      value = value.replace(`\r`, '').replace(`\n`, '');
    }
    console.log({ recordData, field, value });
    recordData.dataRow.filter(data => Object.keys(data.fieldData)[0] === field)[0].fieldData[field] = value;
    return recordData;
  }
};
