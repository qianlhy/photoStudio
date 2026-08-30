package com.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyCustomerEntity;
import com.entity.YonghuEntity;
import com.service.YonghuService;
import com.utils.HyId;
import com.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 客户主数据以 hy_customer 为准；yonghu 仅作小程序 token / 消息 / 收藏等兼容壳。
 */
@Service
public class HyCustomerAccountServiceImpl {

    public static final String AUDIT_APPROVED = "已通过";
    public static final String AUDIT_PENDING = "待审核";
    public static final String AUDIT_REJECTED = "已驳回";

    @Autowired
    private HyCustomerServiceImpl customerService;
    @Autowired
    private YonghuService yonghuService;

    public HyCustomerEntity findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return null;
        String p = phone.trim();
        List<HyCustomerEntity> list = customerService.selectList(new EntityWrapper<HyCustomerEntity>()
                .eq("phone", p)
                .orderBy("addtime", false)
                .last("limit 1"));
        if (list != null && !list.isEmpty()) return list.get(0);
        List<HyCustomerEntity> all = customerService.selectList(new EntityWrapper<HyCustomerEntity>()
                .isNotNull("phone")
                .orderBy("addtime", false)
                .last("limit 500"));
        if (all == null) return null;
        for (HyCustomerEntity c : all) {
            if (phoneMatches(p, c.getPhone())) return c;
        }
        return null;
    }

    public HyCustomerEntity findByOpenid(String openid) {
        if (openid == null || openid.trim().isEmpty()) return null;
        List<HyCustomerEntity> list = customerService.selectList(new EntityWrapper<HyCustomerEntity>()
                .eq("openid", openid.trim())
                .orderBy("addtime", false)
                .last("limit 1"));
        if (list != null && !list.isEmpty()) return list.get(0);
        YonghuEntity y = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("openid", openid.trim()));
        if (y == null) return null;
        String phone = y.getShoujihaoma() != null ? y.getShoujihaoma() : y.getZhanghao();
        HyCustomerEntity c = findByPhone(phone);
        if (c != null && (c.getOpenid() == null || c.getOpenid().trim().isEmpty())) {
            c.setOpenid(openid.trim());
            customerService.updateById(c);
        }
        return c;
    }

    public boolean isApproved(HyCustomerEntity customer) {
        return customer != null && AUDIT_APPROVED.equals(resolveAuditForLogin(customer));
    }

    public boolean isCrmActivatedCustomer(HyCustomerEntity customer) {
        if (customer == null) return false;
        if (customer.getManagerId() != null) return true;
        if (customer.getDealCount() != null && customer.getDealCount() > 0) return true;
        return "已成交".equals(customer.getFollowStatus());
    }

    public String resolveAuditForLogin(HyCustomerEntity customer) {
        if (customer == null) return AUDIT_PENDING;
        if (isCrmActivatedCustomer(customer)) return AUDIT_APPROVED;
        return normalizeAudit(customer.getAuditStatus());
    }

    public HyCustomerEntity ensureLoginReady(HyCustomerEntity customer) {
        if (customer == null) return null;
        if (!isCrmActivatedCustomer(customer)) return customer;
        boolean changed = false;
        if (!AUDIT_APPROVED.equals(normalizeAudit(customer.getAuditStatus()))) {
            customer.setAuditStatus(AUDIT_APPROVED);
            if (customer.getAuditReply() == null) customer.setAuditReply("");
            changed = true;
        }
        if (changed) customerService.updateById(customer);
        syncYonghuShell(customer, "是");
        return customer;
    }

    public String normalizeAudit(String status) {
        if (status == null || status.trim().isEmpty()) return AUDIT_APPROVED;
        if ("是".equals(status)) return AUDIT_APPROVED;
        if ("否".equals(status)) return AUDIT_PENDING;
        if ("驳回".equals(status)) return AUDIT_REJECTED;
        return status.trim();
    }

    public void onAdminSave(HyCustomerEntity entity) {
        if (entity.getAuditStatus() == null || entity.getAuditStatus().trim().isEmpty()) {
            entity.setAuditStatus(AUDIT_APPROVED);
        } else {
            entity.setAuditStatus(normalizeAudit(entity.getAuditStatus()));
        }
        if (AUDIT_APPROVED.equals(entity.getAuditStatus())
                && entity.getPhone() != null && !entity.getPhone().trim().isEmpty()) {
            syncYonghuShell(entity, "是");
        }
    }

    /**
     * 小程序注册申请：写入客户主表（含偏好/品类/openid），yonghu 仅建待审核壳。
     */
    public HyCustomerEntity upsertFromMiniApply(String phone, String name, String contact,
                                                String beizhu, String openid,
                                                String yixiangPinlei, String preference) {
        String p = phone.trim();
        HyCustomerEntity customer = findByPhone(p);
        if (customer == null) {
            customer = new HyCustomerEntity();
            customer.setId(HyId.next());
            customer.setAddtime(new Date());
            customer.setPhone(p);
            customer.setFollowStatus("跟进中");
            customer.setIntention("中");
            customer.setSatisfaction(5);
        }
        boolean existed = customerService.selectById(customer.getId()) != null;
        if (existed && isCrmActivatedCustomer(customer)) {
            throw new IllegalStateException("该手机号已在客户服务中开通，请直接登录");
        }
        customer.setName(name != null && !name.trim().isEmpty() ? name.trim() : p);
        if (contact != null && !contact.trim().isEmpty()) customer.setContact(contact.trim());
        if (openid != null && !openid.trim().isEmpty()) customer.setOpenid(openid.trim());
        if (yixiangPinlei != null && !yixiangPinlei.trim().isEmpty()) {
            customer.setYixiangPinlei(yixiangPinlei.trim());
        }
        String pref = firstNonEmpty(preference, beizhu);
        if (pref != null) customer.setPreference(pref);
        if (!existed || !AUDIT_APPROVED.equals(normalizeAudit(customer.getAuditStatus()))) {
            customer.setAuditStatus(AUDIT_PENDING);
            customer.setAuditReply("");
        }
        if (customer.getAddtime() == null) customer.setAddtime(new Date());
        if (!existed) customerService.insert(customer);
        else customerService.updateById(customer);
        syncYonghuShell(customer, "否");
        return customer;
    }

    public void audit(Long customerId, String auditStatus, String auditReply) {
        HyCustomerEntity customer = customerService.selectById(customerId);
        if (customer == null) throw new IllegalArgumentException("客户不存在");
        String status = normalizeAudit(auditStatus);
        if (!AUDIT_APPROVED.equals(status) && !AUDIT_PENDING.equals(status) && !AUDIT_REJECTED.equals(status)) {
            throw new IllegalArgumentException("审核状态无效");
        }
        customer.setAuditStatus(status);
        customer.setAuditReply(auditReply == null ? "" : auditReply.trim());
        customerService.updateById(customer);
        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) return;
        if (AUDIT_APPROVED.equals(status)) syncYonghuShell(customer, "是");
        else if (AUDIT_REJECTED.equals(status)) syncYonghuShell(customer, "驳回");
        else syncYonghuShell(customer, "否");
    }

    /** 兼容旧名：审核通过时确保登录壳 */
    public YonghuEntity syncYonghuApproved(HyCustomerEntity customer) {
        return syncYonghuShell(customer, "是");
    }

    /**
     * 把客户主表业务字段镜像到 yonghu（token/收藏/消息仍用 yonghu.id）。
     */
    public YonghuEntity syncYonghuShell(HyCustomerEntity customer, String sfsh) {
        if (customer == null || customer.getPhone() == null || customer.getPhone().trim().isEmpty()) return null;
        String phone = customer.getPhone().trim();
        YonghuEntity user = findYonghuByPhone(phone);
        if (user == null) {
            user = new YonghuEntity();
            user.setId(HyId.next());
            user.setAddtime(new Date());
            user.setZhanghao(phone);
            user.setShoujihaoma(phone);
            user.setMima(PasswordUtil.encode(UUID.randomUUID().toString().substring(0, 8)));
        }
        if (customer.getName() != null && !customer.getName().trim().isEmpty()) {
            user.setXingming(customer.getName().trim());
        } else if (customer.getContact() != null && !customer.getContact().trim().isEmpty()) {
            user.setXingming(customer.getContact().trim());
        }
        if (customer.getOpenid() != null && !customer.getOpenid().trim().isEmpty()) {
            user.setOpenid(customer.getOpenid().trim());
        }
        if (customer.getPreference() != null) {
            user.setPianhao(customer.getPreference());
        }
        if (customer.getYixiangPinlei() != null) {
            user.setYixiangpinlei(customer.getYixiangPinlei());
        }
        user.setSfsh(sfsh == null ? "否" : sfsh);
        user.setShhf(customer.getAuditReply() == null ? "" : customer.getAuditReply());
        if (yonghuService.selectById(user.getId()) == null) yonghuService.insert(user);
        else yonghuService.updateById(user);
        return user;
    }

    /**
     * 小程序保存偏好：先写 hy_customer，再镜像 yonghu。
     */
    public HyCustomerEntity savePreferenceByYonghuId(Long yonghuId, String yixiangPinlei, String preference) {
        YonghuEntity user = yonghuService.selectById(yonghuId);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        String phone = firstNonEmpty(user.getShoujihaoma(), user.getZhanghao());
        if (phone == null) throw new IllegalArgumentException("用户未绑定手机号");
        HyCustomerEntity customer = findByPhone(phone);
        if (customer == null) {
            customer = new HyCustomerEntity();
            customer.setId(HyId.next());
            customer.setAddtime(new Date());
            customer.setPhone(phone.trim());
            customer.setName(user.getXingming() != null ? user.getXingming() : phone);
            customer.setFollowStatus("跟进中");
            customer.setIntention("中");
            customer.setSatisfaction(5);
            customer.setAuditStatus(AUDIT_APPROVED);
            customerService.insert(customer);
        }
        if (yixiangPinlei != null) customer.setYixiangPinlei(yixiangPinlei.trim());
        if (preference != null) customer.setPreference(preference.trim());
        customerService.updateById(customer);
        String sfsh = AUDIT_APPROVED.equals(resolveAuditForLogin(customer)) ? "是"
                : AUDIT_REJECTED.equals(normalizeAudit(customer.getAuditStatus())) ? "驳回" : "否";
        syncYonghuShell(customer, sfsh);
        return customer;
    }

    /** session 返回前：用客户主表覆盖偏好/品类/openid（前端仍读 pianhao） */
    public void enrichYonghuFromCustomer(YonghuEntity user) {
        if (user == null) return;
        String phone = firstNonEmpty(user.getShoujihaoma(), user.getZhanghao());
        if (phone == null) return;
        HyCustomerEntity customer = findByPhone(phone);
        if (customer == null) return;
        if (customer.getPreference() != null) user.setPianhao(customer.getPreference());
        if (customer.getYixiangPinlei() != null) user.setYixiangpinlei(customer.getYixiangPinlei());
        if (customer.getOpenid() != null && !customer.getOpenid().trim().isEmpty()) {
            user.setOpenid(customer.getOpenid());
        }
        if ((user.getXingming() == null || user.getXingming().trim().isEmpty())
                && customer.getName() != null) {
            user.setXingming(customer.getName());
        }
    }

    /** 后台查看：主表已是 SoT；若仍空则从旧 yonghu 回填一次 */
    public void fillPreferenceFromYonghu(HyCustomerEntity customer) {
        if (customer == null) return;
        boolean needPref = customer.getPreference() == null || customer.getPreference().trim().isEmpty();
        boolean needPinlei = customer.getYixiangPinlei() == null || customer.getYixiangPinlei().trim().isEmpty();
        boolean needOpenid = customer.getOpenid() == null || customer.getOpenid().trim().isEmpty();
        if (!needPref && !needPinlei && !needOpenid) return;
        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) return;
        YonghuEntity user = findYonghuByPhone(customer.getPhone().trim());
        if (user == null) return;
        boolean changed = false;
        if (needPref && user.getPianhao() != null && !user.getPianhao().trim().isEmpty()) {
            customer.setPreference(user.getPianhao().trim());
            changed = true;
        }
        if (needPinlei && user.getYixiangpinlei() != null && !user.getYixiangpinlei().trim().isEmpty()) {
            customer.setYixiangPinlei(user.getYixiangpinlei().trim());
            changed = true;
        }
        if (needOpenid && user.getOpenid() != null && !user.getOpenid().trim().isEmpty()) {
            customer.setOpenid(user.getOpenid().trim());
            changed = true;
        }
        if (changed) customerService.updateById(customer);
    }

    /** @deprecated 保留调用点；已改为 savePreferenceByYonghuId */
    public void syncPreferenceFromYonghu(YonghuEntity user) {
        if (user == null || user.getId() == null) return;
        savePreferenceByYonghuId(user.getId(), user.getYixiangpinlei(), user.getPianhao());
    }

    public YonghuEntity findYonghuByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return null;
        YonghuEntity user = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("shoujihaoma", phone.trim()));
        if (user == null) {
            user = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("zhanghao", phone.trim()));
        }
        return user;
    }

    private String firstNonEmpty(String a, String b) {
        if (a != null && !a.trim().isEmpty()) return a.trim();
        if (b != null && !b.trim().isEmpty()) return b.trim();
        return null;
    }

    private boolean phoneMatches(String userPhone, String customerPhone) {
        if (customerPhone == null || customerPhone.isEmpty()) return false;
        if (userPhone.equals(customerPhone)) return true;
        String ud = userPhone.replaceAll("\\D", "");
        String cd = customerPhone.replaceAll("\\D", "");
        return ud.length() >= 4 && cd.length() >= 4 && ud.endsWith(cd.substring(Math.max(0, cd.length() - 4)));
    }
}
