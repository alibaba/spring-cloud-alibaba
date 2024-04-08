# Spring Cloud Alibaba AI Output

`TongYiController` 接受一个 HTTP GET 请求 `http://localhost:8080/ai/output`。
`controller` 将会调用 `TongYiService` 中的 `genOutputParse` 方法，完成服务请求得到响应。

有一个可选的 `actor` 参数为演员的名字，其默认值为“Jeff Bridges”。 
演员姓名位于硬编码的文本中：

```java
String userMessage = """
        Generate the filmography for the actor {actor}.
        {format}
        """;
```

`format` 变量来自 `OutputParser` 中。

## BeanOutputParser

`BeanOutputParser` 当请求获得响应时，会将响应内容解析为一个 JavaBean 并返回给用户：

```java
var outputParser = new BeanOutputParser<>(ActorsFilms.class);
String format = outputParser.getFormat();
```

模型响应内容会被解析为类 `ActorsFils` 的类属性：

```java
ActorsFilms actorsFilms = outputParser.parse(content);
```

> 返回的数据结构必须符合 `https://json-schema.org/draft/2020-12/schema` 规范，不然会解析失败，这里采用替换的方式先行处理：
> ```java
> // {@link BeanOutputParser#getFormat}
> // simple solve.
> String content = generation.getOutput().getContent()
> .replace("```json", "")
> .replace("```", "");
> ```

## 构建和运行

1. 修改配置文件 `application.yml` 中的 apikey 为有效的 apikey；
2. 通过 IDE 或者 `./mvnw spring-boot:run` 运行应用程序。

## 访问接口

使用 curl 工具对接口发起请求：

```shell
$ curl http://localhost:8080/ai/output
```

响应结果为：

```json
{
    "actor": "Jeff Bridges",
    "movies": [
        "Starman (1984)",
        "The Natural (1984)",
        "The Longest Week (1979)",
        "Against the Wind (1977)",
        "Fat City (1978)",
        "American Heart (1981)",
        "Iron Eagle (1986)",
        "The China Syndrome (1979)",
        "Blazing Saddles (1974)",
        "Winter Kills (1979)",
        "Theetreowners of Broadway (1973)",
        "Trinity (1990)",
        "Tokyo Story (1953) [English dub voice]",
        "The Longest Ride (2015)",
        "Another Day in the Valley (1991)",
        "Stardust Memories (1976)",
        "Jagged Edge (1985)",
        "The Adventures of Ford Fairlane (1996)",
        "The Vanishing (1988)",
        "Dances with Wolves (1990)",
        "The Mirror Has Two Faces (1996)",
        "The Door in the Floor (2004)",
        "The Way Back (2010)",
        "The Secret Life of Walter Mitty (2013)",
        "The Big Lebowski (1998)",
        "The Iron Giant (1999) [voice]",
        "The Man Who Wasn't There (2001)",
        "True Grit (2010)",
        "Crazy Heart (2009)",
        "Iron Man 2 (2010) [voice]",
        "TRON: Legacy (2010)",
        "The Giver (2014)",
        "Hell or High Water (2016)"
    ]
}
```

现在使用 actor 参数：

```shell
$ curl --get --data-urlencode 'actor=Bill Murray' http://localhost:8080/ai/output

```

Response：

```json
{
    "actor": "Bill Murray",
    "movies": [
        "The<[email protected]>",
        "Caddyshack",
        "Ghostbusters",
        "Stripes",
        "Scrooged",
        "Groundhog Day",
        "What About Bob?",
        " Coneheads ",
        "Stuart Saves His Family",
        " Rushmore",
        "The Royal Tenenbaums",
        "Lost in Translation",
        "Garfield: The Movie",
        "Osmosis Jones",
        "The Life Aquatic with Steve Zissou",
        "Hot Fuzz",
        "Get Smart",
        "The Grand Budapest Hotel",
        "Trainwreck",
        "Chef",
        "Inside Out",
        "Zootopia",
        "On the Rocks",
        "Spies in Disguise"
    ]
}
```
