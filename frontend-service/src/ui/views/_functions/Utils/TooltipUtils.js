const removeElementsByClass = className => {
  const elements = document.getElementsByClassName(className);
  while (elements.length > 0) {
    elements[0].parentNode.removeChild(elements[0]);
  }
};

export const TooltipUtils = {
  removeElementsByClass
};
