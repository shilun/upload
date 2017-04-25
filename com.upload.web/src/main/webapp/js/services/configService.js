/**
 * Created by Administrator on 2016/8/2.
 */

app.factory('ConfigService', ['$http', function ($http) {
    var list = function (data) {
        return $http({method: 'POST', url: "/config/list", params: data});
    };
    var findById = function (id) {
        return $http({method: 'POST', url: "/config/view", params: id});
    };
    var save=function(data){
        return $http({method: 'POST', url: "/config/save", params: data});
    };
    return {
        list: function (data) {
            return list(data);
        },
        findById: function (id) {
            return findById(id);
        },
        save:function(data){
            return save(data);
        }
    }
}]);