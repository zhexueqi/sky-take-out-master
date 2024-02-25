package com.sky.controller.admin;


import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * @author zhexueqi
 * @ClassName CommonController
 * @since 2024/2/25    15:38
 */
@RestController
@Slf4j
@RequestMapping("/admin/common")
@Api(tags = "公共接口")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;


    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public Result<String> upload(MultipartFile file) {
        log.info("上传文件:{}",file);

        try {
            //获取文件原始名字
            String originalFilename = file.getOriginalFilename();
            //获取文件后缀
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            //文件的名字
            String fileName = UUID.randomUUID().toString() +  extension;
            //文件的请求路径
            String url = aliOssUtil.upload(file.getBytes(),fileName);
            return Result.success(url);
        } catch (IOException e) {
            log.info("文件上传失败，{}",e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
