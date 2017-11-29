import React from 'react';
import PropTypes from 'prop-types';
import { AppBar } from 'material-ui';

const Layout = ({ children }) => (
  <div className="default-layout">
    <AppBar
      title="<%= _.capitalize(project) %>"
      iconClassNameRight="muidocs-icon-navigation-expand-more"
    />
    <div className="main">
      { children }
    </div>
  </div>
);

Layout.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.node,
    PropTypes.arrayOf(PropTypes.node),
  ]).isRequired,
};

export default Layout;
