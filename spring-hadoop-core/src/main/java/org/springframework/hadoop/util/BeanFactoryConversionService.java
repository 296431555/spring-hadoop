/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.hadoop.util;

import java.beans.PropertyEditor;
import java.util.Collection;

import org.springframework.beans.BeansException;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.ConversionServiceFactory;
import org.springframework.util.CollectionUtils;

/**
 * @author Dave Syer
 * @author Oleg Zhurakousky
 */
public class BeanFactoryConversionService implements ConversionService, ConverterRegistry, BeanFactoryAware {

	private static final TypeDescriptor STRING_TYPE = TypeDescriptor.valueOf(String.class);

	private static ConversionService defaultConversionService;

	private volatile SimpleTypeConverter delegate = new SimpleTypeConverter();

	private volatile ConversionService conversionService;

	public BeanFactoryConversionService() {
		synchronized (BeanFactoryConversionService.class) {
			if (defaultConversionService == null) {
				defaultConversionService = ConversionServiceFactory.createDefaultConversionService();
			}
		}
		this.conversionService = defaultConversionService;
	}

	public BeanFactoryConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (beanFactory instanceof ConfigurableBeanFactory) {
			Object typeConverter = ((ConfigurableBeanFactory) beanFactory).getTypeConverter();
			if (typeConverter instanceof SimpleTypeConverter) {
				delegate = (SimpleTypeConverter) typeConverter;
			}
		}
	}

	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		if (conversionService.canConvert(sourceType, targetType)) {
			return true;
		}
		if (!String.class.isAssignableFrom(sourceType) && !String.class.isAssignableFrom(targetType)) {
			// PropertyEditor cannot convert non-Strings
			return false;
		}
		if (!String.class.isAssignableFrom(sourceType)) {
			return delegate.findCustomEditor(sourceType, null) != null || delegate.getDefaultEditor(sourceType) != null;
		}
		return delegate.findCustomEditor(targetType, null) != null || delegate.getDefaultEditor(targetType) != null;
	}

	public boolean canConvert(TypeDescriptor sourceTypeDescriptor, TypeDescriptor targetTypeDescriptor) {
		if (conversionService.canConvert(sourceTypeDescriptor, targetTypeDescriptor)) {
			return true;
		}
		// TODO: what does this mean? This method is not used in SpEL so
		// probably ignorable?
		Class<?> sourceType = sourceTypeDescriptor.getObjectType();
		Class<?> targetType = targetTypeDescriptor.getObjectType();
		return canConvert(sourceType, targetType);
	}

	public Object convert(Object value, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (targetType.getType() == Void.class || targetType.getType() == Void.TYPE) {
			return null;
		}
		if (value instanceof Collection<?> && CollectionUtils.isEmpty((Collection<?>) value)
				&& Collection.class.isAssignableFrom(targetType.getObjectType())) {
			return value;
		}
		if (conversionService.canConvert(sourceType, targetType)) {
			return conversionService.convert(value, sourceType, targetType);
		}

		if (!String.class.isAssignableFrom(sourceType.getType())) {
			PropertyEditor editor = delegate.findCustomEditor(sourceType.getType(), null);
			if (editor != null) { // INT-1441
				editor.setValue(value);
				String text = editor.getAsText();
				if (String.class.isAssignableFrom(targetType.getClass())) {
					return text;
				}
				return convert(text, STRING_TYPE, targetType);
			}
		}
		return delegate.convertIfNecessary(value, targetType.getType());
	}

	@SuppressWarnings("unchecked")
	public <T> T convert(Object source, Class<T> targetType) {
		return (T) convert(source, TypeDescriptor.forObject(source), TypeDescriptor.valueOf(targetType));
	}

	/**
	 * @param converter
	 * @see ConverterRegistry#addConverter(Converter)
	 */
	public void addConverter(Converter<?, ?> converter) {
		if (conversionService instanceof ConverterRegistry) {
			((ConverterRegistry) conversionService).addConverter(converter);
		}
	}

	/**
	 * @param converter
	 * @see ConverterRegistry#addConverter(GenericConverter)
	 */
	public void addConverter(GenericConverter converter) {
		if (conversionService instanceof ConverterRegistry) {
			((ConverterRegistry) conversionService).addConverter(converter);
		}
	}

	/**
	 * @param converterFactory
	 * @see ConverterRegistry#addConverterFactory(ConverterFactory)
	 */
	public void addConverterFactory(ConverterFactory<?, ?> converterFactory) {
		if (conversionService instanceof ConverterRegistry) {
			((ConverterRegistry) conversionService).addConverterFactory(converterFactory);
		}
	}

	/**
	 * @param sourceType
	 * @param targetType
	 * @see ConverterRegistry#removeConvertible(java.lang.Class,
	 * java.lang.Class)
	 */
	public void removeConvertible(Class<?> sourceType, Class<?> targetType) {
		if (conversionService instanceof ConverterRegistry) {
			((ConverterRegistry) conversionService).removeConvertible(sourceType, targetType);
		}
	}

}