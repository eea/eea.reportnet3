import { getFieldType } from './getFieldType';

export const setExpressionsFieldsTypes = (expressions, table, tabs) => {
  expressions.forEach(expression => {
    if (expression.expressions.length > 0) {
      setExpressionsFieldsTypes(expression.expressions, table, tabs);
    } else {
      expression['field1Type'] = getFieldType(table, { code: expression.field1 }, tabs);
      if (expression.valueTypeSelector !== 'value') {
        expression['field2Type'] = getFieldType(table, { code: expression.field2 }, tabs);
      }
    }
  });
};
