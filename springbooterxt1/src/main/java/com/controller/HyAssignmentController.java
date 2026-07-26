package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyAssignmentEntity;
import com.entity.HyCustomerEntity;
import com.service.impl.HyAssignmentServiceImpl;
import com.service.impl.HyCustomerServiceImpl;
import com.utils.HyId;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.Query;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hyAssignment")
public class HyAssignmentController {

    @Autowired
    private HyAssignmentServiceImpl service;
    @Autowired
    private HyCustomerServiceImpl customerService;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyAssignmentEntity entity) {
        EntityWrapper<HyAssignmentEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        PageUtils page = new PageUtils(service.selectPage(new Query<HyAssignmentEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HyAssignmentEntity entity) {
        EntityWrapper<HyAssignmentEntity> ew = new EntityWrapper<>();
        ew.orderBy("addtime", false);
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    /** 单个/批量客户划拨：记录交接并更新客户归属 */
    @PostMapping("/transfer")
    public R transfer(@RequestBody HyAssignmentEntity entity) {
        Date now = new Date();
        if (entity.getCustomerId() != null) {
            doTransfer(entity.getCustomerId(), entity, now);
        }
        return R.ok();
    }

    /** 批量分流：customerIds + 目标经理 */
    @PostMapping("/batchTransfer")
    public R batchTransfer(@RequestParam("customerIds") Long[] customerIds,
                           @RequestParam("toManagerId") Long toManagerId,
                           @RequestParam("toManagerName") String toManagerName,
                           @RequestParam(value = "operator", required = false) String operator,
                           @RequestParam(value = "remark", required = false) String remark) {
        Date now = new Date();
        if (customerIds != null) {
            for (Long cid : customerIds) {
                HyAssignmentEntity a = new HyAssignmentEntity();
                a.setToManagerId(toManagerId);
                a.setToManagerName(toManagerName);
                a.setOperator(operator);
                a.setRemark(remark);
                doTransfer(cid, a, now);
            }
        }
        return R.ok();
    }

    private void doTransfer(Long customerId, HyAssignmentEntity tpl, Date now) {
        HyCustomerEntity c = customerService.selectById(customerId);
        if (c == null) return;
        HyAssignmentEntity a = new HyAssignmentEntity();
        a.setId(HyId.next());
        a.setAddtime(now);
        a.setCustomerId(customerId);
        a.setCustomerName(c.getName());
        a.setFromManagerId(c.getManagerId());
        a.setFromManagerName(c.getManagerName());
        a.setToManagerId(tpl.getToManagerId());
        a.setToManagerName(tpl.getToManagerName());
        a.setOperator(tpl.getOperator());
        a.setRemark(tpl.getRemark());
        service.insert(a);
        c.setManagerId(tpl.getToManagerId());
        c.setManagerName(tpl.getToManagerName());
        customerService.updateById(c);
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        service.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
