/**
 * Created by lunsh on 2016/7/21.
 */
'use strict';
app.controller('listCfgCtrl', ['$rootScope', '$scope', '$state', '$http','CommonService','ConfigService', function ($rootScope, $scope, $state, $http,CommonService,ConfigService) {
    CommonService.buildGlossery('yesOrNo').success(function(response){
        $scope.statuses = response.data.list;
    });
    var loadListData = function () {
        var postData = {
            page: $scope.paginationConf.currentPage - 1,
            size: $scope.paginationConf.itemsPerPage,
            status: $scope.status
        }
        ConfigService.list(postData).success(function (response) {
            $scope.paginationConf.totalItems = response.data.totalCount;
            $scope.listData = response.data.list;
        });
    }
    //配置分页基本参数
    $scope.paginationConf = {
        currentPage: 0,
        itemsPerPage: pageSize
    };
    $scope.$watch('paginationConf.currentPage + paginationConf.itemsPerPage', loadListData);
    //条件查询
    $scope.serch = function(){
        loadListData();
    };


}]);

