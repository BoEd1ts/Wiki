package com.wu.wiki.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wu.wiki.aspect.LogAspect;
import com.wu.wiki.domain.User;
import com.wu.wiki.domain.UserExample;
import com.wu.wiki.exception.BusinessException;
import com.wu.wiki.exception.BusinessExceptionCode;
import com.wu.wiki.mapper.UserMapper;
import com.wu.wiki.req.UserLoginReq;
import com.wu.wiki.req.UserQueryReq;
import com.wu.wiki.req.UserResetPasswordReq;
import com.wu.wiki.req.UserSaveReq;
import com.wu.wiki.resp.PageResp;
import com.wu.wiki.resp.UserLoginResp;
import com.wu.wiki.resp.UserQueryResp;
import com.wu.wiki.utils.CopyUtil;
import com.wu.wiki.utils.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;


@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private SnowFlake snowFlake;
    private final static Logger LOG = LoggerFactory.getLogger(LogAspect.class);

    public PageResp<UserQueryResp> list(UserQueryReq req){

        //domain下的example mybaits自动生成了很多方法
        UserExample userExample = new UserExample();
        //当作where语句
        UserExample.Criteria criteria = userExample.createCriteria();
        if(!ObjectUtils.isEmpty(req.getLoginName())){
            criteria.andLoginNameEqualTo(req.getLoginName());
        }
        PageHelper.startPage(req.getPage(),req.getSize());//只会分页最近的需要查询的sql，当页面多条sql时 把分页和sql放一起
        List<User> userList = userMapper.selectByExample(userExample);//查询到所有的User实体

        PageInfo<User> pageInfo=new PageInfo<>(userList);
        LOG.info("页数:{}",pageInfo.getPages());
        LOG.info("行数：{}",pageInfo.getTotal());
//        List<UserResp> respList=new ArrayList<>();
        //遍历所有的User属性给UserResp 并过滤掉不需要返回的属性
//        for (User e:userList) {
//            UserResp userResp=new UserResp();
//            BeanUtils.copyProperties(e,userResp);
//            UserResp userResp = CopyUtil.copy(e, UserResp.class);
//            respList.add(userResp);
//        }
        List<UserQueryResp> respList = CopyUtil.copyList(userList, UserQueryResp.class);
        PageResp<UserQueryResp> pageResp=new PageResp();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(respList);
        return pageResp;
    }

    /**
     * 保存
     */
    public void save(UserSaveReq req){
        User user=CopyUtil.copy(req,User.class);
        if(ObjectUtils.isEmpty(user.getId())){
            User userDB = selectByLoginName(req.getLoginName());
            if (ObjectUtils.isEmpty(userDB)){
                //新增
                user.setId(snowFlake.nextId());
                userMapper.insert(user);
            }else {
                //用户名已存在
                throw new BusinessException(BusinessExceptionCode.USER_LOGIN_NAME_EXIST);
            }
        }else{
            //更新
            user.setLoginName(null);
            user.setPassword(null);
            userMapper.updateByPrimaryKeySelective(user);
        }
    }

    /**
     * 删除
     */
    public void delete(Long id){
        //删除指定id的数据
        userMapper.deleteByPrimaryKey(id);//deleteByPrimaryKey根据主键来删除。
    }
    public User selectByLoginName(String LoginName){
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andLoginNameEqualTo(LoginName);
        List<User> userList = userMapper.selectByExample(userExample);
        if (CollectionUtils.isEmpty(userList)){
            return null;
        }else {
            return userList.get(0);

        }
    }

    /**
     * 修改密码
     */
    public void resetPassword(UserResetPasswordReq req){
        User user=CopyUtil.copy(req,User.class);
        userMapper.updateByPrimaryKeySelective(user);
    }

    /**
     * 登入
     */
    public UserLoginResp login(UserLoginReq req) {
        User userDb = selectByLoginName(req.getLoginName());
        if (ObjectUtils.isEmpty(userDb)) {
            LOG.info("用户名不存在，{}", req.getLoginName());
            throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
        } else {
            if (userDb.getPassword().equals(req.getPassword())) {
                //登入成功
                UserLoginResp userLoginResp = CopyUtil.copy(userDb, UserLoginResp.class);
                return userLoginResp;
            } else {
                //密码不对
                LOG.info("密码不对，输入密码：{}，数据库密码：{}", req.getPassword(), userDb.getPassword());
                throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
            }
        }

    }


}
