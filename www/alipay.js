var exec = require('cordova/exec');

var PLUGIN_NAME = 'Alipay';

var Alipay = {
  pay: function(arg0, success, error) {
    exec(success, error, PLUGIN_NAME, 'pay', [arg0]);
  }
};

module.exports = Alipay;
