/**
 * Created by lunshi on 2016/7/21.
 */
'use strict';
app.controller('changePassController', function ($scope, $state, $http, $localStorage, $translate, $cookieStore, LoginService) {
    $scope.submit = function (entity) {
        if (entity.newPass != entity.vNewPass) {
            alert(label);
            return;
        }
        LoginService.changePass(entity).success(function (response) {
            if (response.success) {
                $localStorage.loginName = $scope.loginName;
                $cookieStore.remove('_col_a');
                $state.go('common.login');
            }
            else {
                alert(response.message);
            }
        });
    }
});

