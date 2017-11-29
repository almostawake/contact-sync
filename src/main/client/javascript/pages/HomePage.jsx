import React from 'react';
import { RaisedButton } from 'material-ui';
import CenteredPanelLayout from './CenteredPanelLayout';

/**
 * Application home page.
 */
const HomePage = () => (
  <CenteredPanelLayout title="Contact Sync">
    <p className="subheading" style={{ textAlign: 'center' }}>Click the button to start synchronising your flagged contacts with 3wks</p>

    <RaisedButton label="Start Sync" href="/system/sync/setup" primary fullWidth />
  </CenteredPanelLayout>
);

export default HomePage;
