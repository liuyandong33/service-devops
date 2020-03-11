package build.dream.devops.services;

import build.dream.common.api.ApiRest;
import build.dream.common.domains.admin.$Service;
import build.dream.common.domains.admin.App;
import build.dream.common.domains.admin.JavaOption;
import build.dream.common.utils.*;
import build.dream.devops.constants.Constants;
import build.dream.devops.mappers.ServiceMapper;
import build.dream.devops.models.service.DeployModel;
import build.dream.devops.models.service.ListServicesModel;
import build.dream.devops.models.service.ObtainServiceInfoModel;
import build.dream.devops.models.service.SaveServiceModel;
import build.dream.devops.tasks.DeployTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ServiceService {
    @Autowired
    private ServiceMapper serviceMapper;

    @Transactional(readOnly = true)
    public ApiRest listServices(ListServicesModel listServicesModel) {
        Integer appId = listServicesModel.getAppId();
        Integer page = listServicesModel.getPage();
        Integer rows = listServicesModel.getRows();

        List<SearchCondition> searchConditions = new ArrayList<SearchCondition>();
        searchConditions.add(new SearchCondition($Service.ColumnName.DELETED, Constants.SQL_OPERATION_SYMBOL_EQUAL, 0));
        searchConditions.add(new SearchCondition($Service.ColumnName.APP_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, appId));
        SearchModel searchModel = SearchModel.builder()
                .searchConditions(searchConditions)
                .build();
        Long total = DatabaseHelper.count($Service.class, searchModel);
        List<$Service> services = null;
        if (total > 0) {
            PagedSearchModel pagedSearchModel = PagedSearchModel.builder()
                    .searchConditions(searchConditions)
                    .page(page)
                    .rows(rows)
                    .build();
            services = DatabaseHelper.findAllPaged($Service.class, pagedSearchModel);
        } else {
            services = new ArrayList<$Service>();
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("total", total);
        data.put("rows", services);
        return ApiRest.builder().data(data).message("查询服务列表成功").successful(true).build();
    }

    @Transactional(readOnly = true)
    public ApiRest obtainServiceInfo(ObtainServiceInfoModel obtainServiceInfoModel) {
        Long serviceId = obtainServiceInfoModel.getServiceId();
        $Service service = DatabaseHelper.find($Service.class, serviceId);
        ValidateUtils.notNull(service, "服务不存在！");

        Map<String, Object> data = new HashMap<String, Object>();
        SearchModel javaOptionSearchModel = SearchModel.builder()
                .autoSetDeletedFalse()
                .equal(JavaOption.ColumnName.SERVICE_ID, serviceId)
                .build();
        JavaOption javaOption = DatabaseHelper.find(JavaOption.class, javaOptionSearchModel);
        if (Objects.nonNull(javaOption)) {
            data.put("javaOpts", javaOption.buildJavaOpts());
        }

        List<Map<String, Object>> serviceNodes = serviceMapper.listServiceNodes(serviceId);
        data.put("service", service);
        data.put("nodes", serviceNodes);
        return ApiRest.builder().data(data).message("获取服务信息成功！").successful(true).build();
    }

    @Transactional(rollbackFor = Exception.class)
    public ApiRest saveService(SaveServiceModel saveServiceModel) {
        Long userId = saveServiceModel.obtainUserId();
        Long id = saveServiceModel.getId();
        Long appId = saveServiceModel.getAppId();
        String name = saveServiceModel.getName();
        String programName = saveServiceModel.getProgramName();
        String programVersion = saveServiceModel.getProgramVersion();
        String healthCheckPath = saveServiceModel.getHealthCheckPath();
        Boolean partitioned = saveServiceModel.getPartitioned();
        String deploymentEnvironment = saveServiceModel.getDeploymentEnvironment();
        String partitionCode = saveServiceModel.getPartitionCode();
        String serviceName = saveServiceModel.getServiceName();
        String zookeeperConnectString = saveServiceModel.getZookeeperConnectString();
        Map<String, Object> javaOpts = saveServiceModel.getJavaOpts();

        $Service service = null;
        if (Objects.nonNull(id)) {
            SearchModel searchModel = SearchModel.builder()
                    .autoSetDeletedFalse()
                    .equal(App.ColumnName.ID, id)
                    .build();
            service = DatabaseHelper.find($Service.class, searchModel);
            ValidateUtils.notNull(service, "服务不存在！");

            service.setName(name);
            service.setProgramName(programName);
            service.setProgramVersion(programVersion);
            service.setHealthCheckPath(healthCheckPath);
            service.setPartitioned(partitioned);
            service.setDeploymentEnvironment(deploymentEnvironment);
            service.setPartitionCode(partitioned ? partitionCode : Constants.VARCHAR_DEFAULT_VALUE);
            service.setServiceName(serviceName);
            service.setZookeeperConnectString(zookeeperConnectString);
            service.setUpdatedUserId(userId);
            service.setUpdatedRemark("修改服务信息！");
            DatabaseHelper.update(service);
        } else {
            service = $Service.builder()
                    .appId(appId)
                    .name(name)
                    .programName(programName)
                    .programVersion(programVersion)
                    .healthCheckPath(healthCheckPath)
                    .partitioned(partitioned)
                    .deploymentEnvironment(deploymentEnvironment)
                    .partitionCode(partitioned ? partitionCode : Constants.VARCHAR_DEFAULT_VALUE)
                    .serviceName(serviceName)
                    .zookeeperConnectString(zookeeperConnectString)
                    .createdUserId(userId)
                    .updatedUserId(userId)
                    .updatedRemark("新增服务信息！")
                    .build();
            DatabaseHelper.insert(service);
        }
        Long serviceId = service.getId();

        serviceMapper.deleteJavaOptions(serviceId);
        JavaOption javaOption = JacksonUtils.readValue(JacksonUtils.writeValueAsString(javaOpts), JavaOption.class);
        javaOption.setServiceId(serviceId);
        javaOption.setCreatedUserId(userId);
        javaOption.setUpdatedUserId(userId);
        javaOption.setUpdatedRemark("设置JVM属性！");
        DatabaseHelper.insert(javaOption);
        return ApiRest.builder().message("保存服务成功！").successful(true).build();
    }

    @Transactional(readOnly = true)
    public ApiRest deploy(DeployModel deployModel) {
        Long serviceId = deployModel.getServiceId();

        $Service service = DatabaseHelper.find($Service.class, serviceId);
        ValidateUtils.notNull(service, "服务不存在！");

        SearchModel javaOptionSearchModel = SearchModel.builder()
                .autoSetDeletedFalse()
                .equal(JavaOption.ColumnName.SERVICE_ID, serviceId)
                .build();
        JavaOption javaOption = DatabaseHelper.find(JavaOption.class, javaOptionSearchModel);

        List<Map<String, Object>> serviceNodes = serviceMapper.listServiceNodes(serviceId);
        ValidateUtils.notEmpty(serviceNodes, "服务节点为空！");

        new DeployTask(service, javaOption, serviceNodes).start();
        return ApiRest.builder().message("服务已经开始部署！").successful(true).build();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listServiceNodes(Long serviceId) {
        return serviceMapper.listServiceNodes(serviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateServiceNodeStatusAndPid(Integer status, String pid, Long id) {
        serviceMapper.updateServiceNodeStatusAndPid(status, pid, id);
    }
}
