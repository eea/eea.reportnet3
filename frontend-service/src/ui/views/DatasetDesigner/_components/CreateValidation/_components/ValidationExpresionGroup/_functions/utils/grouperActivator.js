export const grouperActivator = () => {
  if (ruleProperty.key == 'group') {
    if (ruleProperty.value.value) {
      groupCandidate.push(ruleId);
    } else {
      pull(groupCandidate, ruleId);
    }
    creationFormDispatch({
      type: 'GROUP_RULES_ACTIVATOR',
      payload: {
        groupExpresionsActive: ruleProperty.value.value ? 1 : -1
      }
    });
  }
};
