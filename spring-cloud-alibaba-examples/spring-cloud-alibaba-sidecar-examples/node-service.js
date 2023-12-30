/*
 * Copyright 2013-2023 the original author or authors.
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

var http = require('http');
var url = require("url");
var path = require('path');

// 创建server
var server = http.createServer(function(req, res) {
    // 获得请求的路径
    var pathname = url.parse(req.url).pathname;
    res.writeHead(200, { 'Content-Type' : 'application/json; charset=utf-8' });
    // 访问http://localhost:8060/，将会返回{"index":"欢迎来到首页"}
    if (pathname === '/') {
        res.end(JSON.stringify({ "index" : "欢迎来到首页" }));
    }
    // 访问http://localhost:8060/health，将会返回{"status":"UP"}
    else if (pathname === '/health.json') {
        res.end(JSON.stringify({ "status" : "UP" }));
    }
    // 其他情况返回404
    else {
        res.end("404");
    }
});
// 创建监听，并打印日志
server.listen(8060, function() {
    console.log('listening on localhost:8060');
});