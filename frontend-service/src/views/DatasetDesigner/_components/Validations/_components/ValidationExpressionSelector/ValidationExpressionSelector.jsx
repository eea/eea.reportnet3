import { ComparisonExpression } from 'views/DatasetDesigner/_components/Validations/_components/ComparisonExpression';
import { RelationExpression } from 'views/DatasetDesigner/_components/Validations/_components/RelationExpression';
import { ValidationExpression } from 'views/DatasetDesigner/_components/Validations/_components/ValidationExpression';
import { ValidationExpressionGroup } from 'views/DatasetDesigner/_components/Validations/_components/ValidationExpressionGroup';

export const ValidationExpressionSelector = props => {
  const { expressionType } = props;

  if (props.expressionValues.expressions && props.expressionValues.expressions.length > 0) {
    return <ValidationExpressionGroup {...props} />;
  }

  if (expressionType === 'fieldComparison' || expressionType === 'ifThenClause') {
    return <ComparisonExpression {...props} />;
  }

  if (expressionType === 'fieldRelations') {
    return <RelationExpression {...props} />;
  }

  return <ValidationExpression {...props} />;
};
