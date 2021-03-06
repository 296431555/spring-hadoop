/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.data.hadoop.test.context;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

/**
 * Interface for Hadoop miniclusters.
 *
 * @author Janne Valkealahti
 *
 */
public interface HadoopCluster {

	/**
	 * Gets the {@link Configuration} for the cluster.
	 * As most of the configuration parameters are not
	 * known until after cluster has been started, this
	 * configuration should be configured by the
	 * cluster itself.
	 *
	 * @return the Cluster configured {@link Configuration}
	 */
	Configuration getConfiguration();

	/**
	 * Starts the cluster.
	 *
	 * @throws Exception if cluster failed to start
	 */
	void start() throws Exception;

	/**
	 * Stops the cluster.
	 */
	void stop();

	/**
	 * Gets the configured {@link FileSystem} managed
	 * by {@link HadoopCluster}.
	 *
	 * @return file system managed by cluster
	 * @throws IOException if error occured
	 */
	FileSystem getFileSystem() throws IOException;

}
