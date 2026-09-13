package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyCustomerEntity;
import com.entity.HyFollowTaskEntity;
import com.entity.HyOrderEntity;
import com.entity.YonghuEntity;
import com.service.YonghuService;
import com.service.impl.HyCustomerAccountServiceImpl;
import com.service.impl.HyCustomerServiceImpl;
import com.service.impl.HyDealServiceImpl;
import com.service.impl.HyFollowTaskServiceImpl;
import com.service.impl.HyOrderServiceImpl;
import com.utils.HyId;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.Query;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hyCustomer")
public class HyCustomerController {

    @Autowired
    private HyCustomerServiceImpl service;
    @Autowired
    private HyDealServiceImpl dealService;
    @Autowired
    private HyOrderServiceImpl orderService;
    @Autowired
    private YonghuService yonghuService;
    @Autowired
    private HyFollowTaskServiceImpl followTaskService;
    @Autowired
    private HyCustomerAccountServiceImpl accountService;
    @Autowired
    private com.service.impl.HyOperationLogServiceImpl operationLogService;

    @IgnoreAuth
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyCustomerEntity entity) {
        EntityWrapper<HyCustomerEntity> ew = new EntityWrapper<>();
        PageUtils page = new PageUtils(service.selectPage(new Query<HyCustomerEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @IgnoreAuth
    @RequestMapping("/list")
    public R list(HyCustomerEntity entity) {
        EntityWrapper<HyCustomerEntity> ew = new EntityWrapper<>();
        return R.ok().put("data", service.selectList(MPUtil.likeOrEq(ew, entity)));
    }

    @IgnoreAuth
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        HyCustomerEntity customer = service.selectById(id);
        if (customer != null) {
            accountService.fillPreferenceFromYonghu(customer);
        }
        return R.ok().put("data", customer);
    }

    /**
     * 客户小程序按手机号绑定合意客户档案（需登录）。
     * 登录用户为 yonghu 时，强制使用 session 手机号，防止冒用他人档案。
     */
    @RequestMapping("/bindByPhone")
    public R bindByPhone(@RequestParam(required = false) String phone, HttpServletRequest request) {
        Object tableName = request.getSession().getAttribute("tableName");
        Object userId = request.getSession().getAttribute("userId");
        if ("yonghu".equals(tableName) && userId != null) {
            YonghuEntity u = yonghuService.selectById(Long.valueOf(String.valueOf(userId)));
            if (u != null && u.getShoujihaoma() != null && !u.getShoujihaoma().trim().isEmpty()) {
                phone = u.getShoujihaoma().trim();
            }
        }
        if (phone == null || phone.trim().isEmpty()) {
            return R.error("手机号不能为空");
        }
        String p = phone.trim();
        List<HyCustomerEntity> list = service.selectList(new EntityWrapper<HyCustomerEntity>()
                .eq("phone", p)
                .orderBy("addtime", false)
                .last("limit 1"));
        if (list == null || list.isEmpty()) {
            List<HyCustomerEntity> all = service.selectList(new EntityWrapper<HyCustomerEntity>()
                    .isNotNull("phone")
                    .orderBy("addtime", false)
                    .last("limit 200"));
            if (all != null) {
                for (HyCustomerEntity c : all) {
                    if (phoneMatches(p, c.getPhone())) {
                        list = java.util.Collections.singletonList(c);
                        break;
                    }
                }
            }
        }
        if (list == null || list.isEmpty()) {
            return R.error("未找到与该手机号关联的客户服务，请联系服务经理激活");
        }
        return R.ok().put("data", list.get(0));
    }

    /** 精确匹配，或脱敏号前缀+后缀匹配 */
    private boolean phoneMatches(String userPhone, String customerPhone) {
        if (customerPhone == null || customerPhone.isEmpty()) return false;
        if (userPhone.equals(customerPhone)) return true;
        if (customerPhone.contains("*") && userPhone.length() >= 7) {
            int star = customerPhone.indexOf('*');
            int lastStar = customerPhone.lastIndexOf('*');
            String prefix = customerPhone.substring(0, star);
            String suffix = customerPhone.substring(lastStar + 1);
            return userPhone.startsWith(prefix) && userPhone.endsWith(suffix);
        }
        String ud = userPhone.replaceAll("\\D", "");
        String cd = customerPhone.replaceAll("\\D", "");
        return ud.length() >= 4 && cd.length() >= 4 && ud.endsWith(cd.substring(Math.max(0, cd.length() - 4)));
    }

    /**
     * 确认收款：付款成功后自动生成订单与内容清单（订单无「待付款」状态）。
     */
    @PostMapping("/confirmPay")
    public R confirmPay(@RequestBody Map<String, Object> body) {
        Object cid = body.get("customerId");
        if (cid == null) return R.error("customerId 必填");
        Long customerId = Long.valueOf(String.valueOf(cid));
        Long planId = null;
        if (body.get("planId") != null && !"".equals(String.valueOf(body.get("planId")))) {
            planId = Long.valueOf(String.valueOf(body.get("planId")));
        }
        try {
            Long orderId = dealService.confirmPayAndCreateOrder(customerId, planId);
            HyOrderEntity order = orderService.selectById(orderId);
            return R.ok().put("orderId", orderId).put("data", order);
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
    }

    @PostMapping("/save")
    public R save(@RequestBody HyCustomerEntity entity, HttpServletRequest request) {
        entity.setId(HyId.next());
        entity.setAddtime(new Date());
        accountService.onAdminSave(entity);
        service.insert(entity);
        createTodayReceptionTask(entity);
        operationLogService.record(request, "客户管理", "新增",
                "客户：" + (entity.getName() == null ? entity.getId() : entity.getName()));
        return R.ok().put("id", entity.getId());
    }

    /** 新建客户后，为业务经理生成今日接待待办 */
    private void createTodayReceptionTask(HyCustomerEntity entity) {
        if (entity.getManagerId() == null) {
            return;
        }
        HyFollowTaskEntity task = new HyFollowTaskEntity();
        task.setId(HyId.next());
        task.setAddtime(new Date());
        task.setCustomerId(entity.getId());
        task.setCustomerName(entity.getName());
        task.setOwnerId(entity.getManagerId());
        task.setOwnerName(entity.getManagerName());
        task.setTaskTime(new Date());
        task.setAction("开始接待");
        task.setStatus("待办");
        followTaskService.insert(task);
    }

    @RequestMapping("/update")
    public R update(@RequestBody HyCustomerEntity entity, HttpServletRequest request) {
        HyCustomerEntity old = service.selectById(entity.getId());
        if (old != null && (entity.getAuditStatus() == null || entity.getAuditStatus().trim().isEmpty())) {
            entity.setAuditStatus(old.getAuditStatus());
        }
        if (old != null) {
            if (entity.getOpenid() == null) entity.setOpenid(old.getOpenid());
            if (entity.getYixiangPinlei() == null) entity.setYixiangPinlei(old.getYixiangPinlei());
            if (entity.getPreference() == null) entity.setPreference(old.getPreference());
            if (entity.getSelectTarget() == null) entity.setSelectTarget(old.getSelectTarget());
        }
        if (entity.getSelectTarget() == null || entity.getSelectTarget() < 1) {
            entity.setSelectTarget(15);
        }
        accountService.onAdminSave(entity);
        service.updateById(entity);
        // 后台改偏好/品类后镜像到登录壳，小程序 session 立刻一致
        String audit = accountService.resolveAuditForLogin(entity);
        String sfsh = HyCustomerAccountServiceImpl.AUDIT_APPROVED.equals(audit) ? "是"
                : HyCustomerAccountServiceImpl.AUDIT_REJECTED.equals(audit) ? "驳回" : "否";
        accountService.syncYonghuShell(entity, sfsh);
        String name = entity.getName() != null ? entity.getName() : (old != null ? old.getName() : String.valueOf(entity.getId()));
        operationLogService.record(request, "客户管理", "修改", "客户：" + name);
        return R.ok();
    }

    /** 审核小程序注册客户（通过/驳回）；通过时可指定业务经理供 Pad 可见 */
    @PostMapping("/audit")
    public R audit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Object idObj = body.get("id");
        if (idObj == null) return R.error("客户 id 必填");
        String auditStatus = body.get("auditStatus") == null ? "" : String.valueOf(body.get("auditStatus"));
        String auditReply = body.get("auditReply") == null ? "" : String.valueOf(body.get("auditReply"));
        Long managerId = null;
        if (body.get("managerId") != null && !"".equals(String.valueOf(body.get("managerId")))) {
            managerId = Long.valueOf(String.valueOf(body.get("managerId")));
        }
        String managerName = body.get("managerName") == null ? null : String.valueOf(body.get("managerName"));
        try {
            Long cid = Long.valueOf(String.valueOf(idObj));
            accountService.audit(cid, auditStatus, auditReply, managerId, managerName);
            HyCustomerEntity c = service.selectById(cid);
            String nm = c != null && c.getName() != null ? c.getName() : String.valueOf(cid);
            operationLogService.record(request, "客户管理", "审核",
                    "客户：" + nm + " → " + auditStatus + (managerName != null ? "，经理：" + managerName : ""));
            return R.ok();
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
        service.deleteBatchIds(Arrays.asList(ids));
        operationLogService.record(request, "客户管理", "删除",
                "客户ID：" + (ids == null ? "" : Arrays.toString(ids)));
        return R.ok();
    }
}
