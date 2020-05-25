import React, { useContext, useEffect } from 'react';
import isNil from 'lodash/isNil';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const useHelpSteps = (sideEffects, config, component, roles = []) => {
  const leftSideBarContext = useContext(LeftSideBarContext);
  const resources = useContext(ResourcesContext);

  console.log(component, config);
  const steps = [
    {
      content: <h2>{resources.messages[component]}</h2>,
      locale: { skip: <strong aria-label="skip">{resources.messages['skipHelp']}</strong> },
      placement: 'center',
      target: 'body'
    }
  ];

  config.steps.forEach(step =>
    steps.push({
      content: <h3>{resources.messages[step.content]}</h3>,
      target: step.target
    })
  );

  Object.keys(config).forEach(key => {
    console.log(key, config[key]);
    if (roles.indexOf(key) > -1) {
      config[key].forEach(step =>
        steps.push({
          content: <h3>{resources.messages[step.content]}</h3>,
          target: step.target
        })
      );
    }
  });

  console.log({ steps });

  const loadedClassesSteps = [...steps].filter(
    step =>
      !isNil(document.getElementsByClassName(step.target.substring(1, step.target.length))[0]) || step.target === 'body'
  );

  useEffect(() => {
    console.log({ loadedClassesSteps });
    console.log(sideEffects);
    leftSideBarContext.addHelpSteps(component, loadedClassesSteps);
  }, sideEffects);
};

export { useHelpSteps };
