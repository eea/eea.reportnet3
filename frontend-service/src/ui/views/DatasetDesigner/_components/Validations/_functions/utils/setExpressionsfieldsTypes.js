import { getFieldType } from './getFieldType';

export const setExpressionsfieldsTypes = (expressions, table, tabs) => {
  expressions.forEach(expression => {
    if (expression.expressions.length > 0) {
      setExpressionsfieldsTypes(expression.expressions, table, tabs);
    } else {
      expression['field1Type'] = getFieldType(table, { code: expression.field1 }, tabs);
      expression['field2Type'] = getFieldType(table, { code: expression.field2 }, tabs);
    }
  });
};
