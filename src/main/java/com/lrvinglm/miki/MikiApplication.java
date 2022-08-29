package com.lrvinglm.miki;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;


@SpringBootApplication
public class MikiApplication {
    //LOG作用LoggerFactory.getLogger可以在IDE控制台打印日志,便于开发,一般加在代码最上面
    private static final Logger LOG= LoggerFactory.getLogger(MikiApplication.class);
    public static void main(String[] args) {
        SpringApplication app=new SpringApplication(MikiApplication.class);
        Environment env=app.run(args).getEnvironment();//Environment 这个接口代表应用运行时的环境。它代表了两方面、一个是 profile 一个是 properties。
        LOG.info("启动成功！");
        LOG.info("地址：\thttp://127.0.0.1:{}",env.getProperty("server.port"));
    }

}
