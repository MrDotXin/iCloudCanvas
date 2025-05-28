## 后端

[toc]

---

## 基于tencet OSS 实现图片上传和下载



通过配置`CosClient`我们可以封装OSS服务

```java
@Configuration
@EnableConfigurationProperties(CosConfig.class)
public class CosClientConfig {

    @Resource
    private CosConfig cosConfig;

    @Bean
    public COSClient cosClient() {
        COSCredentials cosClient = new BasicCOSCredentials(cosConfig.getSecretID(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));

        return new COSClient(cosClient, clientConfig);
    }
}
```

```java
public interface OSService {

    String upload(String fileKey, File file);

    UploadFileResult uploadWithInfo(String fileKey, File file);

    byte[] getObjectContentByKey(String filePath);

    void removeObjectByUrl(String url);
}
```



## 一阶段: 用户模块与基础图片模块

主要实现用户管理还有图片增删改查



## 阶段二 用户上传与审核功能



**用户上传图片*** ***管理员审核(AI审核)**

首先添加字段

```sql
ALTER TABLE picture  
    -- 添加新列  
    ADD COLUMN reviewStatus INT DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',  
    ADD COLUMN reviewMessage VARCHAR(512) NULL COMMENT '审核信息',  
    ADD COLUMN reviewerId BIGINT NULL COMMENT '审核人 ID',  
    ADD COLUMN reviewTime DATETIME NULL COMMENT '审核时间';  
  
-- 创建基于 reviewStatus 列的索引  
CREATE INDEX idx_reviewStatus ON picture (reviewStatus);

```



用来表示图片上传后进行的审核操作

我们可以对每一个传入的修改和上传请求都做审核处理

```java
   @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum statusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        if (Objects.isNull(id) || Objects.isNull(statusEnum) || PictureReviewStatusEnum.REVIEWING.equals(statusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Picture picture = this.getById(id);
        if (Objects.isNull(picture)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        if (picture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请勿重复审核!");
        }

        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        ThrowUtils.throwIf(!this.updateById(updatePicture), ErrorCode.SYSTEM_ERROR);
    }
```

### 基于url的上传

首先我们任何文件传进来都会有下载到本地 + 上传到服务器这一步, 也就是说，我们可以单独抽象下载到本地这一个步骤，并且接收`url`参数、`MultipartFile`参数, 最终都会返回



#### 验证`URL`上传的内容

基于`Hutool.FileUtil`以及`Hutool.HttpUtil` 我们可以很容易实现对url目标对象的校验和审查

我们需要验证:

- url的有效性
- 协议的正确性(http, https)
- 文件是否存在
- 文件类型校验
- 文件大小校验
- 拓展: `AI`审查具体内容

```java
@Override
    public void validPicture(Object inputSource) {
        ThrowUtils.throwIf(Objects.isNull(inputSource), ErrorCode.PARAMS_ERROR, "文件不能为空");
        String url = (String) inputSource;

        ThrowUtils.throwIf(!(url.startsWith("http://") || url.startsWith("https://")), ErrorCode.PARAMS_ERROR);

        try { new URL(url); } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, e.getMessage());
        }

        try ( HttpResponse httpResponse = HttpUtil.createRequest(Method.HEAD, url).execute()) {

            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                throw  new BusinessException(ErrorCode.PARAMS_ERROR, "无法获取图片信息");
            }

            String contentType = httpResponse.header("Content-Type");

            if (StrUtil.isNotBlank(contentType)) {
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType), ErrorCode.FORBIDDEN_ERROR, "禁止的文件类型!");

            }
            String contentLength = httpResponse.header("Content-Length");

            if (StrUtil.isNotBlank(contentLength)) {
                long length = Long.parseLong(contentLength);
                final long ONE_M = 1024 * 1024;
                final long limit = maxImageUploadSize * ONE_M;
                ThrowUtils.throwIf(limit < length, ErrorCode.FORBIDDEN_ERROR, "上传文件太大! 不得超过 " + limit + "MB");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw  new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
```

使用**模板方法** + **策略模式**, 我们可以轻松实现多种上传方式的引入.

### 基于搜索关键字的抓取

通过`jsoup`库，我们可以通过解析外部`html`来快速获取批量获取图片

