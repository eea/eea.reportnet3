import { useContext, useReducer } from 'react';
import isNil from 'lodash/isNil';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

const leftSideBarReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ADD_HELP_STEPS':
      return {
        ...state,
        helpTitle: payload.helpTitle,
        steps: payload.steps
      };
    case 'ADD_MODEL':
      return {
        ...state,
        models: payload
      };
    case 'REMOVE_MODEL':
      return {
        ...state,
        models: []
      };
    case 'SET_MENU_STATE':
      return {
        ...state,
        isLeftSideBarOpened: payload.isLeftSideBarOpened
      };
    default:
      return state;
  }
};

const LeftSideBarProvider = ({ children }) => {
  const resources = useContext(ResourcesContext);
  const [state, dispatch] = useReducer(leftSideBarReducer, { models: [], steps: [], helpTitle: '' });

  const getSteps = (config, component) => {
    const steps = [
      {
        content: <h2>{resources.messages[component]}</h2>,
        locale: { skip: <strong aria-label="skip">{resources.messages['skipHelp']}</strong> },
        placement: 'center',
        target: 'body'
      }
    ];
    if (!isNil(config)) {
      config.steps.forEach(step =>
        steps.push({
          content: <h3>{step.content}</h3>,
          target: step.target
        })
      );
    }
    // Object.keys(config).forEach(key => {
    //   if (roles.indexOf(key) > -1) {
    //     config[key].forEach(step =>
    //       steps.push({
    //         content: <h3>{resources.messages[step.content]}</h3>,
    //         target: step.target
    //       })
    //     );
    //   }
    // });

    const loadedClassesSteps = [...steps].filter(
      step =>
        !isNil(document.getElementsByClassName(step.target.substring(1, step.target.length))[0]) ||
        step.target === 'body'
    );

    return loadedClassesSteps;
  };

  return (
    <LeftSideBarContext.Provider
      value={{
        ...state,
        addModels: models => {
          dispatch({
            type: 'ADD_MODEL',
            payload: models
          });
        },
        addHelpSteps: (config, component) => {
          const steps = getSteps(config, component);
          dispatch({
            type: 'ADD_HELP_STEPS',
            payload: { component, steps }
          });
        },
        removeHelpSteps: () => {
          dispatch({
            type: 'ADD_HELP_STEPS',
            payload: { steps: [] }
          });
        },
        removeModels: () => {
          dispatch({
            type: 'REMOVE_MODEL',
            payload: []
          });
        },
        setMenuState: () => {
          dispatch({
            type: 'SET_MENU_STATE',
            payload: { isLeftSideBarOpened: !state.isLeftSideBarOpened }
          });
        }
      }}>
      {children}
    </LeftSideBarContext.Provider>
  );
};
export { LeftSideBarProvider };
