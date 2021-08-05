const onSwitchAnimate = views =>
  Object.keys(views)
    .map(view => views[view])
    .indexOf(true);

const parseViews = (elements, value) => {
  return elements.reduce((a, key) => Object.assign(a, { [key]: value === key }), {});
};

export const TabularSwitchUtils = { onSwitchAnimate, parseViews };
