package org.springframework.cloud.alibaba.cloud.examples;

import java.nio.charset.Charset;

import org.apache.commons.codec.CharEncoding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.IOUtils;
import com.aliyun.oss.model.OSSObject;

/**
 * OSS Controller
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@RestController
public class OSSController {

	@Autowired
	private OSS ossClient;

	@Value("oss://" + OSSApplication.BUCKET_NAME + "/oss-test")
	private Resource file;

	private String dir = "custom-dir/";

	@GetMapping("/upload")
	public String upload() {
		try {
			ossClient.putObject(OSSApplication.BUCKET_NAME, dir + "oss-test", this
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
					file.getInputStream(), Charset.forName(CharEncoding.UTF_8));
		}
		catch (Exception e) {
			e.printStackTrace();
			return "get resource fail: " + e.getMessage();
		}
	}

	@GetMapping("/download")
	public String download() {
		try {
			OSSObject ossObject = ossClient.getObject(OSSApplication.BUCKET_NAME,
					dir + "oss-test");
			return "download success, content: " + IOUtils
					.readStreamAsString(ossObject.getObjectContent(), CharEncoding.UTF_8);
		}
		catch (Exception e) {
			e.printStackTrace();
			return "download fail: " + e.getMessage();
		}
	}

}
