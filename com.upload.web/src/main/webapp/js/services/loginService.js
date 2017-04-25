/**
 * Created by shilun on 2016/8/2.
 */
app.factory('LoginService', function ($http) {
    var login = function (user) {
        return $http({method: 'POST', url: "/login/login", params: user});
    };
    var changePass = function (user) {
        return $http({method: 'POST', url: '/login/changePass', params: user});
    };
    var logOut = function () {
        return $http({method: 'POST', url: '/login/loginOut'});
    };
    return {
        login: function (user) {
            return login(user);
        },
        changePass: function (user) {
            return changePass(user);
        },
        logOut: function () {
            return logOut();
        }
    }
});