const isDuplicateInObject = (array, property) => {
  let isDuplicated = false,
    testObject = {};

  array.map(item => {
    let itemPropertyName = item[property];
    if (itemPropertyName in testObject) {
      testObject[itemPropertyName].duplicatedRoles = true;
      item.duplicatedRoles = true;
      isDuplicated = true;
    } else {
      testObject[itemPropertyName] = item;
      delete item.duplicatedRoles;
    }
  });
  return isDuplicated;
};

const reduceString = (text, prefix, suffix) => {
  let string = text;
  let index = string.indexOf(prefix);

  if (index >= 0) {
    string = string.substring(index + prefix.length);
  } else {
    return '';
  }
  if (suffix) {
    index = string.indexOf(suffix);
    if (index < 0) {
      return '';
    } else {
      string = string.substring(0, index);
    }
  }
  return string;
};

export const DataflowsUtils = {
  isDuplicateInObject,
  reduceString
};
