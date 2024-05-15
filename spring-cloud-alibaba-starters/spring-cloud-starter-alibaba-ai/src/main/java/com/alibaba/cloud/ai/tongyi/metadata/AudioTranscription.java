/*
 * Copyright 2023-2024 the original author or authors.
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

package com.alibaba.cloud.ai.tongyi.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alibaba.dashscope.audio.asr.transcription.TranscriptionMetrics;
import com.alibaba.dashscope.audio.asr.transcription.TranscriptionResult;
import com.alibaba.dashscope.audio.asr.transcription.TranscriptionTaskResult;
import com.alibaba.dashscope.common.DashScopeResult;
import com.alibaba.dashscope.common.TaskStatus;
import com.alibaba.dashscope.exception.ApiException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import org.springframework.ai.model.ModelResult;
import org.springframework.ai.model.ResultMetadata;

/**
 * @author: xYLiu
 * @date: 2024/5/11 23:34
 */

public class AudioTranscription implements ModelResult<Object> {
	@SerializedName("request_id")
	private String requestId;
	private JsonObject output;
	private JsonObject usage;
	private TaskStatus taskStatus;
	private String setTaskId;
	private List<TranscriptionTaskResult> results = new ArrayList();
	private TranscriptionMetrics metrics;

	public static TranscriptionResult fromDashScopeResult(DashScopeResult dashScopeResult)
			throws ApiException {
		TranscriptionResult result = new TranscriptionResult();
		result.setOutput((JsonObject) dashScopeResult.getOutput());
		if (dashScopeResult.getUsage() != null) {
			result.setUsage(dashScopeResult.getUsage().getAsJsonObject());
		}

		result.setRequestId(dashScopeResult.getRequestId());
		if (dashScopeResult.getOutput() != null) {
			JsonElement jsonMetrics;
			if (result.getOutput().has("task_status")) {
				jsonMetrics = result.getOutput().get("task_status");
				if (jsonMetrics != null) {
					result.setTaskStatus(TaskStatus.valueOf(jsonMetrics.getAsString()));
				}
				else {
					result.setTaskStatus(TaskStatus.FAILED);
				}
			}

			if (result.getOutput().has("task_id")) {
				result.setTaskId(result.getOutput().get("task_id").getAsString());
			}
			else {
				result.setTaskId(null);
			}

			if (result.getOutput().has("results")) {
				jsonMetrics = result.getOutput().get("results");
				if (jsonMetrics != null) {
					if (result.getResults() == null) {
						result.setResults(new ArrayList());
					}

					JsonArray array = jsonMetrics.getAsJsonArray();
					Iterator var4 = array.iterator();

					while (var4.hasNext()) {
						JsonElement object = (JsonElement) var4.next();
						TranscriptionTaskResult taskResult = TranscriptionTaskResult
								.from(object.getAsJsonObject());
						result.getResults().add(taskResult);
					}
				}
				else {
					result.setResults(new ArrayList());
				}
			}

			if (result.getOutput().has("task_metrics")) {
				jsonMetrics = result.getOutput().get("task_metrics");
				if (jsonMetrics != null) {
					result.setMetrics(
							TranscriptionMetrics.from(jsonMetrics.getAsJsonObject()));
				}
				else {
					result.setMetrics(new TranscriptionMetrics());
				}
			}
		}

		return result;
	}

	public void TranscriptionResult() {
	}

	public String getRequestId() {
		return this.requestId;
	}

	@Override
	public JsonObject getOutput() {
		return this.output;
	}

	@Override
	public ResultMetadata getMetadata() {
		return null;
	}

	public JsonObject getUsage() {
		return this.usage;
	}

	public TaskStatus getTaskStatus() {
		return this.taskStatus;
	}

	public String getsetTaskId() {
		return this.setTaskId;
	}

	public List<TranscriptionTaskResult> getResults() {
		return this.results;
	}

	public TranscriptionMetrics getMetrics() {
		return this.metrics;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public void setOutput(JsonObject output) {
		this.output = output;
	}

	public void setUsage(JsonObject usage) {
		this.usage = usage;
	}

	public void setTaskStatus(TaskStatus taskStatus) {
		this.taskStatus = taskStatus;
	}

	public void setsetTaskId(String setTaskId) {
		this.setTaskId = setTaskId;
	}

	public void setResults(List<TranscriptionTaskResult> results) {
		this.results = results;
	}

	public void setMetrics(TranscriptionMetrics metrics) {
		this.metrics = metrics;
	}

	@Override
	public String toString() {
		return "TranscriptionResult(requestId=" + this.getRequestId() + ", output="
				+ this.getOutput() + ", usage=" + this.getUsage() + ", taskStatus="
				+ this.getTaskStatus() + ", setTaskId=" + this.getsetTaskId()
				+ ", results=" + this.getResults() + ", metrics=" + this.getMetrics()
				+ ")";
	}
}
