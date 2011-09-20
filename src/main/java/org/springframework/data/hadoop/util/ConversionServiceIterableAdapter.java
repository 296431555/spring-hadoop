/*
 * Copyright 2006-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.data.hadoop.util;

import java.util.Iterator;

import org.springframework.core.convert.ConversionService;

/**
 * @author Dave Syer
 * 
 */
public class ConversionServiceIterableAdapter<I, O> implements Iterable<O> {
	private final Iterable<I> values;

	private final ConversionService conversionService;

	private final Class<? extends O> targetType;

	public ConversionServiceIterableAdapter(Iterable<I> values, Class<? extends O> targetType,
			ConversionService conversionService) {
		this.values = values;
		this.targetType = targetType;
		this.conversionService = conversionService;
	}

	public Iterator<O> iterator() {
		return new Iterator<O>() {

			private Iterator<I> wrapped = values.iterator();

			public boolean hasNext() {
				return wrapped.hasNext();
			}

			public O next() {
				I next = wrapped.next();
				return (O) conversionService.convert(next, targetType);
			}

			public void remove() {
				wrapped.remove();
			}
		};
	}

}
