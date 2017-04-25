'use strict';
angular.module('app')
    .controller('AppCtrl', function ($rootScope, $scope, $state, $http, $localStorage, $window) {
        $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
            if ($rootScope.loginName == "" || $rootScope.loginName == null) {
                $http({method: 'POST', url: "/login/checkLogin"})
                    .success(function (response) {
                        if (!response.data) {
                            $state.go('login.login');
                        }
                    });
            }
        });
        var isIE = !!navigator.userAgent.match(/MSIE/i);
        isIE && angular.element($window.document.body).addClass('ie');
        isSmartDevice($window) && angular.element($window.document.body).addClass('smart');
        $scope.app = {
            name: 'MAIN SEMEN',
            version: '1.0',
            color: {
                primary: '#7266ba',
                info: '#23b7e5',
                success: '#27c24c',
                warning: '#fad733',
                danger: '#f05050',
                light: '#e8eff0',
                dark: '#3a3f51',
                black: '#1c2b36'
            },
            settings: {
                themeID: 8,
                navbarHeaderColor: 'bg-info dker',
                navbarCollapseColor: 'bg-info dker',
                asideColor: 'bg-light dker b-r',
                headerFixed: true,
                asideFixed: false,
                asideFolded: false,
                asideDock: false,
                container: false
            }
        }
        if (angular.isDefined($localStorage.settings)) {
            $scope.app.settings = $localStorage.settings;
        } else {
            $localStorage.settings = $scope.app.settings;
        }
        $scope.$watch('app.settings', function () {
            if ($scope.app.settings.asideDock && $scope.app.settings.asideFixed) {
                $scope.app.settings.headerFixed = true;
            }
            $localStorage.settings = $scope.app.settings;
        }, true);
        function isSmartDevice($window) {
            var ua = $window['navigator']['userAgent'] || $window['navigator']['vendor'] || $window['opera'];
            return (/iPhone|iPod|iPad|Silk|Android|BlackBerry|Opera Mini|IEMobile/).test(ua);
        }
    });