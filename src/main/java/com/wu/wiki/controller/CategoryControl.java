package com.wu.wiki.controller;

import com.wu.wiki.req.CategoryQueryReq;
import com.wu.wiki.req.CategorySaveReq;
import com.wu.wiki.resp.CommonResp;
import com.wu.wiki.resp.CategoryQueryResp;
import com.wu.wiki.resp.PageResp;
import com.wu.wiki.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

//@Controller  返回页面 @RestController 是返回字符串的
@RestController  //@ResponseBody用来返回字符串或JSON对象 大多是JSON对象
@RequestMapping("/category")
public class CategoryControl {
    @Resource
    private CategoryService categoryService;

    @RequestMapping("/all")  //接口支持所有的请求方式
    public CommonResp all(){
        CommonResp<List<CategoryQueryResp>> resp = new CommonResp<>();
        List<CategoryQueryResp> list= categoryService.all();
        resp.setContent(list);
        return resp;
    }
    @RequestMapping("/list")  //接口支持所有的请求方式
    public CommonResp list(@Valid CategoryQueryReq req){
        CommonResp<PageResp<CategoryQueryResp>> resp = new CommonResp<>();
        PageResp<CategoryQueryResp> list= categoryService.list(req);
        resp.setContent(list);
        return resp;
    }

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody CategorySaveReq req){
        CommonResp resp = new CommonResp();
        categoryService.save(req);
        return resp;
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp delete(@PathVariable long id){
        CommonResp resp = new CommonResp();
        categoryService.delete(id);
        return resp;
    }
}
