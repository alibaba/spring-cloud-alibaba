/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appactive.demo.common.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResultHolder <T>  implements Serializable {

    private T result;

    List<Node> chain = new ArrayList<>();

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public List<Node> getChain() {
        return chain;
    }

    public void setChain(List<Node> chain) {
        this.chain = chain;
    }

    public void addChain(String app, String unitFlag){
        chain.add(new Node(app, unitFlag));
    }

    /**
     * 默认构造器，便于序列化
     */
    public ResultHolder() {

    }

    static class Node implements Serializable{
        private String app;
        private String unitFlag;

        public Node() {
        }

        public Node(String app, String unitFlag) {
            this.app = app;
            this.unitFlag = unitFlag;
        }

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public String getUnitFlag() {
            return unitFlag;
        }

        public void setUnitFlag(String unitFlag) {
            this.unitFlag = unitFlag;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "app='" + app + '\'' +
                    ", unitFlag='" + unitFlag + '\'' +
                    '}';
        }
    }

    public ResultHolder(T result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ResultHolder{" +
                "result=" + result +
                ", chain=" + chain +
                '}';
    }
}
