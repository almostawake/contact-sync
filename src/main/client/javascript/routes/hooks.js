/* eslint-disable import/prefer-default-export */
import applyEachSeries from 'async/applyEachSeries';

export const composeOnEnterHooks = (...hooks) => (nextState, replace, callback) => {
  applyEachSeries(hooks, nextState, replace, (error) => {
    if (error) {
      console.error('hook error:', error);
    } // eslint-disable-line no-console
    callback();
  });
};
