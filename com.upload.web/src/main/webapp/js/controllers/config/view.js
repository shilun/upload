/**
 * Dav
 */
'use strict';
app.controller('viewCfgCtrl', function ($stateParams, $scope, $state, $http, CommonService, ConfigService) {

    //下拉框显示状态
    CommonService.buildGlossery('yesOrNo').success(function (response) {
        $scope.statuses = response.data.list;
    });
    var refId = $stateParams.id;
    if (refId != null) {
        ConfigService.findById(refId).success(function (response) {
            $scope.entity = response.data;
        });
    }

    //提交编辑
    $scope.submit = function (entity) {
        ConfigService.save(entity).statuses(function(response){
            if(response.success){
                $state.go('app.config');
            }
            else{
                alert(response.message);
            }
        });
    }
});