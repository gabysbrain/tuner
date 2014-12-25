/** @jsx React.DOM */

require('./react_helper');

var Controls, actions, proj;

describe('Slice controls', function() {

  beforeEach(function() {
    proj = Factory.build('viewableProject');
    Controls = require('../SliceControls.jsx');
    actions = {
      changePSMetric: sinon.spy()
    };
  });

  it('has a slider for each input', function() {
    var ctrl = TestUtils.renderIntoDocument(
      <Controls project={proj}
                actions={actions} />
    );

    var inputs = TestUtils.scryRenderedDOMComponentsWithClass(ctrl, 'slice-control');
    expect(inputs.length).to.equal(4);
  });
});