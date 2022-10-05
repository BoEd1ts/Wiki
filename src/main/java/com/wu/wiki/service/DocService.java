package com.wu.wiki.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wu.wiki.aspect.LogAspect;
import com.wu.wiki.controller.BusinessException;
import com.wu.wiki.controller.BusinessExceptionCode;
import com.wu.wiki.domain.Content;
import com.wu.wiki.domain.Doc;
import com.wu.wiki.domain.DocExample;
import com.wu.wiki.mapper.ContentMapper;
import com.wu.wiki.mapper.DocMapper;
import com.wu.wiki.mapper.DocMapperCust;
import com.wu.wiki.req.DocQueryReq;
import com.wu.wiki.req.DocSaveReq;
import com.wu.wiki.resp.DocQueryResp;
import com.wu.wiki.resp.PageResp;
import com.wu.wiki.utils.CopyUtil;
import com.wu.wiki.utils.RedisUtil;
import com.wu.wiki.utils.RequestContext;
import com.wu.wiki.utils.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
@RestController

@Service
public class DocService {
    @Resource
    private DocMapper docMapper;
    @Resource
    private ContentMapper contentMapper;
    @Resource
    private DocMapperCust docMapperCust;
    @Resource
    private SnowFlake snowFlake;
    @Resource
    private RedisUtil redisUtil;

    @Resource
    private WsService wsService;
    private final static Logger LOG = LoggerFactory.getLogger(LogAspect.class);

    public List<DocQueryResp> all(Long ebookId){
        DocExample docExample = new DocExample();
        docExample.createCriteria().andEbookIdEqualTo(ebookId);
        docExample.setOrderByClause("sort asc");
        List<Doc> docList = docMapper.selectByExample(docExample);


        List<DocQueryResp> list = CopyUtil.copyList(docList, DocQueryResp.class);

        return list;
    }
    public PageResp<DocQueryResp> list(DocQueryReq req){



        //domain下的example mybaits自动生成了很多方法
        DocExample docExample = new DocExample();
        docExample.setOrderByClause("sort asc");
        //当作where语句
        DocExample.Criteria criteria = docExample.createCriteria();
//        if(!ObjectUtils.isEmpty(req.getName())){//不为空才执行
//            criteria.andNameLike("%"+req.getName()+"%"); //模糊查询的条件
//        }
        PageHelper.startPage(req.getPage(),req.getSize());//只会分页最近的需要查询的sql，当页面多条sql时 把分页和sql放一起
        List<Doc> docList = docMapper.selectByExample(docExample);//查询到所有的Doc实体

        PageInfo<Doc> pageInfo=new PageInfo<>(docList);
        LOG.info("页数:{}",pageInfo.getPages());
        LOG.info("行数：{}",pageInfo.getTotal());
//        List<DocResp> respList=new ArrayList<>();
        //遍历所有的Doc属性给DocResp 并过滤掉不需要返回的属性
//        for (Doc e:docList) {
//            DocResp docResp=new DocResp();
//            BeanUtils.copyProperties(e,docResp);
//            DocResp docResp = CopyUtil.copy(e, DocResp.class);
//            respList.add(docResp);
//        }
        List<DocQueryResp> respList = CopyUtil.copyList(docList, DocQueryResp.class);
        PageResp<DocQueryResp> pageResp=new PageResp();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(respList);
        return pageResp;
    }

    /**
     * 保存
     */
    public void save(DocSaveReq req){
        Doc doc=CopyUtil.copy(req,Doc.class);
        Content content=CopyUtil.copy(req, Content.class);
        if(ObjectUtils.isEmpty(doc.getId())){
            //新增
            doc.setId(snowFlake.nextId());
            doc.setViewCount(0);
            doc.setVoteCount(0);
            docMapper.insert(doc);

            content.setId(doc.getId());
            contentMapper.insert(content);
        }else{
            //更新
            docMapper.updateByPrimaryKey(doc); //updateByPrimaryKey没有大字段的操作
            int count = contentMapper.updateByPrimaryKeyWithBLOBs(content);//updateByPrimaryKeyWithBLOBs包含全部字段包含大字段的操作
            if (count==0){
                contentMapper.insert(content);
            }
        }
    }

    /**
     * 删除
     */
    public void delete(Long id){
        //删除指定id的数据
        docMapper.deleteByPrimaryKey(id);
    }
    public void delete(List<String> ids){
        DocExample docExample = new DocExample();
        DocExample.Criteria criteria = docExample.createCriteria();
        criteria.andIdIn(ids);
        docMapper.deleteByExample(docExample);

    }
    public String findcontend(Long id){
        Content content = contentMapper.selectByPrimaryKey(id);
        //文档阅读数加1
        docMapperCust.increaseViewCount(id);
        if (ObjectUtils.isEmpty(content)){
            return "";
        }else {
            return content.getContent();
        }

    }

    /**
     * 点赞
     */
    public void vote(Long id) {
        // docMapperCust.increaseVoteCount(id);
        // 远程IP+doc.id作为key，24小时内不能重复
        String ip = RequestContext.getRemoteAddr();
        if (redisUtil.validateRepeat("DOC_VOTE_" + id + "_" + ip, 3600*24)) {
            docMapperCust.increaseVoteCount(id);
        } else {
            throw new BusinessException(BusinessExceptionCode.VOTE_REPEAT);
        }
        //推送消息
        Doc docDb = docMapper.selectByPrimaryKey(id);
        wsService.sendInfo("【"+docDb.getName()+"】被点赞！");
    }

    public void updateEbookInfo(){
        docMapperCust.updateEbookInfo();
    }

}
