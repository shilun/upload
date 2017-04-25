/**
 * 获取状态与性别的公共服务
 * @param type
 */
app.factory('CommonService', function ($http) {
    var buildGlossery = function(type){
        return $http.get('/glossery/buildGlossery?type=' + type,null);
    }
    return {
        buildGlossery:function(type){
            return buildGlossery(type);
        }
    }
});