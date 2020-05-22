export const isAGroup = expression => {
  if (expression.expressions.length > 0) return true;
  return false;
};
