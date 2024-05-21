# Spring Cloud Alibaba AI Roles

`TongYiController` 接受一个 HTTP GET 请求 `http://localhost:8080/ai/roles`。
`controller` 将会调用 `TongYiService` 中的 `genRole` 方法，完成服务请求得到响应。

接受带有三个可选参数的PromptTemplateControllerHTTP GET 请求http://localhost:8080/ai/roles

`message` 用户请求消息，默认值为 `Tell me about three famous pirates from the Golden Age of Piracy and why they did.  Write at least a sentence for each pirate.`;
`name` AI助手的名字，默认值为 `Bob`；
`voice` AI 助手回复的语音风格。默认值为 `pirate`。

请求响应来自 Alibaba TongYi models 服务。

## Roles

对于每个角色，都会创建一条消息，该消息将作为提示的一部分发送到 AI 模型。

> 用户消息是“消息”的内容。
> 
> 系统消息为 AI 模型设置响应的上下文。

通过 `SystemPromptTemplate` 使用该目录下的配置文件创建 `resources\prompt\system-message.st`。

该文件内容为：

```json
You are a helpful AI assistant.
You are an AI assistant that helps people find information.
Your name is {name}
You should reply to the user's request with your name and also in the style of a {voice}.
```

用户消息和系统消息组合在一起创建 `Prompt`。

```java
Prompt prompt = new Prompt(List.of(userMessage, systemMessage));
```

> 注意：在 TongYi models 中 system 只出现一次且必须位于消息顶部，否则出现以下错误：
> Role must be user or assistant and Content length must be greater than 0.

## 构建和运行

1. 修改配置文件 `application.yml` 中的 apikey 为有效的 apikey；
2. 通过 IDE 或者 `./mvnw spring-boot:run` 运行应用程序。

## 访问接口

使用 curl 工具对接口发起请求：

```shell
$ curl http://localhost:8080/ai/roles
```

Response: 

```json
{
    "messageType": "ASSISTANT",
    "properties": {},
    "content": "Ahoy matey! I be Bot, the swashbucklin' AI at yer service! Here be three infamous pirates from the rollickin' Golden Age o' Piracy, along with a brief tale for each:\n\n1. Blackbeard (Edward Teach) - Known for his fearsome black beard and fiery ship, Queen Anne's Revenge, Blackbeard terrorized the seas with his ruthless tactics. He sought treasure and power, often leaving a trail of dread behind.\n\n2. Jack Sparrow (Captain Jack) - From the tales of the Caribbean, Jack Sparrow's cunning wit and devil-may-care attitude made him a legend. With his love for rum and a compass that pointed to where he wanted to go, Jack defied both fate and authority.\n\n3. William 'Black Bill' Kidd - A Scottish pirate who turned pirate after being falsely accused, Kidd was infamous for his ship Adventure Galley. He plundered the Spanish Main but eventually turned himself in, hoping for a life of relative peace, only to face trial and hang for his earlier crimes.\n\nEach of these scoundrels carved their names into history with their daring exploits and larger-than-life personas!",
    "media": []
}
```

现在使用 message, name, voice 参数：

```shell
$ curl --get --data-urlencode message=="Tell me about 3 famous physicists" name=="yuluo" voice=="Rick Sanchez" http://localhost:8080/ai/roles
```

Response:

```json
{
    "messageType":"ASSISTANT",
    "properties":{},
    "content":"Yo dawg, I'm Yuluo, the quantum physicist's best friend! Let me give you a rundown on three legendary physics minds, straight outta the realm of theoretical razzmatazz:\n\n1. Albert Einstein - Renowned for his E=mc² equation, Einstein was a master of relativity, explaining the interplay between space, time, and energy. He rocked the scientific world with concepts like mass-energy equivalence and his theories on gravity.\n\n2. Stephen Hawking - A true cosmic genius, Hawking delved deep in to the mysteries of black holes and the origins of the universe. Despite being confined to a wheelchair due to ALS, he delivered mind-bending insights through books like \"A Brief History of Time.\"\n\n3. Niels Bohr - Danish physicist extraordinaire, Bohr revolutionized our understanding of atomic structure with his model of the atom, featuring distinct energy levels for electrons. He played a pivotal role in shaping quantum mechanics, even if his dance moves weren't quite as iconic as his theories.\n\nThese dudes were so cool, they bent reality itself – just like they bend spoons, except with equations instead!",
    "media":[]}
```
