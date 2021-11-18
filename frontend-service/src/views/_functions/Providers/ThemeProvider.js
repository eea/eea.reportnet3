import { useContext, useReducer } from 'react';

import themeConfig from 'conf/theme.config.json';

import { ThemeContext } from 'views/_functions/Contexts/ThemeContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

const themeReducer = (state, { type, payload }) => {
  switch (type) {
    case 'TOGGLE_THEME':
      return { ...state, currentTheme: payload.newTheme };

    case 'DEFAULT_VISUAL_THEME':
      return { ...state, currentTheme: payload.newTheme };

    case 'SET_HEADER_COLLAPSE':
      return { ...state, headerCollapse: payload };

    default:
      return state;
  }
};

export const ThemeProvider = ({ children }) => {
  const userContext = useContext(UserContext);

  const [state, dispatch] = useReducer(themeReducer, {
    currentTheme: userContext.userProps.visualTheme,
    themes: {
      light: themeConfig.light,
      dark: themeConfig.dark
    },
    headerCollapse: false
  });

  return (
    <ThemeContext.Provider
      value={{
        ...state,
        onToggleTheme: newTheme => {
          dispatch({
            type: 'TOGGLE_THEME',
            payload: {
              newTheme
            }
          });
          const theme = state.themes[newTheme];

          Object.keys(theme).forEach(key => {
            const cssKey = `--${key}`;
            const cssValue = theme[key];
            document.body.style.setProperty(cssKey, cssValue);
          });
          userContext.onToggleVisualTheme(newTheme);
        },
        setHeaderCollapse: headerCollapse => {
          dispatch({
            type: 'SET_HEADER_COLLAPSE',
            payload: headerCollapse
          });
        }
      }}>
      {children}
    </ThemeContext.Provider>
  );
};
