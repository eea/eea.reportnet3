import React from 'react';

import { storiesOf } from '../../../../../.storybook/storiesOf';
import { action } from '@storybook/addon-actions';
import { config } from 'assets/conf';
import { ButtonY } from './ButtonY';

storiesOf('ButtonY', module)
  .add('Button ', () => <ButtonY label="Push" onClick={action('clicked')} />)
  .add('Button wit Icon Left', () => (
    <ButtonY label="Click" icon={config.icons.check} iconPos="left" onClick={action('clicked')} />
  ))
  .add('Button wit Icon Right', () => (
    <ButtonY label="Button" icon={config.icons.check} iconPos="right" onClick={action('clicked')} />
  ));
