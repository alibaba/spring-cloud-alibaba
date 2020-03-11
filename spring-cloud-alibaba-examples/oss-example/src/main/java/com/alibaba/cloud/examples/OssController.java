/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.examples;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.IOUtils;
import com.aliyun.oss.model.OSSObject;
import org.apache.commons.codec.CharEncoding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OSS Controller.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@RestController
public class OssController {

	@Autowired
	private OSS ossClient;

	@Value("classpath:/oss-test.json")
	private Resource localFile;

	@Value("oss://" + OssApplication.BUCKET_NAME + "/oss-test.json")
	private Resource remoteFile;

	@GetMapping("/upload")
	public String upload() {
		try {
			ossClient.putObject(OssApplication.BUCKET_NAME, "oss-test.json", this
					.getClass().getClassLoader().getResourceAsStream("oss-test.json"));
		}
		catch (Exception e) {
			e.printStackTrace();
			return "upload fail: " + e.getMessage();
		}
		return "upload success";
	}

	@GetMapping("/file-resource")
	public String fileResource() {
		try {
			return "get file resource success. content: " + StreamUtils.copyToString(
					remoteFile.getInputStream(), Charset.forName(CharEncoding.UTF_8));
		}
		catch (Exception e) {
			e.printStackTrace();
			return "get resource fail: " + e.getMessage();
		}
	}

	@GetMapping("/download")
	public String download() {
		try {
			OSSObject ossObject = ossClient.getObject(OssApplication.BUCKET_NAME,
					"oss-test.json");
			return "download success, content: " + IOUtils
					.readStreamAsString(ossObject.getObjectContent(), CharEncoding.UTF_8);
		}
		catch (Exception e) {
			e.printStackTrace();
			return "download fail: " + e.getMessage();
		}
	}

	@GetMapping("/upload2")
	public String uploadWithOutputStream() {
		try {
			try (OutputStream outputStream = ((WritableResource) this.remoteFile)
					.getOutputStream();
					InputStream inputStream = localFile.getInputStream()) {
				StreamUtils.copy(inputStream, outputStream);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return "upload with outputStream failed";
		}
		return "upload success";
	}

}
