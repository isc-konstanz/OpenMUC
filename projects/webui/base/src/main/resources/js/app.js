(function () {

    var app = angular.module('openmuc', ['openmuc.auth',
        'openmuc.common',
        'openmuc.constants',
        'openmuc.dashboard',
        'openmuc.filters',
        'openmuc.i18n',
        'openmuc.sessions',
        'ngCookies',
        'cgNotify',
        'ngAnimate',
        'validation.match',
        'ui.router',
        'oc.lazyLoad',
        'ui.bootstrap']);

    angular.module('openmuc.auth', []);
    angular.module('openmuc.common', []);
    angular.module('openmuc.constants', []);
    angular.module('openmuc.dashboard', []);
    angular.module('openmuc.filters', []);
    angular.module('openmuc.sessions', []);

    // TODO: Move me to somewhere else

})();
