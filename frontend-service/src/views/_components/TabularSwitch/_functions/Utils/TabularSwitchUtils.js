const onSwitchAnimate = views =>
  Object.keys(views)
    .map(view => views[view])
    .indexOf(true);

const parseViews = (elements, value) => {
  const viewTypes = elements.reduce((a, b) => ((a[b] = false), a), {});
  viewTypes[value] = true;

  return viewTypes;
};

export const TabularSwitchUtils = { onSwitchAnimate, parseViews };
