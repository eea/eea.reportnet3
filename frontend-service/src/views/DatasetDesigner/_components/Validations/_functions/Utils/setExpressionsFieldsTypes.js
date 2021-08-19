import { getFieldType } from './getFieldType';

export const setExpressionsFieldsTypes = (expressions, table, tabs) => {
  expressions.forEach(expression => {
    if (expression.expressions.length > 0) {
      setExpressionsFieldsTypes(expression.expressions, table, tabs);
    } else {
      expression['field1Type'] = getFieldType(table, { code: expression.field1 }, tabs);

      if (
        expression.valueTypeSelector !== 'value' &&
        expression.operatorValue !== 'IS NULL' &&
        expression.operatorValue !== 'IS NOT NULL'
      ) {
        expression['field2Type'] = getFieldType(table, { code: expression.field2 }, tabs);
      }
    }
  });
};
