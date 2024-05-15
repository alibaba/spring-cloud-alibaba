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

package com.alibaba.cloud.ai.tongyi.audio.transcription.api;

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

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		else if (!(o instanceof TranscriptionResult)) {
			return false;
		}
		else {
			TranscriptionResult other = (TranscriptionResult) o;
			if (!other.equals(this)) {
				return false;
			}
			else {
				label95: {
					Object this$requestId = this.getRequestId();
					Object other$requestId = other.getRequestId();
					if (this$requestId == null) {
						if (other$requestId == null) {
							break label95;
						}
					}
					else if (this$requestId.equals(other$requestId)) {
						break label95;
					}

					return false;
				}

				Object this$output = this.getOutput();
				Object other$output = other.getOutput();
				if (this$output == null) {
					if (other$output != null) {
						return false;
					}
				}
				else if (!this$output.equals(other$output)) {
					return false;
				}

				Object this$usage = this.getUsage();
				Object other$usage = other.getUsage();
				if (this$usage == null) {
					if (other$usage != null) {
						return false;
					}
				}
				else if (!this$usage.equals(other$usage)) {
					return false;
				}

				label74: {
					Object this$taskStatus = this.getTaskStatus();
					Object other$taskStatus = other.getTaskStatus();
					if (this$taskStatus == null) {
						if (other$taskStatus == null) {
							break label74;
						}
					}
					else if (this$taskStatus.equals(other$taskStatus)) {
						break label74;
					}

					return false;
				}

				label67: {
					Object this$setTaskId = this.getsetTaskId();
					Object other$setTaskId = other.getRequestId();
					if (this$setTaskId == null) {
						if (other$setTaskId == null) {
							break label67;
						}
					}
					else if (this$setTaskId.equals(other$setTaskId)) {
						break label67;
					}

					return false;
				}

				Object this$results = this.getResults();
				Object other$results = other.getResults();
				if (this$results == null) {
					if (other$results != null) {
						return false;
					}
				}
				else if (!this$results.equals(other$results)) {
					return false;
				}

				Object this$metrics = this.getMetrics();
				Object other$metrics = other.getMetrics();
				if (this$metrics == null) {
					if (other$metrics != null) {
						return false;
					}
				}
				else if (!this$metrics.equals(other$metrics)) {
					return false;
				}

				return true;
			}
		}
	}

	protected boolean canEqual(Object other) {
		return other instanceof TranscriptionResult;
	}

	@Override
	public int hashCode() {
		boolean PRIME = true;
		int result = 1;
		Object $requestId = this.getRequestId();
		result = result * 59 + ($requestId == null ? 43 : $requestId.hashCode());
		Object $output = this.getOutput();
		result = result * 59 + ($output == null ? 43 : $output.hashCode());
		Object $usage = this.getUsage();
		result = result * 59 + ($usage == null ? 43 : $usage.hashCode());
		Object $taskStatus = this.getTaskStatus();
		result = result * 59 + ($taskStatus == null ? 43 : $taskStatus.hashCode());
		Object $setTaskId = this.getsetTaskId();
		result = result * 59 + ($setTaskId == null ? 43 : $setTaskId.hashCode());
		Object $results = this.getResults();
		result = result * 59 + ($results == null ? 43 : $results.hashCode());
		Object $metrics = this.getMetrics();
		result = result * 59 + ($metrics == null ? 43 : $metrics.hashCode());
		return result;
	}
}
