/**
 * Created by lunsh on 2016/7/21.
 */
'use strict';
app.controller('SigninFormController', function ($rootScope,$scope, $state, $http, $localStorage, LoginService) {
    $scope.login = function (entity) {
        LoginService.login(entity).success(function (response) {
            if (response.success) {
                $rootScope.loginName = entity.loginName;
                $rootScope.loginName=entity.loginName;
                $state.go('app.main',{cache:false});
            }
            else {
                $scope.authError = response.message;
            }
        });
    }
});

