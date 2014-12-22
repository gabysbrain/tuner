
var Cortex = require('cortexjs');

describe('Gui actions', function() {
  var Actions = require('../GuiActions');
  var proj, actions;

  beforeEach(function() {
    proj = new Cortex(Factory.build('viewableProject'));
    actions = Actions(proj);
  });

  it('change slice', function() {
    var slice1 = proj.currentVis.currentSlice[0];
    expect(slice1.value.val()).to.equal(0.5);

    actions.changeSlice(slice1.name, 0.4);

    expect(slice1.value.val()).to.equal(0.4);
  });

  it('change zoom', function() {
    var zoom1 = proj.currentVis.currentZoom[0];
    expect(zoom1.lowValue.val()).to.equal(0.2);
    expect(zoom1.highValue.val()).to.equal(0.8);

    actions.changeZoom(zoom1.name, 0.4, 0.6);

    expect(zoom1.lowValue.val()).to.equal(0.4);
    expect(zoom1.highValue.val()).to.equal(0.6);
  });

  it('change parameter space view', function() {
    expect(proj.currentVis.currentVis.val()).to.equal('hyperslice');

    actions.changePSView('splom');

    expect(proj.currentVis.currentVis.val()).to.equal('splom');
  });

  it('change param view type', function() {
    expect(proj.currentVis.currentMetric.val()).to.equal('value');

    actions.changePSMetric('error');

    expect(proj.currentVis.currentMetric.val()).to.equal('error');
  });

  it('change response 1', function() {
    actions.changeResponse(1, 'y2');

    expect(proj.currentVis.response1.val()).to.equal('y2');
  });

  it('change response 2', function() {
    actions.changeResponse(1, 'y1');
    
    expect(proj.currentVis.response1.val()).to.equal('y1');
  });

  it('show history');
  
  it('show pending tasks');

  it('start add samples');

  it('change response 1 filter');

  it('change response 2 filter');
});
