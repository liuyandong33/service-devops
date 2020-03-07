package build.dream.admin.controllers;

import build.dream.admin.models.service.ListServicesModel;
import build.dream.admin.models.service.ObtainServiceInfoModel;
import build.dream.admin.services.ServiceService;
import build.dream.common.annotations.ApiRestAction;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/service")
public class ServiceController {
    @RequestMapping(value = "/listServices", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiRestAction(modelClass = ListServicesModel.class, serviceClass = ServiceService.class, serviceMethodName = "listServices", error = "查询服务列表失败")
    public String listServices() {
        return null;
    }

    @RequestMapping(value = "/obtainServiceInfo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @ApiRestAction(modelClass = ObtainServiceInfoModel.class, serviceClass = ServiceService.class, serviceMethodName = "obtainServiceInfo", error = "获取服务信息失败")
    public String obtainServiceInfo() {
        return null;
    }
}
