/** @jsx React.DOM */

require('./react_helper');

var Controls, actions;

describe('Metric view controls', function() {

  beforeEach(function() {
    Controls = require('../MetricViewControls.jsx');
    actions = {
      changePSMetric: sinon.spy()
    };
  });

  it('has all the metrics', function() {
    var ctrl = TestUtils.renderIntoDocument(
      <Controls value="value"
                actions={actions} />
    );

    var inputs = TestUtils.scryRenderedDOMComponentsWithTag(ctrl, 'input');
    expect(inputs.length).to.equal(3);
  });

  it('has the correct metric selected', function() {
    var ctrl = TestUtils.renderIntoDocument(
      <Controls value="value"
                actions={actions} />
    );

    var inputs = TestUtils.scryRenderedDOMComponentsWithTag(ctrl, 'input');
    expect(inputs[0].getDOMNode().hasAttribute('checked')).to.be.true;
    expect(inputs[1].getDOMNode().hasAttribute('checked')).to.be.false;
    expect(inputs[2].getDOMNode().hasAttribute('checked')).to.be.false;
  });

  it('changes to the value view', function() {
    var ctrl = TestUtils.renderIntoDocument(
      <Controls value="error"
                actions={actions} />
    );

    var inputs = TestUtils.scryRenderedDOMComponentsWithTag(ctrl, 'input');
    TestUtils.Simulate.change(inputs[0]);

    expect(actions.changePSMetric.callCount).to.equal(1);
    expect(actions.changePSMetric.getCall(0).args[0]).to.equal('value');
  });

  it('changes to the error view', function() {
    var ctrl = TestUtils.renderIntoDocument(
      <Controls value="value"
                actions={actions} />
    );

    var inputs = TestUtils.scryRenderedDOMComponentsWithTag(ctrl, 'input');
    TestUtils.Simulate.change(inputs[1]);

    expect(actions.changePSMetric.callCount).to.equal(1);
    expect(actions.changePSMetric.getCall(0).args[0]).to.equal('error');
  });

  it('changes to the gain view', function() {
    var ctrl = TestUtils.renderIntoDocument(
      <Controls value="error"
                actions={actions} />
    );

    var inputs = TestUtils.scryRenderedDOMComponentsWithTag(ctrl, 'input');
    TestUtils.Simulate.change(inputs[2]);

    expect(actions.changePSMetric.callCount).to.equal(1);
    expect(actions.changePSMetric.getCall(0).args[0]).to.equal('gain');
  });


});