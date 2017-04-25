/**
 * Created by lunsh on 2016/7/21.
 */
'use strict';
app.controller('LoginOutController', function ($scope, $state, $http, $localStorage, $cookieStore, LoginService) {
    LoginService.logOut().success(function (response) {
        if (response.success) {
            $localStorage.loginName = "";
            $cookieStore.remove('_col_a');
            $state.go('login.login');
        }
    });
});

