const groupOperations = (operation, list) => {
    const extensionList = list.reduce((objectsByKeyValue, obj) => {
    const value = obj[operation].toLowerCase();
    objectsByKeyValue[value] = (objectsByKeyValue[value] || []).concat(obj);
    return objectsByKeyValue;
  }, {});

  return { 
    export: extensionList['export'] || [],
    import: extensionList['import'] || [],
    importOtherSystems: extensionList['importOtherSystems'] || []
  };
};

export const ExtensionUtils = { groupOperations };
