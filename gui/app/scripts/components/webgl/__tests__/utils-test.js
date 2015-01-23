
var utils = require('../utils');

describe('webgl utilities', function() {

  describe('hyperslice layout', function() {
    var hsLayout = utils.hypersliceLayout;

    it('gives an error on less than 3 dimensions');

    it('does not return anything for the matching pairs of dimensions', function() {
      var layout = hsLayout(4, 300, 300, 5);
      /*
       * don't know why this doesn't work...
      expect(layout).to.include.satisfy(function(l) {
        return l.xDim !== l.yDim;
      });
      */
      for(var i in layout) {
        expect(layout[i]).to.satisfy(function(l) {
          //console.log("" + i + " x " + l.xDim + " y " + l.yDim);
          return l.xDim != l.yDim;
        });
      }
    });

    it('puts the first dimension in the correct place', function() {
      var layout = hsLayout(4, 415, 415, 5);
      var firstMtx = layout[0].transMtx;
      expect(firstMtx[12]).to.equal(0);
      expect(firstMtx[13]).to.equal(105);
    });

    it('puts the plots where they belong given a square area');
  });
});