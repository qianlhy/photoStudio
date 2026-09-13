package com.service.impl;

import com.dao.HyOperationLogDao;
import com.entity.HyOperationLogEntity;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.utils.HyId;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service("hyOperationLogServiceImpl")
public class HyOperationLogServiceImpl extends ServiceImpl<HyOperationLogDao, HyOperationLogEntity> {

    /** 写操作日志；失败不影响主业务 */
    public void record(String operatorName, String module, String action, String detail) {
        try {
            HyOperationLogEntity e = new HyOperationLogEntity();
            e.setId(HyId.next());
            e.setAddtime(new Date());
            e.setOperatorName(operatorName == null || operatorName.trim().isEmpty() ? "系统" : operatorName.trim());
            e.setModule(module == null ? "" : module);
            e.setAction(action == null ? "" : action);
            e.setDetail(detail == null ? "" : detail);
            if (e.getDetail().length() > 480) {
                e.setDetail(e.getDetail().substring(0, 480) + "…");
            }
            this.insert(e);
        } catch (Exception ignored) {
            // 审计失败不阻断业务
        }
    }

    public void record(HttpServletRequest request, String module, String action, String detail) {
        String op = "系统";
        if (request != null) {
            try {
                Object u = request.getSession().getAttribute("username");
                if (u != null && !String.valueOf(u).trim().isEmpty()) {
                    op = String.valueOf(u).trim();
                }
            } catch (Exception ignored) {
            }
        }
        record(op, module, action, detail);
    }
}
