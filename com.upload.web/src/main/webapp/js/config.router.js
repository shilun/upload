'use strict';

/**
 * Config for the router
 */
angular.module('app')
    .run(
        ['$rootScope', '$state', '$stateParams',
            function ($rootScope, $state, $stateParams) {
                $rootScope.$state = $state;
                $rootScope.$stateParams = $stateParams;
            }
        ]
    )
    .config(
        ['$stateProvider', '$urlRouterProvider',
            function ($stateProvider, $urlRouterProvider) {
                var timestamp = Date.parse(new Date());
                $urlRouterProvider.otherwise('/app/main');
                $stateProvider
                    .state('app', {
                        url: '/app',
                        templateUrl: 'tpl/app.html'
                    })
                    .state('app.config', {
                        url: '/config',
                        templateUrl: '/ui/config'
                    })
                    .state('app.configView', {
                        url: '/configView/:id',
                        templateUrl: '/ui/configView'
                    })
                    .state('login', {
                        url: '/login',
                        template: '<div ui-view class="fade-in-right-big smooth"></div>'
                    })
                    .state('login.login', {
                        url: '/login',
                        templateUrl: 'ui/login',
                        resolve: {
                            deps: ['$ocLazyLoad',
                                function ($ocLazyLoad) {
                                    return $ocLazyLoad.load(['js/controllers/login/login.js?v='+timestamp]);
                                }]
                        }
                    })
                    /** 密码修改 */
                    .state('login.changePass', {
                        url: '/changePass',
                        templateUrl: 'ui/changePass',
                        resolve: {
                            deps: ['$ocLazyLoad',
                                function ($ocLazyLoad) {
                                    return $ocLazyLoad.load(['js/controllers/login/changePass.js?v='+timestamp]);
                                }]
                        }
                    })
                    /** 退出 */
                    .state('login.loginOut', {
                        url: '/loginOut',
                        templateUrl: 'ui/loginOut',
                        resolve: {
                            deps: ['$ocLazyLoad',
                                function ($ocLazyLoad) {
                                    return $ocLazyLoad.load(['js/controllers/login/loginOut.js?v='+timestamp]);
                                }]
                        }
                    })
            }
        ]
    )