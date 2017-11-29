import React from 'react';
import {IndexRoute, Route, Router} from 'react-router';
import {history} from '../store';
import HomePage from '../pages/HomePage';
import NotFoundPage from '../pages/NotFoundPage';

/**
 * Define frontend routes.
 */
const getRoutes = () => (
  <Router history={history}>
    <Route path="/">
      <IndexRoute component={HomePage} />
      <Route path="*" component={NotFoundPage} />
    </Route>
  </Router>
);

export default getRoutes;
