package com.wu.wiki.controller;

import com.wu.wiki.req.DocQueryReq;
import com.wu.wiki.req.DocSaveReq;
import com.wu.wiki.resp.DocQueryResp;
import com.wu.wiki.resp.CommonResp;
import com.wu.wiki.resp.PageResp;
import com.wu.wiki.service.DocService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

//@Controller  返回页面 @RestController 是返回字符串的
@RestController  //@ResponseBody用来返回字符串或JSON对象 大多是JSON对象
@RequestMapping("/doc")
public class DocControl {
    @Resource
    private DocService docService;

    @RequestMapping("/all/{ebookId}")  //接口支持所有的请求方式
    public CommonResp all(@PathVariable Long ebookId){
        CommonResp<List<DocQueryResp>> resp = new CommonResp<>();
        List<DocQueryResp> list= docService.all(ebookId);
        resp.setContent(list);
        return resp;
    }
    @RequestMapping("/list")  //接口支持所有的请求方式
    public CommonResp list(@Valid DocQueryReq req){
        CommonResp<PageResp<DocQueryResp>> resp = new CommonResp<>();
        PageResp<DocQueryResp> list= docService.list(req);
        resp.setContent(list);
        return resp;
    }

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody DocSaveReq req){
        CommonResp resp = new CommonResp();
        docService.save(req);
        return resp;
    }

    @DeleteMapping("/delete/{idsStr}")
    public CommonResp delete(@PathVariable String idsStr){
        CommonResp resp = new CommonResp <>();
        List<String> list = Arrays.asList(idsStr.split(","));
        docService.delete(list);
        return resp;
    }
    @RequestMapping("/find-contend/{id}")  //接口支持所有的请求方式
    public CommonResp findcontend(@PathVariable Long id) {
        CommonResp<String> resp = new CommonResp<>();
        String content = docService.findcontend(id);
        resp.setContent(content);
        return resp;
    }
    @GetMapping("/vote/{id}")
    public CommonResp vote(@PathVariable Long id) {
        CommonResp commonResp = new CommonResp();
        docService.vote(id);
        return commonResp;
    }

}
