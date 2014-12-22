/** @jsx React.DOM */

require('./react_helper');

var ResponseSelector, actions;

describe('ResponseSelector', function() {
  beforeEach(function() {
    ResponseSelector = require('../ResponseSelector.jsx');
    actions = {
      changeResponse: sinon.spy()
    };
  });

  it('shows a "None" response', function() {
    var ctrl = TestUtils.renderIntoDocument(
      <ResponseSelector responseId={1} value={"None"}
                        responses={["y1", "y2", "y3"]}
                        actions={actions} />
    );

    var options = TestUtils.scryRenderedDOMComponentsWithTag(ctrl, 'option');
    expect(options[0].getDOMNode().textContent).to.equal('None');
    expect(options[0].getDOMNode().value).to.equal('None');
  });

  it('has options for all responses', function() {
    var ctrl = TestUtils.renderIntoDocument(
      <ResponseSelector responseId={1} value={"None"}
                        responses={["y1", "y2", "y3"]}
                        actions={actions} />
    );

    var options = TestUtils.scryRenderedDOMComponentsWithTag(ctrl, 'option');
    expect(options.length).to.equal(4);
  });

  it('sets the proper response value', function() {
    var ctrl = TestUtils.renderIntoDocument(
      <ResponseSelector responseId={1} value={"y2"}
                        responses={["y1", "y2", "y3"]}
                        actions={actions} />
    );

    var options = TestUtils.scryRenderedDOMComponentsWithTag(ctrl, 'option');

    expect(options[2].getDOMNode().selected).to.be.true;
  });

  xit('emits the proper change event', function() {
    var ctrl = TestUtils.renderIntoDocument(
      <ResponseSelector responseId={1} value={"None"}
                        responses={["y1", "y2", "y3"]}
                        actions={actions} />
    );

    var options = TestUtils.scryRenderedDOMComponentsWithTag(ctrl, 'option');

    // FIXME: generate proper change event
    ctrl.getDOMNode().value = 'y2';
    TestUtils.Simulate.change(ctrl, {target: {value: "y2"}});

    expect(actions.changeResponse.callCount).to.equal(1);
    expect(actions.changeResponse.getCall(0).args[0]).to.equal(1);
    expect(actions.changeResponse.getCall(0).args[1]).to.equal("y2");
  });
})