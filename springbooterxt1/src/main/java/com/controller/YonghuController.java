package com.controller;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.YonghuEntity;
import com.entity.view.YonghuView;
import com.service.TokenService;
import com.service.YonghuService;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.PasswordUtil;
import com.utils.R;
import com.utils.SmsRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户
 * 后端接口
 *
 * @author
 * @email

 */
@RestController
@RequestMapping("/yonghu")
public class YonghuController {
    @Autowired
    private YonghuService yonghuService;


    @Autowired
    private TokenService tokenService;

    @Value("${wx.appid:}")
    private String wxAppid;

    @Value("${wx.secret:}")
    private String wxSecret;

    @Value("${sms.mock-enabled:true}")
    private boolean smsMockEnabled;

    @Value("${sms.mock-code:123456}")
    private String smsMockCode;

    /**
     * 短信验证码临时存储（真实网关模式使用）：phone -> code|expireTime
     */
    private static final Map<String, String> SMS_CODES = new ConcurrentHashMap<>();

    /**
     * 登录（账号密码，保留给管理端/兼容旧逻辑）
     */
    @IgnoreAuth
    @RequestMapping(value = "/login")
    public R login(String username, String password, String captcha, HttpServletRequest request) {
        YonghuEntity user = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("zhanghao", username));
        if (user == null || !PasswordUtil.matches(password, user.getMima())) {
            return R.error("账号或密码不正确");
        }
        if ("否".equals(user.getSfsh())) return R.error("账号已锁定，请联系管理员审核。");
        // 老明文密码登录成功后自动升级为加盐密文
        if (!PasswordUtil.isEncoded(user.getMima())) {
            user.setMima(PasswordUtil.encode(password));
            yonghuService.updateById(user);
        }
        String token = tokenService.generateToken(user.getId(), username, "yonghu", "用户");
        return R.ok().put("token", token);
    }

    /**
     * 微信小程序登录：前端 uni.login 取 code，后端用 code 换 openid，按 openid 找/建用户
     */
    @IgnoreAuth
    @RequestMapping(value = "/wxlogin")
    public R wxlogin(String code) {
        if (code == null || code.trim().isEmpty()) {
            return R.error("缺少微信登录凭证 code");
        }
        if (wxAppid == null || wxAppid.trim().isEmpty() || "your_wx_appid".equals(wxAppid)) {
            return R.error("尚未配置微信小程序 AppID/AppSecret（application.yml: wx.appid / wx.secret）");
        }
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + wxAppid
                + "&secret=" + wxSecret + "&js_code=" + code + "&grant_type=authorization_code";
        String resp;
        try {
            resp = HttpUtil.get(url);
        } catch (Exception e) {
            return R.error("调用微信接口失败：" + e.getMessage());
        }
        JSONObject json = JSON.parseObject(resp);
        String openid = json == null ? null : json.getString("openid");
        if (openid == null || openid.isEmpty()) {
            String errmsg = json == null ? resp : json.getString("errmsg");
            return R.error("微信登录失败：" + errmsg);
        }
        YonghuEntity user = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("openid", openid));
        boolean isNew = false;
        if (user == null) {
            user = new YonghuEntity();
            user.setId(new Date().getTime());
            user.setOpenid(openid);
            user.setZhanghao("wx_" + openid);
            user.setMima(PasswordUtil.encode(UUID.randomUUID().toString().substring(0, 8)));
            user.setXingming("微信用户");
            user.setSfsh("是");
            yonghuService.insert(user);
            isNew = true;
        }
        String token = tokenService.generateToken(user.getId(), user.getZhanghao(), "yonghu", "用户");
        boolean needPreference = isNew || user.getPianhao() == null || user.getPianhao().isEmpty();
        return R.ok().put("token", token).put("needPreference", needPreference);
    }

    /**
     * 发送手机号短信验证码
     */
    @IgnoreAuth
    @RequestMapping(value = "/sendSmsCode")
    public R sendSmsCode(String phone) {
        if (phone == null || phone.trim().length() < 6) {
            return R.error("请输入正确的手机号");
        }
        String limited = SmsRateLimiter.tryAcquire(phone.trim());
        if (limited != null) {
            return R.error(limited);
        }
        if (smsMockEnabled) {
            // 开发模拟模式：不实际下发，固定验证码
            return R.ok("验证码已发送（开发模式，固定为 " + smsMockCode + "）");
        }
        // 真实模式：生成随机验证码，5分钟有效（此处留待接入真实短信网关下发）
        String c = String.valueOf((int) ((Math.random() * 900000) + 100000));
        SMS_CODES.put(phone, c + "|" + (System.currentTimeMillis() + 5 * 60 * 1000));
        // TODO: 调用真实短信网关，将 c 下发到 phone
        return R.ok("验证码已发送");
    }

    /**
     * 手机号 + 短信验证码登录（首次自动注册）
     */
    @IgnoreAuth
    @RequestMapping(value = "/smslogin")
    public R smslogin(String phone, String code) {
        if (phone == null || phone.trim().isEmpty() || code == null || code.trim().isEmpty()) {
            return R.error("请输入手机号和验证码");
        }
        boolean ok;
        if (smsMockEnabled) {
            ok = smsMockCode.equals(code.trim());
        } else {
            String v = SMS_CODES.get(phone);
            ok = v != null
                    && v.split("\\|")[0].equals(code.trim())
                    && Long.parseLong(v.split("\\|")[1]) > System.currentTimeMillis();
            if (ok) {
                SMS_CODES.remove(phone);
            }
        }
        if (!ok) {
            return R.error("验证码错误或已过期");
        }
        YonghuEntity user = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("shoujihaoma", phone));
        boolean isNew = false;
        if (user == null) {
            user = new YonghuEntity();
            user.setId(new Date().getTime());
            user.setZhanghao(phone);
            user.setShoujihaoma(phone);
            user.setMima(PasswordUtil.encode(UUID.randomUUID().toString().substring(0, 8)));
            user.setXingming("用户" + phone.substring(Math.max(0, phone.length() - 4)));
            user.setSfsh("是");
            yonghuService.insert(user);
            isNew = true;
        }
        String token = tokenService.generateToken(user.getId(), user.getZhanghao(), "yonghu", "用户");
        boolean needPreference = isNew || user.getPianhao() == null || user.getPianhao().isEmpty();
        return R.ok().put("token", token).put("needPreference", needPreference);
    }

    /**
     * 注册
     */
    @IgnoreAuth
    @RequestMapping("/register")
    public R register(@RequestBody YonghuEntity yonghu) {
        //ValidatorUtils.validateEntity(yonghu);
        YonghuEntity user = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("zhanghao", yonghu.getZhanghao()));
        if (user != null) {
            return R.error("注册用户已存在");
        }
        Long uId = new Date().getTime();
        yonghu.setId(uId);
        if (yonghu.getMima() != null && !PasswordUtil.isEncoded(yonghu.getMima())) {
            yonghu.setMima(PasswordUtil.encode(yonghu.getMima()));
        }
        yonghuService.insert(yonghu);
        return R.ok();
    }


    /**
     * 退出
     */
    @RequestMapping("/logout")
    public R logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return R.ok("退出成功");
    }

    /**
     * 获取用户的session用户信息
     */
    @RequestMapping("/session")
    public R getCurrUser(HttpServletRequest request) {
        Long id = (Long) request.getSession().getAttribute("userId");
        YonghuEntity user = yonghuService.selectById(id);
        return R.ok().put("data", user);
    }

    /**
     * 密码重置
     */
    @IgnoreAuth
    @RequestMapping(value = "/resetPass")
    public R resetPass(String username, HttpServletRequest request) {
        YonghuEntity user = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("zhanghao", username));
        if (user == null) {
            return R.error("账号不存在");
        }
        user.setMima(PasswordUtil.encode("123456"));
        yonghuService.updateById(user);
        return R.ok("密码已重置为：123456");
    }


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, YonghuEntity yonghu,
                  HttpServletRequest request) {

        EntityWrapper<YonghuEntity> ew = new EntityWrapper<YonghuEntity>();
        PageUtils page = yonghuService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, yonghu), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 前端列表
     */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, YonghuEntity yonghu,
                  HttpServletRequest request) {
        EntityWrapper<YonghuEntity> ew = new EntityWrapper<YonghuEntity>();
        PageUtils page = yonghuService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, yonghu), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/lists")
    public R list(YonghuEntity yonghu) {
        EntityWrapper<YonghuEntity> ew = new EntityWrapper<YonghuEntity>();
        ew.allEq(MPUtil.allEQMapPre(yonghu, "yonghu"));
        return R.ok().put("data", yonghuService.selectListView(ew));
    }

    /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(YonghuEntity yonghu) {
        EntityWrapper<YonghuEntity> ew = new EntityWrapper<YonghuEntity>();
        ew.allEq(MPUtil.allEQMapPre(yonghu, "yonghu"));
        YonghuView yonghuView = yonghuService.selectView(ew);
        return R.ok("查询用户成功").put("data", yonghuView);
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        YonghuEntity yonghu = yonghuService.selectById(id);
        return R.ok().put("data", yonghu);
    }

    /**
     * 前端详情
     */
    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        YonghuEntity yonghu = yonghuService.selectById(id);
        return R.ok().put("data", yonghu);
    }


    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody YonghuEntity yonghu, HttpServletRequest request) {
        yonghu.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        //ValidatorUtils.validateEntity(yonghu);
        YonghuEntity user = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("zhanghao", yonghu.getZhanghao()));
        if (user != null) {
            return R.error("用户已存在");
        }

        yonghu.setId(new Date().getTime());
        if (yonghu.getMima() != null && !PasswordUtil.isEncoded(yonghu.getMima())) {
            yonghu.setMima(PasswordUtil.encode(yonghu.getMima()));
        }
        yonghuService.insert(yonghu);
        return R.ok();
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody YonghuEntity yonghu, HttpServletRequest request) {
        yonghu.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        //ValidatorUtils.validateEntity(yonghu);
        YonghuEntity user = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("zhanghao", yonghu.getZhanghao()));
        if (user != null) {
            return R.error("用户已存在");
        }

        yonghu.setId(new Date().getTime());
        if (yonghu.getMima() != null && !PasswordUtil.isEncoded(yonghu.getMima())) {
            yonghu.setMima(PasswordUtil.encode(yonghu.getMima()));
        }
        yonghuService.insert(yonghu);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody YonghuEntity yonghu, HttpServletRequest request) {
        //ValidatorUtils.validateEntity(yonghu);
        yonghuService.updateById(yonghu);//全部更新
        return R.ok();
    }


    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        yonghuService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 提醒接口
     */
    @RequestMapping("/remind/{columnName}/{type}")
    public R remindCount(@PathVariable("columnName") String columnName, HttpServletRequest request,
                         @PathVariable("type") String type, @RequestParam Map<String, Object> map) {
        map.put("column", columnName);
        map.put("type", type);

        if (type.equals("2")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar c = Calendar.getInstance();
            Date remindStartDate = null;
            Date remindEndDate = null;
            if (map.get("remindstart") != null) {
                Integer remindStart = Integer.parseInt(map.get("remindstart").toString());
                c.setTime(new Date());
                c.add(Calendar.DAY_OF_MONTH, remindStart);
                remindStartDate = c.getTime();
                map.put("remindstart", sdf.format(remindStartDate));
            }
            if (map.get("remindend") != null) {
                Integer remindEnd = Integer.parseInt(map.get("remindend").toString());
                c.setTime(new Date());
                c.add(Calendar.DAY_OF_MONTH, remindEnd);
                remindEndDate = c.getTime();
                map.put("remindend", sdf.format(remindEndDate));
            }
        }

        Wrapper<YonghuEntity> wrapper = new EntityWrapper<YonghuEntity>();
        if (map.get("remindstart") != null) {
            wrapper.ge(columnName, map.get("remindstart"));
        }
        if (map.get("remindend") != null) {
            wrapper.le(columnName, map.get("remindend"));
        }


        int count = yonghuService.selectCount(wrapper);
        return R.ok().put("count", count);
    }


}
