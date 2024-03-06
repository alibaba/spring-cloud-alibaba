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

// Created server
var server = http.createServer(function(req, res) {
    // Get request path
    var pathname = url.parse(req.url).pathname;
    res.writeHead(200, { 'Content-Type' : 'application/json; charset=utf-8' });
    // Request http://localhost:8060/,
    // Return {"index":"Welcome to index pages."}
    if (pathname === '/') {
        res.end(JSON.stringify({ "index" : "Welcome to index pages." }));
    }
    // Request http://localhost:8060/health,
    // Return {"status":"UP"}
    else if (pathname === '/health.json') {
        res.end(JSON.stringify({ "status" : "UP" }));
    }
    // Other situations return '404'
    else {
        res.end("404");
    }
});

// Created listener, print logs.
server.listen(8060, function() {

    console.log('node sidecar service listening on localhost:8060 ...');
});
