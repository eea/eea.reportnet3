import React from 'react';

import { storiesOf } from '../../../../../.storybook/storiesOf';
import { action } from '@storybook/addon-actions';
import { Button } from './Button';

storiesOf('Button', module).add('Button TO FIX', () => (
  <Button
    label="Push"
    icon="eye"
    iconPos="right"
    className={`p-button-rounded p-button-secondary`}
    disabled={false}
    onClick={action('clicked')}
  />
));
