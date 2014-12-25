/** @jsx React.DOM */

require('./react_helper');

var Controls, actions;

describe('Vis method controls', function() {

  beforeEach(function() {
    Controls = require('../VisMethodControls.jsx');
    actions = {
      changePSView: sinon.spy()
    };
  });

  it('has all the methods', function() {
    var ctrl = TestUtils.renderIntoDocument(
      <Controls value="hyperslice"
                actions={actions} />
    );

    var inputs = TestUtils.scryRenderedDOMComponentsWithTag(ctrl, 'input');
    expect(inputs.length).to.equal(2);
  });

  it('has the correct method selected', function() {
    var ctrl = TestUtils.renderIntoDocument(
      <Controls value="hyperslice"
                actions={actions} />
    );

    var inputs = TestUtils.scryRenderedDOMComponentsWithTag(ctrl, 'input');
    expect(inputs[0].getDOMNode().hasAttribute('checked')).to.be.true;
    expect(inputs[1].getDOMNode().hasAttribute('checked')).to.be.false;
  });

  it('changes to the hyperslice view', function() {
    var ctrl = TestUtils.renderIntoDocument(
      <Controls value="splom"
                actions={actions} />
    );

    var inputs = TestUtils.scryRenderedDOMComponentsWithTag(ctrl, 'input');
    TestUtils.Simulate.change(inputs[0]);

    expect(actions.changePSView.callCount).to.equal(1);
    expect(actions.changePSView.getCall(0).args[0]).to.equal('hyperslice');
  });

  it('changes to the splom view', function() {
    var ctrl = TestUtils.renderIntoDocument(
      <Controls value="hyperslice"
                actions={actions} />
    );

    var inputs = TestUtils.scryRenderedDOMComponentsWithTag(ctrl, 'input');
    TestUtils.Simulate.change(inputs[1]);

    expect(actions.changePSView.callCount).to.equal(1);
    expect(actions.changePSView.getCall(0).args[0]).to.equal('splom');
  });

  it('throws an exception on invalid value', function() {
    var testFun = function() {
      TestUtils.renderIntoDocument(
        <Controls value="blah"
                  actions={actions} />
      );
    };

    expect(testFun).to.throw();
  });

});