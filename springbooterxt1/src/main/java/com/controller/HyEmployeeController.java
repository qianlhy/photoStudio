package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.HyEmployeeEntity;
import com.service.TokenService;
import com.service.impl.HyEmployeeServiceImpl;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.PasswordUtil;
import com.utils.Query;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * 员工账号 + 三端统一登录
 */
@RestController
@RequestMapping("/hyEmployee")
public class HyEmployeeController {

    @Autowired
    private HyEmployeeServiceImpl service;

    @Autowired
    private TokenService tokenService;

    /** 统一登录（管理端/销售pad端/内容制作） */
    @IgnoreAuth
    @PostMapping("/login")
    public R login(String username, String password, HttpServletRequest request) {
        if (username != null) username = username.trim();
        HyEmployeeEntity emp = service.selectOne(new EntityWrapper<HyEmployeeEntity>().eq("username", username));
        if (emp == null || !PasswordUtil.matches(password, emp.getPassword())) {
            return R.error("账号或密码不正确");
        }
        if ("停用".equals(emp.getStatus())) {
            return R.error("账号已停用，请联系管理员");
        }
        if (!PasswordUtil.isEncoded(emp.getPassword())) {
            emp.setPassword(PasswordUtil.encode(password));
            service.updateById(emp);
        }
        emp.setLastlogin(new Date());
        service.updateById(emp);
        String token = tokenService.generateToken(emp.getId(), username, "hy_employee", emp.getRole());
        return R.ok().put("token", token).put("role", emp.getRole()).put("userId", emp.getId()).put("name", emp.getName());
    }

    /** 当前登录员工信息 */
    @RequestMapping("/session")
    public R session(HttpServletRequest request) {
        Long id = (Long) request.getSession().getAttribute("userId");
        HyEmployeeEntity emp = service.selectById(id);
        if (emp != null) {
            emp.setPassword(null);
        }
        return R.ok().put("data", emp);
    }

    @GetMapping("/logout")
    public R logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return R.ok("退出成功");
    }

    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HyEmployeeEntity entity) {
        EntityWrapper<HyEmployeeEntity> ew = new EntityWrapper<>();
        PageUtils page = new PageUtils(service.selectPage(new Query<HyEmployeeEntity>(params).getPage(),
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, entity), params), params)));
        return R.ok().put("data", page);
    }

    @RequestMapping("/list")
    public R list(HyEmployeeEntity entity) {
        EntityWrapper<HyEmployeeEntity> ew = new EntityWrapper<>();
        ew.allEq(MPUtil.allEQMapPre(entity, "hy_employee"));
        return R.ok().put("data", service.selectList(ew));
    }

    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        return R.ok().put("data", service.selectById(id));
    }

    @PostMapping("/save")
    public R save(@RequestBody HyEmployeeEntity entity) {
        if (entity.getUsername() != null) {
            entity.setUsername(entity.getUsername().trim());
        }
        if (entity.getUsername() == null || entity.getUsername().isEmpty()) {
            return R.error("账号不能为空");
        }
        if (service.selectOne(new EntityWrapper<HyEmployeeEntity>().eq("username", entity.getUsername())) != null) {
            return R.error("账号已存在");
        }
        entity.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        entity.setAddtime(new Date());
        if (entity.getPassword() == null || entity.getPassword().isEmpty()) {
            entity.setPassword(PasswordUtil.encode("123456"));
        } else if (!PasswordUtil.isEncoded(entity.getPassword())) {
            entity.setPassword(PasswordUtil.encode(entity.getPassword()));
        }
        if (entity.getStatus() == null) entity.setStatus("正常");
        service.insert(entity);
        return R.ok();
    }

    @RequestMapping("/update")
    public R update(@RequestBody HyEmployeeEntity entity) {
        if (entity.getPassword() != null && !entity.getPassword().isEmpty() && !PasswordUtil.isEncoded(entity.getPassword())) {
            entity.setPassword(PasswordUtil.encode(entity.getPassword()));
        }
        service.updateById(entity);
        return R.ok();
    }

    @RequestMapping("/resetPass/{id}")
    public R resetPass(@PathVariable("id") Long id) {
        HyEmployeeEntity emp = service.selectById(id);
        if (emp == null) return R.error("员工不存在");
        emp.setPassword(PasswordUtil.encode("123456"));
        service.updateById(emp);
        return R.ok("密码已重置为：123456");
    }

    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        service.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
}
