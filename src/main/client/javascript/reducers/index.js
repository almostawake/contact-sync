import { combineReducers } from 'redux';
import { reducer as form } from 'redux-form';
import { routerReducer as routing } from 'react-router-redux';

/**
 * Root reducer for the app.
 */
const rootReducer = combineReducers({
  form,
  routing,
});

export default rootReducer;
