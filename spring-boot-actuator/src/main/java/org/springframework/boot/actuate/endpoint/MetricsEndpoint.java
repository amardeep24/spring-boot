/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.endpoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.endpoint.Endpoint;
import org.springframework.boot.endpoint.ReadOperation;
import org.springframework.boot.endpoint.Selector;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * {@link Endpoint} to expose a collection of {@link PublicMetrics}.
 *
 * @author Dave Syer
 */
@Endpoint(id = "metrics")
public class MetricsEndpoint {

	private final List<PublicMetrics> publicMetrics;

	/**
	 * Create a new {@link MetricsEndpoint} instance.
	 * @param publicMetrics the metrics to expose
	 */
	public MetricsEndpoint(PublicMetrics publicMetrics) {
		this(Collections.singleton(publicMetrics));
	}

	/**
	 * Create a new {@link MetricsEndpoint} instance.
	 * @param publicMetrics the metrics to expose. The collection will be sorted using the
	 * {@link AnnotationAwareOrderComparator}.
	 */
	public MetricsEndpoint(Collection<PublicMetrics> publicMetrics) {
		Assert.notNull(publicMetrics, "PublicMetrics must not be null");
		this.publicMetrics = new ArrayList<>(publicMetrics);
		AnnotationAwareOrderComparator.sort(this.publicMetrics);
	}

	public void registerPublicMetrics(PublicMetrics metrics) {
		this.publicMetrics.add(metrics);
		AnnotationAwareOrderComparator.sort(this.publicMetrics);
	}

	public void unregisterPublicMetrics(PublicMetrics metrics) {
		this.publicMetrics.remove(metrics);
	}

	@ReadOperation
	public Map<String, Object> getMetrics() {
		Map<String, Object> result = new LinkedHashMap<>();
		List<PublicMetrics> metrics = new ArrayList<>(this.publicMetrics);
		for (PublicMetrics publicMetric : metrics) {
			try {
				for (Metric<?> metric : publicMetric.metrics()) {
					result.put(metric.getName(), metric.getValue());
				}
			}
			catch (Exception ex) {
				// Could not evaluate metrics
			}
		}
		return result;
	}

	@ReadOperation
	public Object getSelectedMetrics(@Selector String name) {
		return new NamePatternMapFilter(getMetrics()).getResults(name);
	}

	/**
	 * {@link NamePatternFilter} for the Map source.
	 */
	private class NamePatternMapFilter extends NamePatternFilter<Map<String, ?>> {

		NamePatternMapFilter(Map<String, ?> source) {
			super(source);
		}

		@Override
		protected void getNames(Map<String, ?> source, NameCallback callback) {
			for (String name : source.keySet()) {
				try {
					callback.addName(name);
				}
				catch (NoSuchMetricException ex) {
					// Metric with null value. Continue.
				}
			}
		}

		@Override
		protected Object getOptionalValue(Map<String, ?> source, String name) {
			return source.get(name);
		}

		@Override
		protected Object getValue(Map<String, ?> source, String name) {
			Object value = getOptionalValue(source, name);
			if (value == null) {
				throw new NoSuchMetricException("No such metric: " + name);
			}
			return value;
		}

	}

	/**
	 * Exception thrown when the specified metric cannot be found.
	 */
	@SuppressWarnings("serial")
	@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such metric")
	public static class NoSuchMetricException extends RuntimeException {

		public NoSuchMetricException(String string) {
			super(string);
		}

	}

}
