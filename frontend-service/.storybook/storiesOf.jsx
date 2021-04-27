import { storiesOf as storiesOfOriginal } from '@storybook/react';

import { withKnobs } from '@storybook/addon-knobs';

import 'primereact/resources/primereact.min.css';
import '../src/styles.css';
import 'primeicons/primeicons.css';
import '../src/index.css';
import '../src/reportnet.css';

export const storiesOf = (name, nodeModule) =>
  storiesOfOriginal(name, nodeModule)
    .addDecorator(withKnobs)
    .addDecorator(story => <>{story()}</>);
